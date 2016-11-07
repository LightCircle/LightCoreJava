package cn.alphabets.light.db.mongo;

import cn.alphabets.light.Config;
import cn.alphabets.light.model.ModBase;
import com.mongodb.Block;
import com.mongodb.client.model.Projections;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller
 */
public class Controller {

    private String table;
    private Model model;


    public Controller(String table) {

//        this.model = new Model();

        this.table = table;
    }

    public <T> List<T> list(Class clazz) {

//        List<T> result = new ArrayList<>();
//        this.connection.getCollection(this.table)
//                .find(Document.parse("{valid:1}"))
//                .projection(Projections.exclude("createAt", "updateAt", "valid", "createBy", "updateBy"))
//                .forEach((Block<? super Document>) document -> {
//                    result.add((T) ModBase.fromDoc(document, clazz.getClass()));
//                });
//
//        return result;
        return null;
    }
}
