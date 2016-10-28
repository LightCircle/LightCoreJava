/**
 * Created by luohao on 16/9/5.
 */

'use strict';

var rider      = light.model.rider
  , async      = light.util.async
  , _          = light.util.underscore
  , security   = light.model.security
  , log        = light.framework.log
  , error      = light.framework.error
  , moment     = light.util.moment
  , constants  = require('./constants')
  , enterprise = require("./enterprise")
  , request    = require("request")
  , config     = light.framework.config
  , context    = light.framework.context
  ;


exports.tick = function (handler, callback) {

  var hour = moment().get('hour');


  if (!(hour >= 9 && hour <= 22)) {
    return callback(null, "unvalid time");
  }
  console.log('===>>>>>>>>>>', 'task in');
  if (!handler.uid) {
    handler = new context().create("000000000000000000000001", process.env.APPNAME);
  }

  async.waterfall([
    /**
     * 获取所有要处理的提醒
     * @param next
     */
      function (next) {
      rider.alert.list(handler, {
        free: {
          valid    : 1,
          to       : 0,
          alertDate: {
            $lte: moment().endOf('day').toDate(),
            $gte: moment().startOf('day').toDate()
          }
        }
      }, next);
    },
    /**
     * 获取所有支行主管账户
     * @param alerts
     * @param next
     */
      function (alerts, next) {
      rider.chief.list(handler, {skip: 0, limit: Number.MAX_VALUE}, function (err, admins) {
        if (err) {
          next(err);
        } else {
          next(err, alerts, admins.items);
        }
      });
    },
    /**
     * 构造发送数据结构
     * @param alerts
     * @param admin
     * @param next
     */
      function (alerts, admin, next) {
      //客户经理推送数据
      var clerkData = [];

      //支行主管推送数据
      var adminData = [];

      async.eachSeries(alerts.items, function (item, cb) {
        rider.enterprise.get(handler, {id: item.enterprise}, function (err, result) {
          console.log(result);
          if (err) {
            return cb(null);
          }
          var managers = result.managers;
          //添加客户经理数据
          _.each(managers, function (m) {
            clerkData.push({
              to   : m,
              nid  : m + item.type,
              type : item.type,
              eid  : item.enterprise,
              ename: result.name,
              phone: result.options.clerk[m].phone,
              uname: result.options.clerk[m].name
            });
          });
          //添加主管数据
          _.each(_.filter(admin, function (a) {
            return _.contains(a.groups, result.subbranch);
          }), function (a) {
            adminData.push({
              to   : a._id.toString(),
              nid  : a._id.toString() + item.type,
              type : item.type,
              eid  : item.enterprise,
              ename: result.name,
              phone: a.phone,
              uname: a.name
            });
          });

          return cb();
        });
        //push
        //sms
      }, function (err) {
        next(err, {clerkData: clerkData, adminData: adminData}, alerts);
      });
    },
    /**
     * 同一个人的消息进行合并,并构造发送内容
     * @param notifyData
     * @param next
     */
      function (notifyData, alerts, next) {
      var clerkDataGrouped = _.groupBy(notifyData.clerkData, 'nid');
      clerkDataGrouped = _.map(_.map(clerkDataGrouped, function (v, k) {
        if (v.length == 1) {
          return v[0];
        } else {
          var base = v[0];
          var enames = _.pluck(v, 'ename').join("，");
          base.ename = enames;
          return base;
        }
      }), function (item) {

        var sms
          , pushTitle
          , pushContent;
        if (item.type == constants.alertType.expiration) {
          sms = "【贷后检查】授信企业 " + item.ename + " 授信将于7天后到期。";
          pushTitle = "授信到期提醒";
          pushContent = "授信企业 " + item.ename + " 授信将于7天后到期。";
        } else if (item.type == constants.alertType.checkPoint) {
          sms = "【贷后检查】授信企业 " + item.ename + " 需要检查，请使用APP进行相关操作。";
          pushTitle = "检查到期提醒";
          pushContent = "授信企业 " + item.ename + " 需要检查，请打开APP进行相关操作。";
        } else if (item.type == constants.alertType.xyzl) {
          sms = "【贷后检查】授信企业 " + item.ename + " 信用总量将于60天后到期。";
          pushTitle = "信用总量到期提醒";
          pushContent = "授信企业 " + item.ename + " 信用总量将于60天后到期。";
        } else if (item.type == constants.alertType.dywbx) {
          sms = "【贷后检查】授信企业 " + item.ename + " 抵押物保险将于7天后到期。";
          pushTitle = "抵押物保险到期提醒";
          pushContent = "授信企业 " + item.ename + " 抵押物保险将于7天后到期。";
        }

        item.sms = sms;
        item.pushTitle = pushTitle;
        item.pushContent = pushContent;


        return item;
      });


      var adminDataGrouped = _.groupBy(notifyData.adminData, 'nid');
      adminDataGrouped = _.map(_.map(adminDataGrouped, function (v, k) {
        if (v.length == 1) {
          return v[0];
        } else {
          var base = v[0];
          var enames = _.pluck(v, 'ename').join("，");
          base.ename = enames;
          return base;
        }
      }), function (item) {

        var sms
          , pushTitle
          , pushContent;
        if (item.type == constants.alertType.expiration) {
          sms = "【贷后检查】授信企业 " + item.ename + " 授信将于7天后到期。";
          pushTitle = "授信到期提醒";
          pushContent = "授信企业 " + item.ename + " 授信将于7天后到期。";
        } else if (item.type == constants.alertType.checkPoint) {
          sms = "【贷后检查】授信企业 " + item.ename + " 需要检查。";
          pushTitle = "检查到期提醒";
          pushContent = "授信企业 " + item.ename + " 需要检查。";
        } else if (item.type == constants.alertType.xyzl) {
          sms = "【贷后检查】授信企业 " + item.ename + " 信用总量将于60天后到期。";
          pushTitle = "信用总量到期提醒";
          pushContent = "授信企业 " + item.ename + " 信用总量将于60天后到期。";
        } else if (item.type == constants.alertType.dywbx) {
          sms = "【贷后检查】授信企业 " + item.ename + " 抵押物保险将于7天后到期。";
          pushTitle = "抵押物保险到期提醒";
          pushContent = "授信企业 " + item.ename + " 抵押物保险将于7天后到期。";
        }

        item.sms = sms;
        item.pushTitle = pushTitle;
        item.pushContent = pushContent;


        return item;
      });
      next(null, {clerkData: clerkDataGrouped, adminData: adminDataGrouped}, alerts);
    },
    /**
     * 发送系统消息
     * @param notifyData
     * @param alerts
     * @param next
     */
      function (notifyData, alerts, next) {
      async.eachLimit(notifyData.clerkData.concat(notifyData.adminData), 5, function (item, cb) {
        rider.message.add(handler, {
          data: {
            clerk  : item.to,
            content: item.pushTitle + ":" + item.pushContent
          }
        }, function () {
          cb();
        })
      }, function () {
        next(null, notifyData, alerts);
      });
    },
    /**
     * 发送推送
     * @param notifyData
     * @param next
     */
      function (notifyData, alerts, next) {
      var allData = notifyData.clerkData.concat(notifyData.adminData);


      async.eachLimit(allData, 5, function (item, cb) {
        pushOneAlias(item.to, {
          title: item.pushTitle,
          text : item.pushContent
        }, function () {
          cb();
        });

      }, function () {
        next(null, allData, alerts);
      });


    },
    /**
     * 发送短信
     */
      function (notifyData, alerts, next) {

      async.eachLimit(notifyData, 5, function (item, cb) {
        sendSMS(item.phone, item.sms, function () {
          cb();
        });

      }, function () {
        next(null, notifyData, alerts);
      });

    },
    /**
     * 更改通知状态
     * @param notifyData
     * @param alerts
     * @param next
     */
      function (notifyData, alerts, next) {
      var aids = _.map(alerts.items, function (item) {
        return item._id.toString();
      });

      async.eachLimit(aids, 5, function (item, cb) {
        rider.alert.update(handler, {
          id  : item,
          data: {
            to: [1]
          }
        }, cb);
      }, function (err, result) {
        next();
      });

      //next(null, {data: notifyData, aids: aids});
    }
  ], callback);
};


