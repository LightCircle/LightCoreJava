package cn.alphabets.light.db.mysql;

import cn.alphabets.light.Environment;
import cn.alphabets.light.Helper;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.bson.Document;

import java.sql.Connection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Model
 * Created by lilin on 2017/7/16.
 */
public class Model {

    private static final Logger logger = LoggerFactory.getLogger(Model.class);
    private Connection db;

    private Model() {
    }

    public Model(String domain, String code) throws SQLException {
        // TODO: 暂不支持domain和code
        this.db = cn.alphabets.light.db.mysql.Connection.instance(Environment.instance());
    }


    public List<Document> list(String query, Document params) throws SQLException {

        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = this.getSql(query, params);

        logger.debug(sql);

        try {
            ps = this.db.prepareStatement(sql);
            rs = ps.executeQuery();
            return getEntitiesFromResultSet(rs);
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
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
            document.put(columnName, rs.getObject(i));

        }
        return document;
    }
}
