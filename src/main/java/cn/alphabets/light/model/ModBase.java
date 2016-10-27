package cn.alphabets.light.model;

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

import java.io.IOException;
import java.util.Date;

/**
 * Created by luohao on 2016/10/27.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModBase {
    @JsonIgnore
    static final Logger logger = LoggerFactory.getLogger(ModBase.class);
    @JsonIgnore
    private static ObjectMapper objectMapper = new ObjectMapper();

    @JsonSerialize(using = ObjectIdSerializer.class)
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private ObjectId _id;

    @JsonSerialize(using = DateSerializer.class)
    @JsonDeserialize(using = DateDeserializer.class)
    private Date createAt;

    @JsonSerialize(using = DateSerializer.class)
    @JsonDeserialize(using = DateDeserializer.class)
    private Date updateAt;


    private int valid;
    private String createBy;
    private String updateBy;


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

    public static <T> T fromDoc(Document doc, Class<T> clz) {
        try {
            return objectMapper.readValue(doc.toJson(), clz);
        } catch (IOException e) {
            logger.error(e);
        }
        return null;
    }

    public static Document toDoc(ModBase obj) {
        try {
            return Document.parse(objectMapper.writeValueAsString(obj));
        } catch (JsonProcessingException e) {
            logger.error(e);
        }
        return null;
    }
}
