package com.orangomango.snake.game.cycle;

import java.util.Objects;

public class Point{
	public int x, y;

	public Point(int x, int y){
		this.x = x;
		this.y = y;
	}

	@Override
	public boolean equals(Object other){
		if (other instanceof Point p){
			return this.x == p.x && this.y == p.y;
		} else return false;
	}

	@Override
	public int hashCode(){
		return Objects.hash(this.x, this.y);
	}
}