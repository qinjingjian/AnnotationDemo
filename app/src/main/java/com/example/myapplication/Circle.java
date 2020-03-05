package com.example.myapplication;

import com.example.annotation.Factory;

@Factory(id = "Circle", type = IShape.class)
public class Circle implements IShape {  //  TypeElement

    private int i; //   VariableElement
    private Triangle triangle;  //  VariableElement


    public Circle() {   //    ExecuteableElement
    }


    public void draw(   //  ExecuteableElement
                        String s)   //  VariableElement
    {
        System.out.println(s);
    }

    @Override
    public String draw() {    //  ExecuteableElement
        return  "Draw a circle";
    }
}
