package com.example.yoyo_data.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PlantVO {
    private Integer result_num;
    private List<Plant> result;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class Plant{
        private String name;
        private BigDecimal score;
    }

}
