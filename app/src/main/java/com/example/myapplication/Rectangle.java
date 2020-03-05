package com.example.myapplication;

import com.example.annotation.Factory;

@Factory(id = "Rectangle", type = IShape.class)
public class Rectangle implements IShape {
    @Override
    public String draw() {
        return "Draw a Rectangle";
    }
}
