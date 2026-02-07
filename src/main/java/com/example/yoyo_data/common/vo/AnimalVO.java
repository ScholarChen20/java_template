package com.example.yoyo_data.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnimalVO {
    private List<Animal> result;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class Animal{
        private String name;
        private BigDecimal score;
    }
}
