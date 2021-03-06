package cn.alphabets.light.entity;

import cn.alphabets.light.model.ModCommon;
import java.lang.Object;
import java.lang.String;
import java.util.List;

/**
 * Generated by the Light platform. Do not manually modify the code.
 */
public class ModUser extends ModCommon {
  private String status;

  private String timezone;

  private String password;

  private List roles;

  private String email;

  private Object extend;

  private String id;

  private String type;

  private List groups;

  private String lang;

  private String name;

  private String outer;

  public String getStatus() {
    return this.status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getTimezone() {
    return this.timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  public String getPassword() {
    return this.password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public List getRoles() {
    return this.roles;
  }

  public void setRoles(List roles) {
    this.roles = roles;
  }

  public String getEmail() {
    return this.email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Object getExtend() {
    return this.extend;
  }

  public void setExtend(Object extend) {
    this.extend = extend;
  }

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getType() {
    return this.type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public List getGroups() {
    return this.groups;
  }

  public void setGroups(List groups) {
    this.groups = groups;
  }

  public String getLang() {
    return this.lang;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getOuter() {
    return this.outer;
  }

  public void setOuter(String outer) {
    this.outer = outer;
  }
}
