package com.example.yoyo_data.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralImageVO {
    private List<GeneralImage> result;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class GeneralImage{
        private String score;
        private String root;
        private String keyword;
    }
}
//{
//  "result": [
//        {
//        "score": 0.675859,
//        "root": "非自然图像-彩色动漫",
//        "keyword": "卡通动漫人物"
//        }]
// }