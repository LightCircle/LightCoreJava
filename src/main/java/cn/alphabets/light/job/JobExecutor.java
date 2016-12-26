package cn.alphabets.light.job;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.config.ConfigManager;
import cn.alphabets.light.db.mongo.Model;
import cn.alphabets.light.entity.ModJob;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang3.text.WordUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.quartz.*;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;


/**
 * Created by luohao on 2016/12/24.
 */
public class JobExecutor implements Job {
    private static final Logger logger = LoggerFactory.getLogger(JobExecutor.class);

    private JobDetail detail;
    private Trigger trigger;

    public JobExecutor() {
    }

    public JobExecutor(ModJob job) {
        trigger = TriggerBuilder
                .newTrigger()
                .withIdentity("trigger-" + job.get_id(), "light")
                .withSchedule(CronScheduleBuilder
                        .cronSchedule(job.getSchedule())
                        .inTimeZone(getTimeZone()))
                .build();

        detail = JobBuilder.newJob(JobExecutor.class)
                .withIdentity("job-" + job.get_id(), "light")
                .usingJobData("jobId", job.get_id().toString())
                .build();
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        ObjectId jobId = new ObjectId(dataMap.getString("jobId"));
        ModJob info = CacheManager.INSTANCE
                .getJobs()
                .stream()
                .filter(job -> job.get_id().equals(jobId))
                .findFirst()
                .get();

        logger.info(String.format("Job [%s] executing start ...", jobId.toString()));

        List<ModJob.Step> steps = info.getStep();

        try {
            for (ModJob.Step step : steps) {
                executeStep(step);
            }
            setJobStatus(info, true);
        } catch (JobExecutionException e) {
            setJobStatus(info, false);
            throw e;
        }
    }


    private void executeStep(ModJob.Step step) throws JobExecutionException {
        if ("action".equals(step.getType())) {
            executeAction(step);
        } else {
            executeScript(step);
        }
    }

    private void executeScript(ModJob.Step step) throws JobExecutionException {
        //TODO: support script
    }

    private void executeAction(ModJob.Step step) throws JobExecutionException {
        try {
            String clazz = step.getClass_();
            String fullName = Environment.instance().getPackages() + ".controller" + "." + WordUtils.capitalize(clazz);
            Method method = Class.forName(fullName).getMethod(step.getAction(), HashMap.class);
            boolean result = (boolean) method.invoke(method.getDeclaringClass().newInstance(), step.getParams());
            if (!result) {
                throw new JobExecutionException("Action method return false");
            }
        } catch (Exception e) {
            throw new JobExecutionException("Action method invoke by exception : ", e);
        }
    }


    private void setJobStatus(ModJob info, boolean success) {
        //update "last" & "status"
        String status = success ? "COMPLETED" : "FAILED";
        Environment env = Environment.instance();
        String code = Constant.SYSTEM_DB_PREFIX;

        try {
            new Model(env.getAppName(), code, Constant.SYSTEM_DB_JOB)
                    .update(new Document("_id", info.get_id()),
                            new Document("status", status).append("last", new Date()));
        } catch (Exception e) {
            logger.error("Error update job info", e);
        }

    }


    private TimeZone getTimeZone() {
        String conftz = ConfigManager.INSTANCE.getString(Constant.CFK_TIMEZONE);
        if (conftz != null) {
            return TimeZone.getTimeZone(conftz);
        }
        return TimeZone.getDefault();
    }

    public JobDetail getDetail() {
        return detail;
    }


    public Trigger getTrigger() {
        return trigger;
    }


}
