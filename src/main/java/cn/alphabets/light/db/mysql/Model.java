package cn.alphabets.light.db.mysql;

import cn.alphabets.light.Environment;
import cn.alphabets.light.Helper;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.bson.Document;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static javax.lang.model.type.TypeKind.INT;

/**
 * Model
 * Created by lilin on 2017/7/16.
 */
public class Model {

    private static final Logger logger = LoggerFactory.getLogger(Model.class);
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

        PreparedStatement ps = null;
        SQLException exception = null;
        ResultSet rs = null;
        String sql = this.getSql(query, new Document("condition", params));

        System.out.println(sql);

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
            System.out.println(columnName);
            Object columnValue = this.parseByMetaType(
                    columnName,
                    meta.getColumnTypeName(i),
                    rs.getObject(i));

            document.put(columnName, columnValue);
        }
        return document;
    }

    private Object parseByMetaType(String name, String type, Object value) {

        if (name.equals("_id")) {
            String val = "000000000000000000000000" + value;
            return val.substring(val.length() - 24);
        }

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
