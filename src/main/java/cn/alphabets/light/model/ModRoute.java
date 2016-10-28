package cn.alphabets.light.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by luohao on 16/10/22.
 */
public class ModRoute extends ModBase {

    private String url;
    @JsonProperty("class")
    private String class_;
    private String action;
    private String template;
    private String description;


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getClass_() {
        return class_;
    }

    public void setClass_(String class_) {
        this.class_ = class_;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
