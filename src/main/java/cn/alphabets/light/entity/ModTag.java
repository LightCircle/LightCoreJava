package cn.alphabets.light.entity;

import cn.alphabets.light.model.ModBase;
import java.lang.Long;
import java.lang.String;

/**
 * Generated by the Light platform. Do not manually modify the code.
 */
public class ModTag extends ModBase {
  private String name;

  private String type;

  private Long counter;

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return this.type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Long getCounter() {
    return this.counter;
  }

  public void setCounter(Long counter) {
    this.counter = counter;
  }
}