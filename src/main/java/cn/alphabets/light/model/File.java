package cn.alphabets.light.model;

import cn.alphabets.light.Constant;
import cn.alphabets.light.db.mongo.Controller;
import cn.alphabets.light.entity.ModFile;
import cn.alphabets.light.http.Context;

import java.util.List;

/**
 * File
 * Created by lilin on 2016/11/12.
 */
public class File {

    /**
     * Add one or more files
     *
     * @param handler context
     * @return meta
     */
    public Plural<ModFile> add(Context handler) {

        Plural<ModFile> files = new Controller(handler).writeFileToGrid();

        handler.params.setDataList(files.getItems());
        List<ModFile> result = new Controller(handler, Constant.SYSTEM_DB_FILE).add();

        return new Plural<>((long) result.size(), result);
    }

    /**
     * Physically delete the file, and also delete the GridFS file
     *
     * @param handler context
     */
    public void delete(Context handler) {

        assert handler.params.getId() != null : "Id can not be empty.";

        Controller ctrl = new Controller(handler, Constant.SYSTEM_DB_FILE);

        ModFile file = ctrl.get();
        if (file == null) {
            return;
        }

        ctrl.delete();

        handler.params.setId(file.getFileId());
        ctrl.deleteFromGrid();
    }

    public Plural<ModFile> image(Context handler) {

        Controller ctrl = new Controller(handler, Constant.SYSTEM_DB_FILE);
        ModFile file = ctrl.get();

        handler.params.setId(file.getFileId());
        return ctrl.readStreamFromGrid();
    }

}
