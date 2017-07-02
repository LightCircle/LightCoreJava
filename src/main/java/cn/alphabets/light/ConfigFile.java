package cn.alphabets.light;

/**
 * ConfigFile
 * Created by lilin on 2016/11/11.
 */
public class ConfigFile {
    public static class ConfigApp {
        public boolean isDev() {
            return dev;
        }

        public void setDev(boolean dev) {
            this.dev = dev;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public boolean isMaster() {
            return master;
        }

        public void setMaster(boolean master) {
            this.master = master;
        }

        public boolean isLocal() {
            return local;
        }

        public void setLocal(boolean local) {
            this.local = local;
        }

        public String getPackages() {
            return packages;
        }

        public void setPackages(String packages) {
            this.packages = packages;
        }

        private boolean dev;
        private int port;
        private String domain;
        private boolean master;
        private boolean local;
        private String packages;
    }

    public static class ConfigMongoDB {
        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPass() {
            return pass;
        }

        public void setPass(String pass) {
            this.pass = pass;
        }

        public String getAuth() {
            return auth;
        }

        public void setAuth(String auth) {
            this.auth = auth;
        }

        private String host;
        private int port;
        private String user;
        private String pass;
        private String auth;
    }

    public static class ConfigMySQL {
        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPass() {
            return pass;
        }

        public void setPass(String pass) {
            this.pass = pass;
        }

        private String host;
        private int port;
        private String user;
        private String pass;
    }

    public static class ConfigBinary {

        public String[] getSuffix() {
            return suffix;
        }

        public void setSuffix(String[] suffix) {
            this.suffix = suffix;
        }

        public String[] getFile() {
            return file;
        }

        public void setFile(String[] file) {
            this.file = file;
        }

        private String[] suffix;
        private String[] file;
    }
}
