package cn.alphabets.light.model;


import cn.alphabets.light.http.session.LightSession;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.Session;

import java.util.Base64;

/**
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
        LightSession session1 = (LightSession) session;
        Buffer buffer = Buffer.buffer();
        session1.writeToBuffer(buffer);
        ModSession modSession = new ModSession();
        modSession.rawData = new String(Base64.getEncoder().encode(buffer.getBytes()));
        return modSession;
    }

    public static LightSession toSession(ModSession mod) {
        if (mod == null) {
            return null;
        }
        byte[] aa = Base64.getDecoder().decode(mod.getRawData());
        Buffer buffer = Buffer.buffer();
        buffer.appendBytes(aa);
        LightSession s = new LightSession();
        s.readFromBuffer(0, buffer);
        return s;
    }
}
