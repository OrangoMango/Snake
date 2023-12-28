package com.orangomango.snake.game.pathfinder;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import com.orangomango.snake.game.SnakeBody;

public class Cell{
	private int x, y;
	public boolean start, end;
	public boolean visited, solid, path, soft;
	public Cell parent;

	public Cell(int x, int y, boolean solid){
		this.x = x;
		this.y = y;
		this.solid = solid;
	}

	public int getX(){
		return this.x;
	}

	public int getY(){
		return this.y;
	}

	public void setStart(){
		this.start = true;
	}

	public void setEnd(){
		this.end = true;
		this.solid = false;
	}
	
	public void render(GraphicsContext gc, boolean showText){
		gc.setStroke(Color.BLACK);
		gc.strokeRect(this.x*SnakeBody.SIZE, this.y*SnakeBody.SIZE, SnakeBody.SIZE, SnakeBody.SIZE);
		Color color = null;
		if (this.solid){
			color = Color.BLACK;
		} else {
			color = Color.WHITE;
		}

		if (visited){
			color = Color.ORANGE;
		}
		if (path){
			color = Color.GREEN;
		}

		if (this.start){
			color = Color.BLUE;
		} else if (this.end){
			color = Color.CYAN;
		}

		gc.setFill(color);
		gc.fillRect(this.x*SnakeBody.SIZE, this.y*SnakeBody.SIZE, SnakeBody.SIZE, SnakeBody.SIZE);
		gc.setFill(Color.BLACK);
		if (showText) gc.fillText(this.toString(), this.x*SnakeBody.SIZE, this.y*SnakeBody.SIZE+SnakeBody.SIZE/2);
	}
	
	@Override
	public String toString(){
		return String.format("%d %d", this.x, this.y);
	}
}