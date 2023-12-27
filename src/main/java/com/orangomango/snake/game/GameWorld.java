package com.orangomango.snake.game;

import com.orangomango.snake.game.cycle.Cycle;

public class GameWorld{
	private boolean[][] map;
	private int w, h;
	private Cycle cycle;
	
	public GameWorld(int w, int h){
		this.w = w;
		this.h = h;
		this.map = new boolean[w][h];
		this.cycle = new Cycle(w, h);
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
	
	public void clear(){
		this.map = new boolean[this.w][this.h];
	}

	public boolean isBoardFull(){
		for (int x = 0; x < this.w; x++){
			for (int y = 0; y < this.h; y++){
				if (!this.map[x][y]){
					return false;
				}
			}
		}

		return true;
	}
	
	public boolean isSolid(int i, int j){
		return this.map[i][j];
	}
	
	public void set(int i, int j){
		this.map[i][j] = true;
	}

	public boolean isInsideMap(int x, int y){
		return x >= 0 && y >= 0 && x < this.w && y < this.h;
	}
}
