package cn.alphabets.light.db.mongo;

import java.util.List;

/**
 * Controller
 */
public class Controller {

    private String table;


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
