//package cn.alphabets.light.db.mongo;
//
//import cn.alphabets.light.Constant;
//import cn.alphabets.light.entity.ModFile;
//import cn.alphabets.light.http.Context;
//import cn.alphabets.light.model.Entity;
//import cn.alphabets.light.model.ModCommon;
//import cn.alphabets.light.model.Plural;
//import org.bson.Document;
//import org.bson.types.ObjectId;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.stream.Collectors;
//
///**
// * Controller
// */
//public class Controller {
//
//    private Model model;
//    private Context.Params params;
//    private String uid;
//
//    public Controller(Context handler) {
//        this.model = new Model(handler.getDomain(), handler.getCode());
//        this.params = handler.params;
//        this.uid = handler.uid();
//    }
//
//    public Controller(Context handler, Class table) {
//        this(handler, table.getSimpleName().toLowerCase());
//    }
//
//    public Controller(Context handler, String table) {
//        this.model = new Model(handler.getDomain(), handler.getCode(), table);
//        this.params = handler.params;
//        this.uid = handler.uid();
//    }
//
//    public <T extends ModCommon> Plural<T> list() {
//
//        if (!this.params.getCondition().containsKey("valid")) {
//            this.params.getCondition().put("valid", Constant.VALID);
//        }
//
//        List<T> items = this.model.list(this.params.getCondition(),
//                this.params.getSelect(),
//                this.params.getSort(),
//                this.params.getSkip(),
//                this.params.getLimit());
//
//        return new Plural<>(this.count(), items);
//    }
//
//    /**
//     * add document
//     *
//     * @param <T> ModBase
//     * @return document
//     */
//    public <T extends ModCommon> List<T> add() {
//
//        List<Object> data;
//        Object object = this.params.getData();
//
//        // Transforms a single object into a list
//        if (object instanceof List) {
//            data = (List) object;
//        } else {
//            data = new ArrayList<>();
//            data.add(object);
//        }
//
//        List<Document> documents = data.stream().map((x) -> {
//
//            Document document;
//
//            // If ModBase, need to be converted to Document
//            if (x instanceof Entity) {
//                document = ((Entity) x).toDocument();
//            } else {
//                document = (Document) x;
//            }
//
//            document.put("createAt", new Date());
//            document.put("updateAt", new Date());
//            document.put("createBy", this.uid);
//            document.put("updateBy", this.uid);
//            document.put("valid", Constant.VALID);
//            document.remove("_id");
//            return document;
//        }).collect(Collectors.toList());
//
//        return this.model.add(documents);
//    }
//
//    public Long count() {
//        if (!this.params.getCondition().containsKey("valid")) {
//            this.params.getCondition().put("valid", Constant.VALID);
//        }
//
//        return this.model.count(this.params.getCondition());
//    }
//
//    public <T extends ModCommon> T get() {
//
//        Document condition = new Document();
//
//        if (this.params.getId() != null) {
//            Object id = this.params.getId();
//            condition.put("_id", (id instanceof String) ? new ObjectId((String) id) : id);
//        } else {
//            condition.putAll(this.params.getCondition());
//        }
//
//        if (!this.params.getCondition().containsKey("valid")) {
//            condition.put("valid", Constant.VALID);
//        }
//        return this.model.get(condition);
//    }
//
//
//    public Long delete() {
//
//        Document condition = new Document();
//
//        if (this.params.getId() != null) {
//            Object id = this.params.getId();
//            condition.put("_id", (id instanceof String) ? new ObjectId((String) id) : id);
//        } else {
//            condition.putAll(this.params.getCondition());
//        }
//
//        assert condition.size() > 0 : "The delete condition can not be null.";
//        return this.model.delete(condition);
//    }
//
//    public Long remove() {
//
//        Document condition = new Document();
//
//        if (this.params.getId() != null) {
//            Object id = this.params.getId();
//            condition.put("_id", (id instanceof String) ? new ObjectId((String) id) : id);
//        } else {
//            condition.putAll(this.params.getCondition());
//        }
//
//        if (!this.params.getCondition().containsKey("valid")) {
//            condition.put("valid", Constant.VALID);
//        }
//
//        assert condition.size() > 0 : "The remove condition can not be null";
//        return this.model.remove(condition);
//    }
//
//    public Plural<ModFile> writeFileToGrid() {
//
//        List<ModFile> files = this.params.getFiles().stream().map((document) ->
//                this.model.writeFileToGrid(document)
//        ).collect(Collectors.toList());
//
//        return new Plural<>((long) files.size(), files);
//    }
//
//    public ModFile readStreamFromGrid() {
//        return this.model.readStreamFromGrid((ObjectId) this.params.getId(), this.params.getStream());
//    }
//
//    public void deleteFromGrid() {
//        this.model.deleteFromGrid((ObjectId) this.params.getId());
//    }
//}
