package com.orangomango.snake.game;

public class GameWorld{
	private boolean[][] map;
	private int w, h;
	
	public GameWorld(int w, int h){
		this.w = w;
		this.h = h;
		this.map = new boolean[w][h];
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
	
	public boolean isSolid(int i, int j){
		return this.map[i][j];
	}
	
	public void set(int i, int j){
		this.map[i][j] = true;
	}
}
