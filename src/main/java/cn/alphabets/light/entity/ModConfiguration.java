package cn.alphabets.light.entity;

import cn.alphabets.light.model.ModCommon;
import cn.alphabets.light.model.deserializer.LongDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

/**
 * Generated by the Light platform. Do not manually modify the code.
 */
public class ModConfiguration extends ModCommon {
  private String description;

  @JsonDeserialize(
      using = LongDeserializer.class
  )
  private Long displayType;

  private List options;

  private String type;

  private String key;

  private Object value;

  private String valueType;

  public String getDescription() {
    return this.description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Long getDisplayType() {
    return this.displayType;
  }

  public void setDisplayType(Long displayType) {
    this.displayType = displayType;
  }

  public List getOptions() {
    return this.options;
  }

  public void setOptions(List options) {
    this.options = options;
  }

  public String getType() {
    return this.type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getKey() {
    return this.key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public Object getValue() {
    return this.value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public String getValueType() {
    return this.valueType;
  }

  public void setValueType(String valueType) {
    this.valueType = valueType;
  }
}
