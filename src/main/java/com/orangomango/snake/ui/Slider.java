package com.orangomango.snake.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.geometry.Rectangle2D;

import com.orangomango.snake.MainApplication;

public class Slider extends UiElement implements MouseSensible{
	private double min, max, def;
	private String label, unit;
	private double value;
	private boolean selected = false;
	private Runnable sChanged = null;

	public Slider(double x, double y, double w, double h, String label, String unit){
		super(x, y, w, h);
		this.label = label;
		this.unit = unit;
	}

	public void setInterval(double min, double max, double def){
		this.min = min;
		this.max = max;
		this.def = def;

		this.value = (this.def-this.min) / (this.max-this.min);
	}

	@Override
	public void onClick(double ex, double ey){
		Rectangle2D rect = getBallBounds();
		if (rect.contains(ex, ey)){
			this.selected = true;
		}
	}

	@Override
	public void onRelease(double ex, double ey){
		this.selected = false;
		Rectangle2D rect = getBallBounds();
		if (rect.contains(ex, ey)){
			MainApplication.playSound("gui");
		}
	}

	@Override
	public void onDrag(double ex, double ey){
		if (this.selected){
			double newValue = (ex-rw(this.x)) / rw(this.w);
			this.value = Math.min(1, Math.max(newValue, 0));
			if (this.sChanged != null) this.sChanged.run();
		}
	}

	@Override
	public void render(GraphicsContext gc){
		// Label text
		gc.setFill(Color.web("#94f7d4"));
		gc.setFont(UiElement.FONT_SMALLSMALL);
		gc.setTextAlign(TextAlignment.LEFT);
		gc.fillText(this.label, rw(this.x+0.02*this.w), rh(this.y+0.25*this.h));

		// Value text
		gc.setFill(Color.web("#10b981"));
		gc.setFont(UiElement.FONT_SMALLSMALL);
		gc.setTextAlign(TextAlignment.RIGHT);
		gc.fillText(String.format("%.0f%s", this.value*(this.max-this.min)+this.min, this.unit), rw(this.x+0.98*this.w), rh(this.y+0.25*this.h));

		// Slider bar
		gc.setFill(Color.web("#1e293b"));
		gc.fillRect(rw(this.x+0.05*this.w), rh(this.y+0.70*this.h), rw(0.90*this.w), rh(0.05*this.h));

		// Slider ball
		Rectangle2D ball = getBallBounds();
		gc.setFill(Color.web("#10b981"));
		gc.fillOval(ball.getMinX(), ball.getMinY(), ball.getWidth(), ball.getHeight());
	}

	public double getValue(){
		return this.min + this.value * (this.max-this.min);
	}

	public void setValue(double v){
		if (v < this.min || v > this.max){
			throw new IllegalStateException("Input value out of bounds");
		}

		this.value = (v-this.min) / (this.max-this.min);
	}

	private Rectangle2D getBallBounds(){
		return new Rectangle2D(rw((this.x+0.05*this.w)+(this.value*0.90*this.w))-rh(0.015), rh(this.y+0.71*this.h-0.015), rh(0.03), rh(0.03));	
	}

	public void setOnStateChanged(Runnable r){
		this.sChanged = r;
	}
}