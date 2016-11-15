package cn.alphabets.light.model;

import cn.alphabets.light.http.session.SessionImpl;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.Session;

import java.util.Base64;

/**
 * ModSession
 * Created by luohao on 16/10/22.
 */
public class ModSession {
    private String _id;
    private String rawData;
    private String uid;

    public String getRawData() {
        return rawData;
    }

    public static ModSession fromSession(Session session) {
        SessionImpl session1 = (SessionImpl) session;
        Buffer buffer = Buffer.buffer();
        session1.writeToBuffer(buffer);
        ModSession modSession = new ModSession();
        modSession.rawData = new String(Base64.getEncoder().encode(buffer.getBytes()));
        return modSession;
    }

    public static SessionImpl toSession(ModSession mod) {
        if (mod == null) {
            return null;
        }
        byte[] aa = Base64.getDecoder().decode(mod.getRawData());
        Buffer buffer = Buffer.buffer();
        buffer.appendBytes(aa);
        SessionImpl s = new SessionImpl();
        s.readFromBuffer(0, buffer);
        return s;
    }
}
