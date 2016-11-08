package cn.alphabets.light.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by luohao on 2016/10/29.
 */
public class ModStructure extends ModBase {
    private int type;
    private String schema;
    private int kind;
    private String description;
    @JsonProperty("public")
    private String public_;
    private String version;
    private HashMap<String, Field> items;
    private String tenant;
    private int lock;
    private List<Map<String, Object>> extend;

    public ModStructure() {
        super();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public int getKind() {
        return kind;
    }

    public void setKind(int kind) {
        this.kind = kind;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublic_() {
        return public_;
    }

    public void setPublic_(String public_) {
        this.public_ = public_;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public HashMap<String, Field> getItems() {
        return items;
    }

    public void setItems(HashMap<String, Field> items) {
        this.items = items;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public int getLock() {
        return lock;
    }

    public void setLock(int lock) {
        this.lock = lock;
    }

    public List<Map<String, Object>> getExtend() {
        return extend;
    }

    public void setExtend(List<Map<String, Object>> extend) {
        this.extend = extend;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Field {
        private int reserved;
        private String type;
        private String name;
        private String description;


        public int getReserved() {
            return reserved;
        }

        public void setReserved(int reserved) {
            this.reserved = reserved;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

}
