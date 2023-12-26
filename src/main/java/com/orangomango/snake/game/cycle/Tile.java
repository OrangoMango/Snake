package com.orangomango.snake.game.cycle;

public class Tile{
	private int x, y;
	public boolean visited;

	public Tile(int x, int y){
		this.x = x;
		this.y = y;
	}

	public int getX(){
		return this.x;
	}

	public int getY(){
		return this.y;
	}

	@Override
	public String toString(){
		return String.format("{%d %d}", this.x, this.y);
	}
}