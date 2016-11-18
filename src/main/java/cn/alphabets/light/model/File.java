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

    public Plural<ModFile> add(Context handler) {

        List<ModFile> files = new Controller(handler).writeFileToGrid();

        handler.params.setDataList(files);
        List<ModFile> result = new Controller(handler, Constant.SYSTEM_DB_FILE).add();

        return new Plural<>((long) result.size(), result);
    }
}
