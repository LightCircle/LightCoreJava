package cn.alphabets.light.http.session;

import cn.alphabets.light.Constant;
import cn.alphabets.light.db.mysql.Model;
import cn.alphabets.light.entity.ModUser;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.sstore.SessionStore;
import org.bson.Document;

/**
 * MySQLSessionStoreImpl
 */
public class MySQLSessionStoreImpl implements SessionStore {
    private static final Logger log = LoggerFactory.getLogger(MySQLSessionStoreImpl.class);

    private static final String SESSION_COLLECTION_NAME = "jsessions";
    private String domain;
    private Vertx vertx;
    private Model model;

    public MySQLSessionStoreImpl(String domain, Vertx vertx) {
        this.domain = domain;
        this.model = new Model(domain, Constant.SYSTEM_DB_PREFIX);
        this.vertx = vertx;
    }

    @Override
    public long retryTimeout() {
        return 0;
    }

    @Override
    public Session createSession(long timeout) {
        return new SessionImpl(timeout);
    }

    @Override
    public void get(String id, Handler<AsyncResult<Session>> resultHandler) {

        String script = String.format("SELECT * FROM `%s`.`%s` WHERE `_id` = <%%= condition._id %%>",
                this.domain, SESSION_COLLECTION_NAME);

        Document doc = this.model.get(script, new Document("_id", id));
        Session session = SessionImpl.fromDoc(doc);
        if (session != null && System.currentTimeMillis() - session.lastAccessed() > session.timeout()) {
            this.delete(session.id(), result -> {
            });
            session = null;
        }
        resultHandler.handle(Future.succeededFuture(session));
    }

    @Override
    public void delete(String id, Handler<AsyncResult<Boolean>> resultHandler) {

        String script = String.format("DELETE FROM `%s`.`%s` WHERE `_id` = <%%= condition._id %%>",
                this.domain, SESSION_COLLECTION_NAME);

        long count = this.model.remove(script, new Document("_id", id));
        resultHandler.handle(Future.succeededFuture(count > 0));
    }

    @Override
    public void put(Session session, Handler<AsyncResult<Boolean>> resultHandler) {

        Document updates = new Document();

        //store session rawdata
        String raw = ((SessionImpl) session).toRawString();
        updates.put("rawData", raw);

        //store user _id if user exist
        ModUser user = ((SessionImpl) session).get(Constant.SK_USER);
        if (user != null) {
            updates.put("uid", user.getId());
        }

        //store lastAccessed
        updates.put("lastAccessed", session.lastAccessed());

        //store timeout in ms
        updates.put("timeoutMS", session.timeout());

        //store isDestroyed
        updates.put("isDestroyed", session.isDestroyed());

        if (!updates.containsKey("uid")) {
            updates.put("uid", null);
        }

        if (!updates.containsKey("_id")) {
            updates.put("_id", session.id());
        }

        String script = String.format("SELECT COUNT(1) as COUNT FROM `%s`.`%s` WHERE `_id` = <%%= condition._id %%>",
                this.domain, SESSION_COLLECTION_NAME);
        long count = this.model.count(script, new Document("_id", session.id()));
        if (count > 0) {
            script = String.format("UPDATE `%s`.`%s` SET " +
                            "`_id` = <%%= data._id %%>, `rawData` = <%%= data.rawData %%>, " +
                            "`lastAccessed` = <%%= data.lastAccessed %%>, `timeoutMS` = <%%= data.timeoutMS %%>, " +
                            "`isDestroyed` = <%%= data.isDestroyed %%>, `uid` = <%%= data.uid %%> " +
                            "WHERE `_id` = <%%= condition._id %%>",
                    this.domain, SESSION_COLLECTION_NAME);

            count = this.model.update(script, updates, new Document("_id", session.id()));
            resultHandler.handle(Future.succeededFuture(count > 0));
        } else {
            script = String.format(
                    "INSERT INTO `%s`.`%s` (`_id`,`rawData`,`lastAccessed`,`timeoutMS`,`isDestroyed`,`uid`) " +
                            "VALUES (" +
                            "<%%= data._id %%>, <%%= data.rawData %%>, " +
                            "<%%= data.lastAccessed %%>, <%%= data.timeoutMS %%>, " +
                            "<%%= data.isDestroyed %%>, <%%= data.uid %%>" +
                            ")",
                    this.domain, SESSION_COLLECTION_NAME);
            this.model.add(script, updates);
            resultHandler.handle(Future.succeededFuture(true));
        }
    }

    @Override
    public void clear(Handler<AsyncResult<Boolean>> resultHandler) {
        String script = String.format("DELETE FROM `%s`.`%s`", this.domain, SESSION_COLLECTION_NAME);
        this.model.remove(script, new Document());
        resultHandler.handle(Future.succeededFuture(true));
    }

    @Override
    public void size(Handler<AsyncResult<Integer>> resultHandler) {

        String script = String.format("SELECT COUNT(1) as COUNT FROM `%s`.`%s`",
                this.domain, SESSION_COLLECTION_NAME);

        long count = this.model.count(script, new Document());
        resultHandler.handle(Future.succeededFuture((int) count));
    }

    @Override
    public void close() {
    }
}

