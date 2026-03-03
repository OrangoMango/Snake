package com.orangomango.snake.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;

public class Apple{
	public static int SIZE = SnakeBody.SIZE;
	
	public int x, y;
	
	public Apple(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public void render(GraphicsContext gc) {
	    double centerX = (this.x + 0.5) * SIZE;
	    double centerY = (this.y + 0.5) * SIZE;
	    
	    double pulse = 1.0 + 0.15 * Math.sin(System.currentTimeMillis() / 200.0);
	    double currentSize = (SIZE * 0.7) * pulse;

	    gc.save();
	    gc.setEffect(new DropShadow(15 * pulse, Color.web("#ef4444")));
	    
	    gc.setFill(Color.web("#ef4444"));
	    double[] xPoints = {centerX, centerX+currentSize/2, centerX, centerX-currentSize/2};
	    double[] yPoints = {centerY-currentSize/2, centerY, centerY+currentSize/2, centerY};
	    gc.fillPolygon(xPoints, yPoints, 4);
	    
	    gc.setEffect(null);
	    gc.setFill(Color.web("#fecaca", 0.8));
	    double sparkSize = currentSize * 0.3;
	    double[] xSpark = {centerX, centerX+sparkSize/2, centerX, centerX-sparkSize/2};
	    double[] ySpark = {centerY-sparkSize/2, centerY, centerY+sparkSize/2, centerY};
	    gc.fillPolygon(xSpark, ySpark, 4);

	    gc.restore();
	}
}
