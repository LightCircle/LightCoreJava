package cn.alphabets.light.http.session;

import cn.alphabets.light.db.mongo.DBConnection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.sstore.SessionStore;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Created by luohao on 2016/10/27.
 */
public class MongoSessionStoreImpl implements SessionStore {
    private static final Logger log = LoggerFactory.getLogger(MongoSessionStoreImpl.class);

    private DBConnection mongo;
    private static final String SESSION_COLLECTION_NAME = "sessiontttt";
    private Vertx vertx;

    public MongoSessionStoreImpl(DBConnection mongo, Vertx vertx) {
        this.mongo = mongo;
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


        Document doc = mongo.getCollection(SESSION_COLLECTION_NAME)
                .find(Filters.eq("_id", new ObjectId(id)))
                .first();

        Session session = SessionImpl.fromDoc(doc);
        if (session != null
                && System.currentTimeMillis() - session.lastAccessed() > session.timeout()) {
            this.delete(session.id(), result -> {
            });
            session = null;
        }
        resultHandler.handle(Future.succeededFuture(session));
    }

    @Override
    public void delete(String id, Handler<AsyncResult<Boolean>> resultHandler) {

        DeleteResult result = mongo.getCollection(SESSION_COLLECTION_NAME)
                .deleteOne(new Document("_id", new ObjectId(id)));

        resultHandler.handle(Future.succeededFuture(result.wasAcknowledged()));
    }

    @Override
    public void put(Session session, Handler<AsyncResult<Boolean>> resultHandler) {


        String raw = ((SessionImpl) session).toRawString();
        UpdateResult result = mongo.getCollection(SESSION_COLLECTION_NAME)
                .updateOne(new Document("_id", new ObjectId(session.id()))
                        , Updates.set("rawData", raw)
                        , new UpdateOptions().upsert(true));

        resultHandler.handle(Future.succeededFuture(result.wasAcknowledged()));
    }

    @Override
    public void clear(Handler<AsyncResult<Boolean>> resultHandler) {
        mongo.getCollection(SESSION_COLLECTION_NAME).drop();
        resultHandler.handle(Future.succeededFuture(true));
    }

    @Override
    public void size(Handler<AsyncResult<Integer>> resultHandler) {
        int count = (int) mongo.getCollection(SESSION_COLLECTION_NAME).count();
        resultHandler.handle(Future.succeededFuture(count));
    }

    @Override
    public void close() {

    }
}

