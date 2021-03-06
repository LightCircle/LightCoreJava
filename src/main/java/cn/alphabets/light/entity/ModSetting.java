package cn.alphabets.light.entity;

import cn.alphabets.light.model.ModCommon;
import java.lang.String;

/**
 * Generated by the Light platform. Do not manually modify the code.
 */
public class ModSetting extends ModCommon {
  private String key;

  private String value;

  private String description;

  private String type;

  public String getKey() {
    return this.key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return this.value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getType() {
    return this.type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
