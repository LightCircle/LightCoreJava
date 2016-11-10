package cn.alphabets.light.model;

import org.bson.Document;

/**
 * Json
 */
public class Json extends Document {
    public Json() {
        super();
    }

    public Json(String key, Object value) {
        super(key, value);
    }
}
