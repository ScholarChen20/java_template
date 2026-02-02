package com.example.yoyo_data.utils;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 日志助手类
 * 简化的日志记录工具
 */
@Component
@Slf4j
public class LogHelper {

    /**
     * 打印业务日志
     *
     * @param data 日志数据
     */
    public void printLog(Object data) {
        log.debug("业务日志：[{}]", JSON.toJSONString(data));
    }

}