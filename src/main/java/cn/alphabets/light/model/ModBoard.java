package cn.alphabets.light.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * Created by luohao on 16/10/22.
 */
public class ModBoard {

    private String _id;
    private int valid;
    private Date createAt;
    private String createBy;
    private Date updateAt;
    private String updateBy;
    private String schema;
    private String api;
    private int type;
    private int kind;
    private String path;
    @JsonProperty("class")
    private String class_;
    private String action;
    private String description;
    private String reserved;

    public String get_id() {
        return _id;
    }

    public int getValid() {
        return valid;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public String getCreateBy() {
        return createBy;
    }

    public Date getUpdateAt() {
        return updateAt;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public String getSchema() {
        return schema;
    }

    public String getApi() {
        return api;
    }

    public int getType() {
        return type;
    }

    public int getKind() {
        return kind;
    }

    public String getPath() {
        return path;
    }

    public String getClass_() {
        return class_;
    }

    public String getAction() {
        return action;
    }

    public String getDescription() {
        return description;
    }

    public String getReserved() {
        return reserved;
    }
}