function pushOneAlias(to, msg, callback) {
  log.info("SEND PUSH:" + to + "|" + msg.title + "|" + msg.text);
  console.log("SEND PUSH:" + to + "|" + msg.title + "|" + msg.text);
  if (process.env.DEV) {
    return callback();
  }

  request.post({
    url    : "https://leancloud.cn/1.1/push",
    headers: {
      'X-LC-Id' : config.push.LeanCloudAppID,
      'X-LC-Key': config.push.LeanCloudAppKey
    },
    body   : {
      channels           : [to],
      data               : {
        alert: msg.text,
        title: msg.title
      },
      expiration_interval: "604800"
    },
    json   : true
  }, function (err, httpResponse, body) {
    console.log(err, body);
    if (err) {
      log.error(err, body);
    }
    return callback(err, body);
  });
}

function sendSMS(to, msg, callback) {

  if (!to) {
    return callback();
  }
  log.info("SEND SMS:" + to + "|" + msg);
  console.log("SEND SMS:" + to + "|" + msg);

  if (process.env.DEV) {
    return callback();
  }

  request.post({
    url : 'https://sms.yunpian.com/v2/sms/single_send.json',
    form: {
      apikey: config.push.yunpianApiKey,
      mobile: to,
      text  : msg
    }
  }, function (err, httpResponse, body) {
    console.log(err, body);
    return callback();
  });

}