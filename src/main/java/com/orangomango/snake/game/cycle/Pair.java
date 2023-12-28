package com.orangomango.snake.game.cycle;

public class Pair<T, U>{
	public T a;
	public U b;

	public Pair(T a, U b){
		this.a = a;
		this.b = b;
	}

	@Override
	public boolean equals(Object other){
		if (other instanceof Pair p){
			return (p.a.equals(this.a) && p.b.equals(this.b)) || (p.a.equals(this.b) && p.b.equals(this.a));
		} else return false;
	}
}