
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

#### memo
/**
 * 执行以下替换操作：
 * ObjectId("57c52f87fb35fd050073f9c4") -> "57c52f87fb35fd050073f9c4"
 * ISODate("2016-08-30T07:02:31.391Z") -> "2016-08-30T07:02:31.391Z"
 */

json = json.replaceAll("ObjectId\\((\\\"\\w{24}\\\")\\)|ISODate\\((\\\"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}Z\\\")\\)", "$1$2");
res().putHeader(CONTENT_TYPE, "application/json").end(json);

#### Maven Central Repository公开lightcore-java
- 生成公钥秘钥对（有时时间会很久，20分钟？）
  # gpg --gen-key

- 查看生成的秘钥对
  # gpg --list-keys

- 上传公钥到指定的验证服务器，DA34A5F为生成的秘钥对名称
  # gpg2 --keyserver hkp://pool.sks-keyservers.net --send-keys DA34A5F

- 申请Sonatype账号，并提交issue，需要指定groupId

- 设定上传Jar用的账号，使用在上一步设定的账户
  # vi /root/.m2/settings.xml
    <settings>
      <servers>
        <server>
          <id>light-core</id>
          <username>username</username>
          <password>password</password>
        </server>
      </servers>
    </settings>

- 编译，上传
  # mvn deploy -Dgpg.passphrase=password

- 登陆到sonatype，依次Close -> Release
  https://oss.sonatype.org/
  需要一个小时，才能反映到Repository
  
- 编译命令行中 指定版本
  mvn -Dmaven.compiler.source=1.8 -Dmaven.compiler.target=1.8 package -Djar.finalName=app
