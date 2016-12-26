package cn.alphabets.light.http;

import org.bson.Document;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by luohao on 2016/12/26.
 */
public class Params {
    private Document json;
    private List<RequestFile> files;

    public Params(Document json) {
        this(json, null);
    }

    public Params(Document json, List<RequestFile> files) {
        this.json = json;
        this.files = files;
    }

    public String getString(String key) {
        return this.json.getString(key);
    }

    public void set(String key, Object val) {
        this.json.put(key, val);
    }

    public List<RequestFile> getFiles() {
        return files;
    }


    public Document getJson() {
        return json;
    }

    @Override
    public String toString() {
        return "\n{" +
                "\n\tparams = " + json.toJson() +
                "\n\tfiles = " + (files == null ? "null" : "\n\t\t" + files.stream().map(file -> file.toString()).collect(Collectors.joining("\n\t\t"))) +
                "\n}";
    }

}
