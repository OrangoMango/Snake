package com.orangomango.snake.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class SnakeBody{
	public static int SIZE = 25;
	
	public int x, y;
	
	public SnakeBody(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public void render(GraphicsContext gc, boolean first){
		gc.setFill(first ? Color.DARKGREEN : Color.GREEN);
		gc.fillRect(this.x*SIZE, this.y*SIZE, SIZE, SIZE);
	}
	
	public void wrap(int w, int h){
		if (this.x >= w){
			this.x = 0;
		}
		if (this.x < 0){
			this.x = w-1;
		}
		if (this.y < 0){
			this.y = h-1;
		}
		if (this.y >= h){
			this.y = 0;
		}
	}
}
