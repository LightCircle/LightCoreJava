package cn.alphabets.light.model.datamigrate;

import cn.alphabets.light.db.mongo.Model;
import cn.alphabets.light.entity.ModEtl;
import cn.alphabets.light.http.Context;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.bson.Document;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Common
 * Created by lilin on 2017/7/12.
 */
public class Common {

    private static final Logger logger = LoggerFactory.getLogger(EtlImporter.class);

    public static void invokeInitialize(Context handler, String clazz, Model model) {

        if (clazz == null) {
            return;
        }

        try {
            Method method = Class.forName(clazz).getMethod("initialize", Context.class, Model.class);
            method.invoke(method.getDeclaringClass().newInstance(), handler, model);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException e) {
            logger.debug("Did not find initialize method." + clazz);
        }
    }

    public static List<Document> invokeBefore(Context handler, String clazz, List<Document> data) {

        if (clazz == null) {
            return null;
        }

        try {
            Method method = Class.forName(clazz).getMethod("initialize", Context.class, List.class);
            return (List<Document>)method.invoke(method.getDeclaringClass().newInstance(), handler, data);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException e) {
            logger.debug("Did not find initialize method." + clazz);
        }

        return null;
    }

    public void fetchLinkData(Context handler, ModEtl.Mappings mapping){

    }
}
