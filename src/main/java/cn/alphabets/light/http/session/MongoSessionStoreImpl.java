package cn.alphabets.light.http.session;

import cn.alphabets.light.db.mongo.DBConnection;
import cn.alphabets.light.model.ModSession;
import com.mongodb.WriteResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.sstore.SessionStore;
import org.bson.types.ObjectId;

/**
 * Created by luohao on 16/10/22.
 */
public class MongoSessionStoreImpl implements SessionStore {
    private static final Logger logger = LoggerFactory.getLogger(MongoSessionStoreImpl.class);

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
        return new LightSession(timeout);
    }

    @Override
    public void get(String id, Handler<AsyncResult<Session>> resultHandler) {
        ModSession modSession = mongo.getCollection(SESSION_COLLECTION_NAME)
                .findOne(new ObjectId(id))
                .as(ModSession.class);
        Session session = ModSession.toSession(modSession);
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
        WriteResult result = mongo.getCollection(SESSION_COLLECTION_NAME)
                .remove(new ObjectId(id));
        resultHandler.handle(Future.succeededFuture(result.wasAcknowledged()));
    }

    @Override
    public void put(Session session, Handler<AsyncResult<Boolean>> resultHandler) {
        WriteResult result = mongo.getCollection(SESSION_COLLECTION_NAME)
                .update(new ObjectId(session.id()))
                .upsert()
                .with(ModSession.fromSession(session));
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
