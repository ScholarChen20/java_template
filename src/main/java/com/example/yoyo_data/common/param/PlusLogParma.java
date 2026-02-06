package com.example.yoyo_data.common.param;

import com.example.yoyo_data.common.enums.LogType;
import lombok.Data;

@Data
public class PlusLogParma {

    private String mediaType; //媒体平台

    private String url; //请求地址

    private String req; //请求参数

    private String resp; //返回参数

    private Long timestamp; //耗时

    private Integer type; //标识： 1:请求媒体的日志 2:业务流程的日志

    private String accountId; //任务ID

    private Integer userId; //创建用户id

    private String batchId; //'批次id'

    private Integer status; //'推送状态 1.成功 0.失败'

    private String remark; //说明

    /**
     * 日志业务类型
     */
    private LogType logType;

    private String ip;

    /**
     * 业务id
     */
    private String id;

}
