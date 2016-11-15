
#### 如何对应动态变化的Structure
 - 使用JsonObject，用字符串的方式去访问，需要繁琐的类型转换
 - 启动时，代码动态生成Structure类

#### 依赖的第三方包

- vertx-web : web framework

- mongo : mongo driver

- commons-jcs : Java Caching System
  http://commons.apache.org/proper/commons-jcs/

- SnakeYAML : yaml处理

- JavaPoet : Java代码生成
  https://github.com/square/javapoet

- 模板引擎
  http://jtwig.org/

----
/**
 * 执行以下替换操作：
 * ObjectId("57c52f87fb35fd050073f9c4") -> "57c52f87fb35fd050073f9c4"
 * ISODate("2016-08-30T07:02:31.391Z") -> "2016-08-30T07:02:31.391Z"
 */

json = json.replaceAll("ObjectId\\((\\\"\\w{24}\\\")\\)|ISODate\\((\\\"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}Z\\\")\\)", "$1$2");
res().putHeader(CONTENT_TYPE, "application/json").end(json);

