package com.orangomango.snake.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Apple{
	public static int SIZE = SnakeBody.SIZE;
	
	public int x, y;
	
	public Apple(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public void render(GraphicsContext gc){
		gc.setFill(Color.RED);
		gc.fillRect(this.x*SIZE, this.y*SIZE, SIZE, SIZE);
	}
}
