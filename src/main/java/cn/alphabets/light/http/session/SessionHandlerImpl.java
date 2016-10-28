package cn.alphabets.light.http.session;

/**
 * Created by luohao on 2016/10/28.
 */


import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.SessionStore;

public class SessionHandlerImpl implements SessionHandler {

    private static final Logger log = LoggerFactory.getLogger(io.vertx.ext.web.handler.impl.SessionHandlerImpl.class);


    /**
     * Default name of session cookie
     */
    private static String DEFAULT_SESSION_COOKIE_NAME = "light.sid";

    /**
     * Default time, in ms, that a session lasts for without being accessed before expiring.
     */
    private static long DEFAULT_SESSION_TIMEOUT = 30 * 60 * 1000; // 30 minutes

    /**
     * Default of whether a nagging log warning should be written if the session handler is accessed over HTTP, not
     * HTTPS
     */
    private static boolean DEFAULT_NAG_HTTPS = true;

    /**
     * Default of whether the cookie has the HttpOnly flag set
     * More info: https://www.owasp.org/index.php/HttpOnly
     */
    private static boolean DEFAULT_COOKIE_HTTP_ONLY_FLAG = false;

    /**
     * Default of whether the cookie has the 'secure' flag set to allow transmission over https only.
     * More info: https://www.owasp.org/index.php/SecureFlag
     */
    private static boolean DEFAULT_COOKIE_SECURE_FLAG = false;

    private final SessionStore sessionStore;
    private String sessionCookieName;
    private long sessionTimeout;
    private boolean nagHttps;
    private boolean sessionCookieSecure;
    private boolean sessionCookieHttpOnly;

    public SessionHandlerImpl(String sessionCookieName, long sessionTimeout, boolean nagHttps, boolean sessionCookieSecure, boolean sessionCookieHttpOnly, SessionStore sessionStore) {
        this.sessionCookieName = sessionCookieName;
        this.sessionTimeout = sessionTimeout;
        this.nagHttps = nagHttps;
        this.sessionStore = sessionStore;
        this.sessionCookieSecure = sessionCookieSecure;
        this.sessionCookieHttpOnly = sessionCookieHttpOnly;
    }

    public static SessionHandlerImpl create(SessionStore sessionStore) {
        return new SessionHandlerImpl(DEFAULT_SESSION_COOKIE_NAME, DEFAULT_SESSION_TIMEOUT, DEFAULT_NAG_HTTPS, DEFAULT_COOKIE_SECURE_FLAG, DEFAULT_COOKIE_HTTP_ONLY_FLAG, sessionStore);
    }

    @Override
    public SessionHandler setSessionTimeout(long timeout) {
        this.sessionTimeout = timeout;
        return this;
    }

    @Override
    public SessionHandler setNagHttps(boolean nag) {
        this.nagHttps = nag;
        return this;
    }

    @Override
    public SessionHandler setCookieSecureFlag(boolean secure) {
        this.sessionCookieSecure = secure;
        return this;
    }

    @Override
    public SessionHandler setCookieHttpOnlyFlag(boolean httpOnly) {
        this.sessionCookieHttpOnly = httpOnly;
        return this;
    }

    @Override
    public SessionHandler setSessionCookieName(String sessionCookieName) {
        this.sessionCookieName = sessionCookieName;
        return this;
    }

    @Override
    public void handle(RoutingContext context) {
        context.response().ended();

        if (nagHttps) {
            String uri = context.request().absoluteURI();
            if (!uri.startsWith("https:")) {
                log.warn("Using session cookies without https could make you susceptible to session hijacking: " + uri);
            }
        }

        // Look for existing session cookie
        Cookie cookie = context.getCookie(sessionCookieName);
        if (cookie != null) {
            // Look up session
            String sessionID = cookie.getValue();
            getSession(context.vertx(), sessionID, res -> {
                if (res.succeeded()) {
                    Session session = res.result();
                    if (session != null) {
                        context.setSession(session);
                        session.setAccessed();
                        addStoreSessionHandler(context);
                        refreshCookie(context, session);
                    } else {
                        // Cannot find session - either it timed out, or was explicitly destroyed at the server side on a
                        // previous request.
                        // Either way, we create a new one.
                        createNewSession(context);
                    }
                } else {
                    context.fail(res.cause());
                }
                context.next();
            });
        } else {
            createNewSession(context);
            context.next();
        }
    }

    private void getSession(Vertx vertx, String sessionID, Handler<AsyncResult<Session>> resultHandler) {
        doGetSession(vertx, System.currentTimeMillis(), sessionID, resultHandler);
    }

    private void doGetSession(Vertx vertx, long startTime, String sessionID, Handler<AsyncResult<Session>> resultHandler) {
        sessionStore.get(sessionID, res -> {
            if (res.succeeded()) {
                if (res.result() == null) {
                    // Can't find it so retry. This is necessary for clustered sessions as it can take sometime for the session
                    // to propagate across the cluster so if the next request for the session comes in quickly at a different
                    // node there is a possibility it isn't available yet.
                    long retryTimeout = sessionStore.retryTimeout();
                    if (retryTimeout > 0 && System.currentTimeMillis() - startTime < retryTimeout) {
                        vertx.setTimer(5, v -> doGetSession(vertx, startTime, sessionID, resultHandler));
                        return;
                    }
                }
            }
            resultHandler.handle(res);
        });
    }

    private void addStoreSessionHandler(RoutingContext context) {
        context.addHeadersEndHandler(v -> {
            Session session = context.session();
            if (!session.isDestroyed()) {
                final int currentStatusCode = context.response().getStatusCode();
                // Store the session (only and only if there was no error)
                if (currentStatusCode >= 200 && currentStatusCode < 400) {
                    session.setAccessed();
                    sessionStore.put(session, res -> {
                        if (res.failed()) {
                            log.error("Failed to store session", res.cause());
                        }
                    });
                } else {
                    // don't send a cookie if status is not 2xx or 3xx
                    context.removeCookie(sessionCookieName);
                }
            } else {
                sessionStore.delete(session.id(), res -> {
                    if (res.failed()) {
                        log.error("Failed to delete session", res.cause());
                    }
                });
            }
        });
    }


    private void refreshCookie(RoutingContext context, Session session) {

        Cookie cookie = Cookie.cookie(sessionCookieName, session.id());
        cookie.setPath("/");
        cookie.setSecure(sessionCookieSecure);
        cookie.setHttpOnly(sessionCookieHttpOnly);
        // 设置cookie过期时间与session过期时间相同,session timeout 单位是毫秒,maxage单位是秒
        cookie.setMaxAge(session.timeout() / 1000);
        context.addCookie(cookie);
    }


    private void createNewSession(RoutingContext context) {
        Session session = sessionStore.createSession(sessionTimeout);
        context.setSession(session);
        Cookie cookie = Cookie.cookie(sessionCookieName, session.id());
        cookie.setPath("/");
        cookie.setSecure(sessionCookieSecure);
        cookie.setHttpOnly(sessionCookieHttpOnly);
        // 设置cookie过期时间与session过期时间相同,session timeout 单位是毫秒,maxage单位是秒
        cookie.setMaxAge(session.timeout() / 1000);
        context.addCookie(cookie);
        addStoreSessionHandler(context);
    }

}
