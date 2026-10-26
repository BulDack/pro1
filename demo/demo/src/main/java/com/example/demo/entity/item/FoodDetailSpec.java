package com.example.demo.entity.item;


import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class FoodDetailSpec extends ItemDetailSpec{

    private String calories;
    private boolean spicy;
}
