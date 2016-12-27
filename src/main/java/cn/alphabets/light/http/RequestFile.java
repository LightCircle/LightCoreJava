package cn.alphabets.light.http;

import cn.alphabets.light.Constant;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.bson.Document;

import java.util.Map;

/**
 * Created by luohao on 2016/12/26.
 */
public class RequestFile extends Document {
    public RequestFile() {
    }

    public RequestFile(String key, Object value) {
        super(key, value);
    }

    public RequestFile(String filePath, String contentType, String fileName, Boolean keep) {
        this.put(Constant.PARAM_FILE_PHYSICAL, filePath);
        this.put(Constant.PARAM_FILE_TYPE, contentType);
        this.put(Constant.PARAM_FILE_NAME, fileName);
        this.put(Constant.PARAM_FILE_KEEP, keep);
    }

    public RequestFile(Map<String, Object> map) {
        super(map);
    }

    public String getFilePath() {
        return this.getString(Constant.PARAM_FILE_PHYSICAL);
    }

    public String getContentType() {
        return this.getString(Constant.PARAM_FILE_TYPE);
    }

    public String getFileName() {
        return this.getString(Constant.PARAM_FILE_NAME);
    }

    public Boolean isKeep() {
        return this.getBoolean(Constant.PARAM_FILE_KEEP, false);
    }
}
