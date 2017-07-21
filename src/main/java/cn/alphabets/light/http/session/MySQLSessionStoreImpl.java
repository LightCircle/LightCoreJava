//package cn.alphabets.light.http.session;
//
//import cn.alphabets.light.Constant;
//import cn.alphabets.light.Environment;
//import cn.alphabets.light.db.mongo.Connection;
//import cn.alphabets.light.db.mysql.Model;
//import cn.alphabets.light.entity.ModUser;
//import com.mongodb.MongoClient;
//import com.mongodb.client.MongoDatabase;
//import com.mongodb.client.model.Filters;
//import com.mongodb.client.model.UpdateOptions;
//import com.mongodb.client.model.Updates;
//import com.mongodb.client.result.DeleteResult;
//import com.mongodb.client.result.UpdateResult;
//import io.vertx.core.AsyncResult;
//import io.vertx.core.Future;
//import io.vertx.core.Handler;
//import io.vertx.core.Vertx;
//import io.vertx.core.logging.Logger;
//import io.vertx.core.logging.LoggerFactory;
//import io.vertx.ext.web.Session;
//import io.vertx.ext.web.sstore.SessionStore;
//import org.bson.Document;
//import org.bson.conversions.Bson;
//import org.bson.types.ObjectId;
//
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * MySQLSessionStoreImpl
// */
//public class MySQLSessionStoreImpl implements SessionStore {
//    private static final Logger log = LoggerFactory.getLogger(MySQLSessionStoreImpl.class);
//
//    private static final String SESSION_COLLECTION_NAME = "jsessions";
//    private String domain;
//    private Vertx vertx;
//    private Model model;
//
//    public MySQLSessionStoreImpl(String domain, Vertx vertx) {
//        this.domain = domain;
//        this.model = new Model(domain, Constant.SYSTEM_DB_PREFIX);
//        this.vertx = vertx;
//    }
//
//    @Override
//    public long retryTimeout() {
//        return 0;
//    }
//
//    @Override
//    public Session createSession(long timeout) {
//        return new SessionImpl(timeout);
//    }
//
//    @Override
//    public void get(String id, Handler<AsyncResult<Session>> resultHandler) {
//
//        String script = String.format("SELECT * FROM `%s`.`%s` WHERE _id = <%%= condition._id %%>",
//                this.domain, SESSION_COLLECTION_NAME);
//
//        Document doc = this.model.get(script, new Document("_id", id));
//        Session session = SessionImpl.fromDoc(doc);
//        if (session != null && System.currentTimeMillis() - session.lastAccessed() > session.timeout()) {
//            this.delete(session.id(), result -> {
//            });
//            session = null;
//        }
//        resultHandler.handle(Future.succeededFuture(session));
//    }
//
//    @Override
//    public void delete(String id, Handler<AsyncResult<Boolean>> resultHandler) {
//
//        String script = String.format("DELETE FROM `%s`.`%s` WHERE _id = <%%= condition._id %%>",
//                this.domain, SESSION_COLLECTION_NAME);
//
//        this.model.remove(script, new Document("_id", id));
//
//        DeleteResult result = mongo.getCollection(SESSION_COLLECTION_NAME)
//                .deleteOne(new Document("_id", new ObjectId(id)));
//
//        resultHandler.handle(Future.succeededFuture(result.wasAcknowledged()));
//    }
//
//    @Override
//    public void put(Session session, Handler<AsyncResult<Boolean>> resultHandler) {
//
//        List<Bson> updates = new ArrayList<>();
//
//        //store session rawdata
//        String raw = ((SessionImpl) session).toRawString();
//        updates.add(Updates.set("rawData", raw));
//
//        //store user _id if user exist
//        ModUser user = ((SessionImpl) session).get(Constant.SK_USER);
//        if (user != null) {
//            updates.add(Updates.set("uid", user.getId()));
//        }
//
//        //store lastAccessed
//        updates.add(Updates.set("lastAccessed", session.lastAccessed()));
//
//        //store timeout in ms
//        updates.add(Updates.set("timeoutMS", session.timeout()));
//
//        //store isDestroyed
//        updates.add(Updates.set("isDestroyed", session.isDestroyed()));
//
//        UpdateResult result = mongo.getCollection(SESSION_COLLECTION_NAME)
//                .updateOne(new Document("_id", new ObjectId(session.id()))
//                        , Updates.combine(updates)
//                        , new UpdateOptions().upsert(true));
//
//        resultHandler.handle(Future.succeededFuture(result.wasAcknowledged()));
//    }
//
//    @Override
//    public void clear(Handler<AsyncResult<Boolean>> resultHandler) {
//        mongo.getCollection(SESSION_COLLECTION_NAME).drop();
//        resultHandler.handle(Future.succeededFuture(true));
//    }
//
//    @Override
//    public void size(Handler<AsyncResult<Integer>> resultHandler) {
//        int count = (int) mongo.getCollection(SESSION_COLLECTION_NAME).count();
//        resultHandler.handle(Future.succeededFuture(count));
//    }
//
//    @Override
//    public void close() {
//
//    }
//}
//
