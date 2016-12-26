package cn.alphabets.light.job;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.entity.ModUser;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.model.ModCommon;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import org.bson.types.ObjectId;

import java.util.TimeZone;

/**
 * Created by luohao on 2016/12/26.
 */
public class JobContext extends Context {


    private ModUser fakeSessionUser;


    public JobContext() {
        this(null, null, null);
    }

    public JobContext(String code) {
        this(null, code, null);
    }

    public JobContext(String domain, String code) {
        this(domain, code, null);
    }

    public JobContext(String domain, String code, ModUser user) {
        this.domain = domain == null ? Environment.instance().getAppName() : domain;
        this.code = code == null ? Constant.DEFAULT_TENANT : code;

        if (user == null) {
            user = new ModUser();
            user.set_id(new ObjectId(Constant.DEFAULT_JOB_USER_ID));
            user.setLang(Constant.DEFAULT_JOB_USER_LANG);
        }
        fakeSessionUser = user;

    }


    @Override
    public HttpServerResponse res() {
        throw new UnsupportedOperationException("JobContext#res unsupported");
    }

    @Override
    public HttpServerRequest req() {
        throw new UnsupportedOperationException("JobContext#req unsupported");
    }

    @Override
    public RoutingContext ctx() {
        throw new UnsupportedOperationException("JobContext#ctx unsupported");
    }

    @Override
    public Session session() {
        throw new UnsupportedOperationException("JobContext#session unsupported");
    }

    @Override
    public Object user() {
        return fakeSessionUser;
    }

    @Override
    public void setUser(ModCommon user) {
        fakeSessionUser = (ModUser) user;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String uid() {
        return fakeSessionUser.get_id().toString();
    }

    @Override
    public void setUid(String uid) {
        super.setUid(uid);
    }

    @Override
    public TimeZone getTimeZone() {
        return super.getTimeZone();
    }

    @Override
    public String getLang() {
        return fakeSessionUser.getLang();
    }
}
