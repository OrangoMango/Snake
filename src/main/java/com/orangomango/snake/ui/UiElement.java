package com.orangomango.snake.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static com.orangomango.snake.HomeScreen.WIDTH;
import static com.orangomango.snake.HomeScreen.HEIGHT;

public abstract class UiElement{
	protected double x, y, w, h;

	public static Font FONT_SMALLSMALLSMALL = Font.loadFont(UiElement.class.getResourceAsStream("/main_font.ttf"), 0.018*HEIGHT);
	public static Font FONT_SMALLSMALL = Font.loadFont(UiElement.class.getResourceAsStream("/main_font.ttf"), 0.025*HEIGHT);
	public static Font FONT_SMALL = Font.loadFont(UiElement.class.getResourceAsStream("/main_font.ttf"), 0.035*HEIGHT);
	public static Font FONT_MEDIUM = Font.loadFont(UiElement.class.getResourceAsStream("/main_font.ttf"), 0.05*HEIGHT);
	public static Font FONT_LARGE = Font.loadFont(UiElement.class.getResourceAsStream("/main_font.ttf"), 0.065*HEIGHT);

	public UiElement(double x, double y, double w, double h){
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	public abstract void render(GraphicsContext gc);

	public static double rw(double x){
		return x * WIDTH;
	}

	public static double rh(double y){
		return y * HEIGHT;
	}
}