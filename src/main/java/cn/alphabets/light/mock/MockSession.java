package cn.alphabets.light.mock;

import io.vertx.ext.web.Session;

import java.util.Map;

/**
 * MockSession
 * Created by lilin on 2016/11/14.
 */
public class MockSession implements Session {
    @Override
    public String id() {
        return null;
    }

    @Override
    public Session put(String s, Object o) {
        return null;
    }

    @Override
    public <T> T get(String s) {
        return null;
    }

    @Override
    public <T> T remove(String s) {
        return null;
    }

    @Override
    public Map<String, Object> data() {
        return null;
    }

    @Override
    public long lastAccessed() {
        return 0;
    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean isDestroyed() {
        return false;
    }

    @Override
    public long timeout() {
        return 0;
    }

    @Override
    public void setAccessed() {

    }
}
