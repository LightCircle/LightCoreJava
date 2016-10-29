package cn.alphabets.light.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by luohao on 16/10/22.
 */
public class ModBoard extends ModBase {

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
    private List<Filter> filters;
    private List<Sort> sorts;
    private List<Select> selects;


    public void setSchema(String schema) {
        this.schema = schema;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setKind(int kind) {
        this.kind = kind;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setClass_(String class_) {
        this.class_ = class_;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setReserved(String reserved) {
        this.reserved = reserved;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    public List<Sort> getSorts() {
        return sorts;
    }

    public void setSorts(List<Sort> sorts) {
        this.sorts = sorts;
    }

    public List<Select> getSelects() {
        return selects;
    }

    public void setSelects(List<Select> selects) {
        this.selects = selects;
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Filter {
        @JsonProperty("default")
        private String default_;
        private String key;
        private String operator;
        private String parameter;
        private String group;

        public String getDefault_() {
            return default_;
        }

        public void setDefault_(String default_) {
            this.default_ = default_;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }

        public String getParameter() {
            return parameter;
        }

        public void setParameter(String parameter) {
            this.parameter = parameter;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sort {
        private String key;
        private String order;
        private int index;
        private String dynamic;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getOrder() {
            return order;
        }

        public void setOrder(String order) {
            this.order = order;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public String getDynamic() {
            return dynamic;
        }

        public void setDynamic(String dynamic) {
            this.dynamic = dynamic;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Select {
        private String key;
        private boolean select;
        private String format;
        private String alias;
        private String option;
        private List<String> fields;
        private String link;
        private int reserved;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public boolean isSelect() {
            return select;
        }

        public void setSelect(boolean select) {
            this.select = select;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public String getOption() {
            return option;
        }

        public void setOption(String option) {
            this.option = option;
        }

        public List<String> getFields() {
            return fields;
        }

        public void setFields(List<String> fields) {
            this.fields = fields;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public int getReserved() {
            return reserved;
        }

        public void setReserved(int reserved) {
            this.reserved = reserved;
        }
    }


}
