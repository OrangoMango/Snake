package com.orangomango.snake.ui;

public interface MouseSensible{
	public void onClick(double x, double y);
	
	public default void onRelease(double x, double y){
	}
	
	public default void onDrag(double x, double y){
	}

	public default void onHover(double x, double y){
	}
}