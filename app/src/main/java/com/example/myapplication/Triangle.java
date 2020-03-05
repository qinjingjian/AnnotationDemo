package com.example.myapplication;


import com.example.annotation.Factory;

@Factory(id = "Triangle", type = IShape.class)
public class Triangle implements IShape {
	@Override
	public String draw() {
		return "Draw a Triangle";
	}
}
