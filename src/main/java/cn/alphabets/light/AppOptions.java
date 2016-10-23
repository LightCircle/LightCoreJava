package cn.alphabets.light;

/**
 * Created by luohao on 16/10/22.
 */
public class AppOptions {
    private String appDomain;
    private int appPort;
    private String dbHost;
    private int dbPort;
    private String dbUser;
    private String dbPass;
    private boolean isDev;
    private String packageNmae;

    public String getAppDomain() {
        return appDomain;
    }

    public int getAppPort() {
        return appPort;
    }

    public String getDbHost() {
        return dbHost;
    }

    public int getDbPort() {
        return dbPort;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPass() {
        return dbPass;
    }

    public boolean isDev() {
        return isDev;
    }

    public String getPackageNmae() {
        return packageNmae;
    }

    public AppOptions setAppDomain(String appDomain) {
        this.appDomain = appDomain;
        return this;
    }

    public AppOptions setAppPort(int appPort) {
        this.appPort = appPort;
        return this;
    }

    public AppOptions setDbHost(String dbHost) {
        this.dbHost = dbHost;
        return this;
    }

    public AppOptions setDbPort(int dbPort) {
        this.dbPort = dbPort;
        return this;
    }

    public AppOptions setDbUser(String dbUser) {
        this.dbUser = dbUser;
        return this;
    }

    public AppOptions setDbPass(String dbPass) {
        this.dbPass = dbPass;
        return this;
    }

    public AppOptions setDev(boolean dev) {
        isDev = dev;
        return this;
    }

    public AppOptions setPackageNmae(String packageNmae) {
        this.packageNmae = packageNmae;
        return this;
    }

    public AppOptions() {
    }

    public AppOptions(String appDomain, int appPort, String dbHost, int dbPort, String dbUser, String dbPass, boolean isDev, String packageNmae) {
        this.appDomain = appDomain;
        this.appPort = appPort;
        this.dbHost = dbHost;
        this.dbPort = dbPort;
        this.dbUser = dbUser;
        this.dbPass = dbPass;
        this.isDev = isDev;
        this.packageNmae = packageNmae;
    }
}
