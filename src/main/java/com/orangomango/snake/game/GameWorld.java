package com.orangomango.snake.game;

import com.orangomango.snake.game.ai.Cycle;

public class GameWorld{
	private int w, h;
	private Cycle cycle;
	
	public GameWorld(int w, int h){
		this.w = w;
		this.h = h;

		// Fix width and height
		if (this.w % 2 == 1 && this.h % 2 == 1){
			this.w--;
		}

		System.out.format("%dx%d\n", this.w, this.h);
		this.cycle = new Cycle(this.w, this.h);
		this.cycle.generate(250);
	}

	public Cycle getCycle(){
		return this.cycle;
	}
	
	public int getWidth(){
		return this.w;
	}
	
	public int getHeight(){
		return this.h;
	}

	public boolean isInsideMap(int x, int y){
		return x >= 0 && y >= 0 && x < this.w && y < this.h;
	}
}
