package com.example.yoyo_data.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class VegetableVO {
    private Integer result_num;
    private List<Vegetable> result;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class Vegetable{
        private String name;
        private BigDecimal score;
    }

}
