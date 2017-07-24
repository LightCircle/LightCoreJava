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
    private String domain;
    private String code;

    private Model() {
    }

    public Model(String domain, String code) {
        try {
            // TODO: 暂不支持domain和code
            this.domain = domain;
            this.code = code;
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
        if (!data.containsKey("_id") || data.getObjectId("_id") == null) {
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

    public long increase(String type) {

        String select = String.format("SELECT `sequence` + 1 AS sequence " +
                "FROM `%s`.`counter` WHERE `type` = <%%= condition.type %%>", this.domain);

        String update = String.format("UPDATE `%s`.`counter` " +
                "SET `sequence` = `sequence` + 1 WHERE `type` = <%%= condition.type %%>", this.domain);

        String insert = String.format("INSERT INTO `%s`.`counter` (`_id`, `valid`, `type`, `sequence`) VALUES (" +
                "<%%= data._id %%>, 1, <%%= data.type %%>, <%%= data.sequence %%>)", this.domain);

        Document condition = new Document("type", type);
        Document document = this.get(select, condition);
        if (document == null) {
            Document data = new Document();
            data.put("_id", new ObjectId());
            data.put("type", type);
            data.put("sequence", 1);
            this.add(insert, data);
            return 1;
        }

        this.update(update, new Document(), condition);
        return document.getLong("sequence");
    }

    private String getSql(String sql, Document params) {

        // escape sql （参数里的 escape 参照 parse 方法）
        sql = sql.replaceAll(";", "");

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
            return String.format("'%s'", escapeSql(((ObjectId) val).toHexString()));
        } else if (val instanceof Date) {
            return String.format("'%s'", dateFormat.format((Date) val));
        } else if (val instanceof String) {
            return String.format("'%s'", escapeSql(val));
        } else if (val instanceof List) {
            List list = (List) ((List) val).stream().map(this::parse).collect(Collectors.toList());
            return String.format("(%s)", StringUtils.join(list, ","));
        }
        return escapeSql(String.valueOf(val));
    }

    private String escapeSql(Object sql) {
        return ((String) sql).replaceAll("'", "''");
    }

    // 对数据库检索出的数据进行类型转换
    private Object parseByMetaType(String type, Object value) {

        switch (type) {
            case "DECIMAL":
                return ((BigDecimal) value).longValue();
            case "DATETIME":
                if (value == null) {
                    return null;
                }
                return new java.util.Date(((Timestamp) value).getTime());
            case "BIGINT":
            case "VARCHAR":
            case "INT":
                return value;
        }

        throw new RuntimeException("Core has not yet supported the data type.");
    }
}
