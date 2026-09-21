package com.example.demo.entity.item;

import lombok.Getter;
import lombok.Setter;

@Getter@Setter
//전자기기의 비핵심 정보
public class ElectronicsDetail extends ItemDetailSpec {

    private String energyRating;
}
