package cn.alphabets.light.db.mysql;

import cn.alphabets.light.Environment;
import cn.alphabets.light.Helper;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Model
 * Created by lilin on 2017/7/16.
 */
public class Model {

    private static final Logger logger = LoggerFactory.getLogger(Model.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private Connection db;

    private Model() {
    }

    public Model(String domain, String code) {
        try {
            // TODO: 暂不支持domain和code
            this.db = cn.alphabets.light.db.mysql.Connection.instance(Environment.instance());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Document> list(String query, Document params) {

        String sql = this.getSql(query, new Document("condition", this.parseByValueType(params)));

        PreparedStatement ps = null;
        SQLException exception = null;
        ResultSet rs = null;

        logger.debug(sql);

        try {
            ps = this.db.prepareStatement(sql);
            rs = ps.executeQuery();
            return getEntitiesFromResultSet(rs);
        } catch (SQLException e) {
            exception = e;
            throw new RuntimeException(exception);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException e) {
                if (exception != null) {
                    exception.addSuppressed(e);
                }
            }
        }
    }

    public Document get(String query, Document params) {
        List<Document> documents = this.list(query, params);
        if (documents != null && documents.size() > 0) {
            return documents.get(0);
        }

        return null;
    }

    public long count(String query, Document params) {
        List<Document> documents = this.list(query, params);
        if (documents != null && documents.size() > 0) {
            return documents.get(0).getLong("COUNT");
        }

        return 0;
    }

    public Document add(String query, Document data) {
        if (!data.containsKey("_id")) {
            data.put("_id", new ObjectId());
        }
        long count = this.update(query, data, null);
        if (count > 0) {
            return data;
        }

        return null;
    }

    public long remove(String query, Document condition) {
        return this.update(query, null, condition);
    }

    public long update(String query, Document data, Document condition) {

        Document params = new Document();
        if (data != null) {
            params.put("data", this.parseByValueType(data));
        }
        if (condition != null) {
            params.put("condition", this.parseByValueType(condition));
        }

        String sql = this.getSql(query, params);
        PreparedStatement ps = null;
        SQLException exception = null;

        logger.debug(sql);

        try {
            ps = this.db.prepareStatement(sql);
            return ps.executeUpdate();
        } catch (SQLException e) {
            exception = e;
            throw new RuntimeException(exception);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException e) {
                if (exception != null) {
                    exception.addSuppressed(e);
                }
            }
        }
    }

    private String getSql(String sql, Document params) {

        // TODO: condition.name.replace(/[ ;].*/g, ""); // 防止SQL注入
        return Helper.loadInlineTemplate(sql, params);
    }

    private List<Document> getEntitiesFromResultSet(ResultSet rs) throws SQLException {
        List<Document> entities = new ArrayList<>();
        while (rs.next()) {
            entities.add(this.getEntityFromResultSet(rs));
        }
        return entities;
    }

    private Document getEntityFromResultSet(ResultSet rs) throws SQLException {

        Document document = new Document();

        ResultSetMetaData meta = rs.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); ++i) {

            String columnName = meta.getColumnName(i);
            Object columnValue = this.parseByMetaType(
                    meta.getColumnTypeName(i),
                    rs.getObject(i));

            document.put(columnName, columnValue);
        }
        return document;
    }

    private Document parseByValueType(Document document) {
        Document values = new Document();
        document.forEach((key, val) -> values.put(key, parse(val)));
        return values;
    }

    private Object parse(Object val) {
        if (val == null) {
            return "null";
        } else if (val instanceof Boolean) {
            return ((boolean) val) ? 1 : 0;
        } else if (val instanceof ObjectId) {
            return String.format("'%s'", ((ObjectId) val).toHexString());
        } else if (val instanceof Date) {
            return String.format("'%s'", dateFormat.format((Date) val));
        } else if (val instanceof String) {
            return String.format("'%s'", val);
        } else if (val instanceof List) {
            List list = (List) ((List) val).stream().map(this::parse).collect(Collectors.toList());
            return String.format("(%s)", StringUtils.join(list, ","));
        }
        return val;
    }

    // 对数据库检索出的数据进行类型转换
    private Object parseByMetaType(String type, Object value) {

        switch (type) {
            case "DECIMAL":
                return ((BigDecimal) value).longValue();
            case "DATETIME":
                return new java.util.Date(((Timestamp) value).getTime());
            case "BIGINT":
            case "VARCHAR":
            case "INT":
                return value;
        }

        throw new RuntimeException("Core has not yet supported the data type.");
    }
}
