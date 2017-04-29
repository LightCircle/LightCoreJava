package cn.alphabets.light.job;

import cn.alphabets.light.Environment;
import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.entity.ModJob;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Properties;

/**
 * Created by luohao on 2016/12/24.
 */
public class JobManager {
    private static final Logger logger = LoggerFactory.getLogger(JobManager.class);

    public static void start() {

        //start job only app is master
        if (!Environment.instance().isMaster()) {
            return;
        }

        try {

            logger.info("Jobs starting ...");
            int pollSize = CacheManager.INSTANCE.getJobs().size() + 1;

            Properties properties = new Properties();
            //set thread poll size
            properties.setProperty("org.quartz.threadPool.threadCount", String.valueOf(pollSize));
            //disable quartz check new version feature
            properties.setProperty("org.quartz.scheduler.skipUpdateCheck", "true");

            StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory(properties);
            Scheduler scheduler = stdSchedulerFactory.getScheduler();
            scheduler.start();

            for (ModJob job : CacheManager.INSTANCE.getJobs()) {
                if ("STARTED".equals(job.getRun())) {
                    JobExecutor jobExecutor = new JobExecutor(job);
                    scheduler.scheduleJob(jobExecutor.getDetail(), jobExecutor.getTrigger());
                }
            }
            logger.info("Jobs started .");
        } catch (Exception e) {
            logger.error("Jobs start failed : ", e);
        }

    }
}
