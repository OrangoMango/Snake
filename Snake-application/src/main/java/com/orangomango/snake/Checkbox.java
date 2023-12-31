package com.orangomango.snake;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.geometry.Rectangle2D;

import dev.webfx.platform.resource.Resource;

public class Checkbox{
	private double x, y;
	private String text;
	private boolean on = false;
	
	private static final double WIDTH = 0.23*HomeScreen.HEIGHT;
	private static final double HEIGHT = 0.1*HomeScreen.HEIGHT;
	private static final Image IMAGE_ON = new Image(Resource.toUrl("/images/checkbox_on.png", Checkbox.class));
	private static final Image IMAGE_OFF = new Image(Resource.toUrl("/images/checkbox_off.png", Checkbox.class));
	
	public Checkbox(double x, double y, String text){
		this.x = x;
		this.y = y;
		this.text = text;
	}
	
	public boolean contains(double x, double y){
		Rectangle2D rect = new Rectangle2D(this.x, this.y, WIDTH, HEIGHT);
		return rect.contains(x, y);
	}
	
	public void toggle(){
		this.on = !this.on;
	}
	
	public boolean isSelected(){
		return this.on;
	}
	
	public void render(GraphicsContext gc){
		gc.drawImage(on ? IMAGE_ON : IMAGE_OFF, this.x, this.y, WIDTH, HEIGHT);
		gc.setFill(Color.WHITE);
		gc.fillText(this.text, this.x+WIDTH*1.1, this.y+HEIGHT/2);
	}
}
