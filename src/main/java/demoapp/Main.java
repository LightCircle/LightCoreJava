package demoapp;


import cn.alphabets.light.App;
import cn.alphabets.light.AppOptions;

/**
 * Created by luohao on 16/10/20.
 */
public class Main {
    public static void main(String[] args) {

        new App(new AppOptions()
                .setAppDomain("4d5f4746d95d")
                .setAppPort(45689)
                .setDbHost("db.alphabets.cn")
                .setDbPort(54017)
                .setDbUser("dev")
                .setDbPass("dev")
                .setDev(true)
                .setPackageNmae("demoapp"))
                .start();
    }
}
