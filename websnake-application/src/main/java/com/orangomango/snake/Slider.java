package com.orangomango.snake;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.Rectangle2D;

public class Slider{
	private double x, y;
	private int min, max;
	private double cursor = 0;
	
	private static final double WIDTH = 0.3*HomeScreen.WIDTH;
	private static final double HEIGHT = 0.1*HomeScreen.HEIGHT;
	
	public Slider(double x, double y, int min, int max, int defaultValue){
		this.x = x;
		this.y = y;
		this.min = min;
		this.max = max;
		this.cursor = (double)defaultValue/(max-min);
	}
	
	public double contains(double x, double y){
		Rectangle2D rect = new Rectangle2D(this.x, this.y, WIDTH, HEIGHT);
		if (rect.contains(x, y)){
			return (x-this.x)/WIDTH;
		} else {
			return -1;
		}
	}
	
	public void updateCursor(double cursor){
		this.cursor = cursor;
	}
	
	public int getValue(){
		return (int)Math.round(this.min+this.cursor*(this.max-this.min));
	}
	
	public void render(GraphicsContext gc){
		gc.setFill(Color.WHITE);
		gc.fillRect(this.x, this.y+0.3*HEIGHT, WIDTH, HEIGHT*0.3);
		gc.fillOval(this.x+this.cursor*WIDTH-HEIGHT/2, this.y, HEIGHT, HEIGHT);
		gc.fillText(Integer.toString(getValue()), this.x+this.cursor*WIDTH-HEIGHT/2, this.y+1.25*HEIGHT);
	}
}
