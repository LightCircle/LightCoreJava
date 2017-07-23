package cn.alphabets.light.http;

import cn.alphabets.light.config.ConfigManager;
import cn.alphabets.light.exception.BadRequestException;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.SessionHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * CSRFHandler
 * Created by lilin on 2016/11/16.
 */
public interface CSRFHandler extends Handler<RoutingContext> {

    String DEFAULT_COOKIE_NAME = "csrf-token";
    String DEFAULT_HEADER_NAME = "csrf-token";
    String DEFAULT_QUERY_NAME = "_csrf";

    static CSRFHandler create(String secret) {
        return new CSRFHandlerImpl(secret);
    }

    CSRFHandler setTimeout(long timeout);

    class CSRFHandlerImpl implements CSRFHandler {

        private static final Logger log = LoggerFactory.getLogger(CSRFHandlerImpl.class);
        private static final Base64.Encoder BASE64 = Base64.getMimeEncoder();

        private final Random RAND = new SecureRandom();
        private final Mac mac;

        private boolean nagHttps;
        private String cookieName = DEFAULT_COOKIE_NAME;
        private String headerName = DEFAULT_HEADER_NAME;
        private String queryName = DEFAULT_QUERY_NAME;
        private long timeout = SessionHandler.DEFAULT_SESSION_TIMEOUT;

        private Pattern ignore;

        CSRFHandlerImpl(final String secret) {
            try {
                mac = Mac.getInstance("HmacSHA256");
                mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }

            List<String> paths = ConfigManager.INSTANCE.getIgnoreCsrf();
            if (CollectionUtils.isNotEmpty(paths)) {
                String regex = StringUtils.join(paths, "|");
                ignore = Pattern.compile(regex.replaceAll("(?<!\\.)\\*", ".*"));
            }
        }

        public CSRFHandler setCookieName(String cookieName) {
            this.cookieName = cookieName;
            return this;
        }

        public CSRFHandler setHeaderName(String headerName) {
            this.headerName = headerName;
            return this;
        }

        public CSRFHandler setQueryName(String queryName) {
            this.queryName = queryName;
            return this;
        }

        @Override
        public CSRFHandler setTimeout(long timeout) {
            this.timeout = timeout;
            return this;
        }

        public CSRFHandler setNagHttps(boolean nag) {
            this.nagHttps = nag;
            return this;
        }

        private String generateToken() {
            byte[] salt = new byte[32];
            RAND.nextBytes(salt);

            String saltPlusToken = BASE64.encodeToString(salt) + "." + Long.toString(System.currentTimeMillis());
            String signature = BASE64.encodeToString(mac.doFinal(saltPlusToken.getBytes()));

            return saltPlusToken + "." + signature;
        }

        private boolean validateToken(String header) {

            if (header == null) {
                return false;
            }

            String[] tokens = header.split("\\.");
            if (tokens.length != 3) {
                return false;
            }

            String saltPlusToken = tokens[0] + "." + tokens[1];
            String signature = BASE64.encodeToString(mac.doFinal(saltPlusToken.getBytes()));

            if (!signature.equals(tokens[2])) {
                return false;
            }

            try {
                // validate validity
                return !(System.currentTimeMillis() > Long.parseLong(tokens[1]) + timeout);
            } catch (NumberFormatException e) {
                return false;
            }
        }

        @Override
        public void handle(RoutingContext ctx) {

            if (nagHttps) {
                String uri = ctx.request().absoluteURI();
                if (!uri.startsWith("https:")) {
                    log.warn("Using session cookies without https could make you susceptible to session hijacking: " + uri);
                }
            }

            HttpMethod method = ctx.request().method();

            // ignore path
            String path = ctx.request().path();
            if (ignore != null && ignore.matcher(path).matches()) {
                ctx.next();
                return;
            }

            switch (method) {
                case GET:
                    final String token = generateToken();
                    // put the token in the context for users who prefer to render the token directly on the HTML
                    ctx.put(headerName, token);
                    ctx.addCookie(Cookie.cookie(cookieName, token));
                    ctx.next();
                    break;
                case POST:
                case PUT:
                case DELETE:
                case PATCH:

                    // 尝试从Header里获取
                    String value = this.decode(ctx.request().getHeader(headerName));

                    // 尝试从body里获取
                    if (value == null) {
                        value = this.decode(ctx.request().getFormAttribute(queryName));
                    }

                    // 尝试从query里获取
                    if (value == null) {
                        value = this.decode(ctx.request().params().get(queryName));
                    }

                    if (validateToken(value)) {
                        ctx.next();
                    } else {
                        ctx.fail(new BadRequestException("CSRF error."));
                    }
                    break;
                default:
                    // ignore these methods
                    ctx.next();
                    break;
            }
        }

        private String decode(String original) {
            if (original == null) {
                return null;
            }
            try {
                return URLDecoder.decode(original, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return original;
            }
        }
    }
}
