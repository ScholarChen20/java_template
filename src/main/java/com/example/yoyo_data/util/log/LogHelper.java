package com.example.yoyo_data.util.log;

import com.alibaba.fastjson.JSON;
import com.aliyun.openservices.log.Client;
import com.aliyun.openservices.log.common.LogContent;
import com.aliyun.openservices.log.common.LogItem;
import com.aliyun.openservices.log.common.QueriedLog;
import com.aliyun.openservices.log.exception.LogException;
import com.aliyun.openservices.log.response.GetLogsResponse;
import com.aliyun.openservices.log.util.NetworkUtils;
import com.example.yoyo_data.common.constant.Environment;
import com.example.yoyo_data.common.param.PlusLogParma;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 日志助手类
 * 简化的日志记录工具
 */
//@Component
@Slf4j
public class LogHelper {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.profiles.active}")
    private String profilesActive;

    @Value("${sls-log.endpoint}")
    private static String host ;

    @Value("sls-log.accessId")
    private static String accessId;

    @Value("sls-log.accessKey")
    private static String accessKey;

    @Value("sls-log.projectName")
    private static String projectName = "aliyun-test-gs-project";

    @Value("sls-log.logstoreName")
    private static String logstoreName = "aliyun-test-logstore";

    private static String query = "*| select * from " + logstoreName;

    private static Client client = new Client(host, accessId, accessKey);

    /**
     * 打印业务日志
     *
     * @param data 日志数据
     */
    public void printLog(Object data) {
        log.debug("业务日志：[{}]", JSON.toJSONString(data));

    }


    public void printSLSLog(PlusLogParma plusLogParma) {
        List<LogItem> logGroup = new ArrayList<LogItem>();
        //获取本机ip
        plusLogParma.setIp(NetworkUtils.getLocalMachineIP());
        log.debug("业务日志：[{}]", JSON.toJSONString(plusLogParma));
        if (StringUtils.equals(Environment.ENVIRONMENT_DEV, profilesActive)) {
            return;
        }

        LogItem logItem = new LogItem();
        logItem.PushBack("mediaType", plusLogParma.getMediaType());
        logItem.PushBack("url", plusLogParma.getUrl());
        logItem.PushBack("req", plusLogParma.getReq());
        logItem.PushBack("resp", plusLogParma.getResp());
        logItem.PushBack("timestamp", String.valueOf(plusLogParma.getTimestamp()));
        logItem.PushBack("type", String.valueOf(plusLogParma.getType()));
        logItem.PushBack("accountId", plusLogParma.getAccountId());
        logItem.PushBack("userId", String.valueOf(plusLogParma.getUserId()));
        logItem.PushBack("batchId", plusLogParma.getBatchId());
        logItem.PushBack("status", String.valueOf(plusLogParma.getStatus()));
        logItem.PushBack("remark", plusLogParma.getRemark());
        logItem.PushBack("logBizType", String.valueOf(plusLogParma.getLogType()));
        logItem.PushBack("ip", plusLogParma.getIp());
        logItem.PushBack("id", plusLogParma.getId());

        try {
            logGroup.add(logItem);
            client.PutLogs(projectName, logstoreName, "", logGroup, "");
            System.out.println(String.format("push logs for %s success", logstoreName));
        } catch (Exception e) {
            log.error("[打印SLS日志异常] store:[{}]", applicationName, e);
        }
    }

    /**
     * 通过SQL查询日志。
     *
     * @throws LogException
     */
    static void queryLogs() throws LogException {
        System.out.println(String.format("ready to query logs from %s", logstoreName));
        // fromTime和toTime表示查询日志的时间范围，Unix时间戳格式。
        int fromTime = (int) (System.currentTimeMillis() / 1000 - 3600);
        int toTime = fromTime + 3600;
        GetLogsResponse getLogsResponse = client.GetLogs(projectName, logstoreName, fromTime, toTime, "", query);
        for (QueriedLog log : getLogsResponse.getLogs()) {
            for (LogContent mContent : log.mLogItem.mContents) {
                System.out.println(mContent.mKey + " : " + mContent.mValue);
            }
            System.out.println("********************");
        }
    }


}