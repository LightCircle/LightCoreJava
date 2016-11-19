package cn.alphabets.light.model;

import cn.alphabets.light.model.deserializer.DateDeserializer;
import cn.alphabets.light.model.deserializer.ObjectIdDeserializer;
import cn.alphabets.light.model.serializer.DateSerializer;
import cn.alphabets.light.model.serializer.ObjectIdSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * ModBase
 * Created by luohao on 2016/10/27.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModBase extends Model {

    @JsonIgnore
    private static final Logger logger = LoggerFactory.getLogger(ModBase.class);

    @JsonIgnore
    private static ObjectMapper objectMapper = new ObjectMapper();

    private int valid;
    private String createBy;
    private String updateBy;

    @JsonSerialize(using = ObjectIdSerializer.class)
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private ObjectId _id;

    @JsonSerialize(using = DateSerializer.class)
    @JsonDeserialize(using = DateDeserializer.class)
    private Date createAt;

    @JsonSerialize(using = DateSerializer.class)
    @JsonDeserialize(using = DateDeserializer.class)
    private Date updateAt;

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public Date getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Date updateAt) {
        this.updateAt = updateAt;
    }

    public int getValid() {
        return valid;
    }

    public void setValid(int valid) {
        this.valid = valid;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }


    public static <T> T fromJson(String json, Class<T> clz) {
        try {
            return objectMapper.readValue(json, clz);
        } catch (IOException e) {
            logger.error("error fromJson", e);
        }
        return null;
    }

    public String toJson() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            logger.error("error toJson", e);
        }
        return null;
    }

    public static <T extends ModBase> T fromDoc(Document doc, Class<T> clz) {
        if (doc == null) {
            return null;
        }

        try {
            return objectMapper.readValue(doc.toJson(), clz);
        } catch (IOException e) {
            logger.error("error fromDoc", e); // TODO: throw exception
        }
        return null;
    }

    public Document toDoc() {
        try {
            // TODO:自己写转换方法？
            return Document.parse(objectMapper.writeValueAsString(this));
        } catch (JsonProcessingException e) {
            logger.error("error fromDoc", e);
        }
        return null;
    }
}
