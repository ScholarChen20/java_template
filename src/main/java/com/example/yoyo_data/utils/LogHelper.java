//package com.example.yoyo_data.log;
//
//import com.alibaba.fastjson.JSON;
//import com.aliyun.openservices.log.common.LogItem;
//import com.yoyo.cloud.marketing.common.constant.Constant;
//import com.yoyo.cloud.marketing.common.param.PlusLogParma;
//import com.yoyo.cloud.marketing.common.util.NetworkUtils;
//import com.yoyo.cloud.sls.log.starter.service.AliYunLogService;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//@Component
//@Slf4j
//public class LogHelper {
//
//    @Autowired
//    private AliYunLogService aliYunLogService;
//
//    @Value("${spring.application.name}")
//    private String applicationName;
//
//    @Value("${spring.profiles.active}")
//    private String profilesActive;
//
//    public void printLog(PlusLogParma plusLogParma) {
//        //获取本机ip
//        plusLogParma.setIp(NetworkUtils.getHostAddress());
//        log.debug("业务日志：[{}]", JSON.toJSONString(plusLogParma));
//        if (StringUtils.equals(Constant.ENVIRONMENT_DEV, profilesActive)) {
//            return;
//        }
//
//        LogItem logItem = new LogItem();
//        logItem.PushBack("mediaType", plusLogParma.getMediaType());
//        logItem.PushBack("url", plusLogParma.getUrl());
//        logItem.PushBack("req", plusLogParma.getReq());
//        logItem.PushBack("resp", plusLogParma.getResp());
//        logItem.PushBack("timestamp", String.valueOf(plusLogParma.getTimestamp()));
//        logItem.PushBack("type", String.valueOf(plusLogParma.getType()));
//        logItem.PushBack("accountId", plusLogParma.getAccountId());
//        logItem.PushBack("userId", String.valueOf(plusLogParma.getUserId()));
//        logItem.PushBack("batchId", plusLogParma.getBatchId());
//        logItem.PushBack("status", String.valueOf(plusLogParma.getStatus()));
//        logItem.PushBack("remark", plusLogParma.getRemark());
//        logItem.PushBack("logBizType", String.valueOf(plusLogParma.getLogBizType()));
//        logItem.PushBack("ip", plusLogParma.getIp());
//        logItem.PushBack("id", plusLogParma.getId());
//
//        try {
//            aliYunLogService.send(applicationName, logItem, result -> {
//                if (!result.isSuccessful()) {
//                    log.error("[打印SLS日志失败] store:[{}] 发送日志失败，error:{}", applicationName, result.getErrorMessage());
//                }
//            });
//        } catch (Exception e) {
//            log.error("[打印SLS日志异常] store:[{}]", applicationName, e);
//        }
//    }
//
//}
