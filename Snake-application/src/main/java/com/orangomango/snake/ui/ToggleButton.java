package com.orangomango.snake.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.geometry.Rectangle2D;

import com.orangomango.snake.MainApplication;

public class ToggleButton extends UiElement implements MouseSensible{
	private String label;
	private boolean selected = false;
	private Runnable sChanged = null;

	public ToggleButton(double x, double y, double w, double h, String label){
		super(x, y, w, h);
		this.label = label;
	}

	@Override
	public void onClick(double ex, double ey){
		Rectangle2D rect = new Rectangle2D(rw(this.x), rh(this.y), rw(this.w), rh(this.h));
		if (rect.contains(ex, ey)){
			this.selected = !this.selected;
			if (this.sChanged != null) this.sChanged.run();
			MainApplication.playSound("gui");
		}
	}

	@Override
	public void render(GraphicsContext gc){
		// Label text
		gc.setFill(Color.web("#94f7d4"));
		gc.setFont(UiElement.FONT_SMALLSMALL);
		gc.setTextAlign(TextAlignment.LEFT);
		gc.fillText(this.label, rw(this.x+0.02*this.w), rh(this.y+0.60*this.h));

		// Toggle
		gc.setFill(Color.web(this.selected ? "#064e3b" : "#1e293b"));
		Rectangle2D rect = getToggleRect();
		gc.fillRoundRect(rect.getMinX(), rect.getMinY(), rect.getWidth(), rect.getHeight(), rh(0.017), rh(0.017));

		if (this.selected){
			gc.setFill(Color.web("#10b981"));
			gc.fillOval(rw(this.x+0.90*this.w-0.06*this.h), rh(this.y+0.50*this.h)-rw(0.06*this.h), rw(0.12*this.h), rw(0.12*this.h));
		} else {
			gc.setFill(Color.web("#94a3b8"));
			gc.fillOval(rw(this.x+0.80*this.w-0.06*this.h), rh(this.y+0.50*this.h)-rw(0.06*this.h), rw(0.12*this.h), rw(0.12*this.h));
		}
	}

	public boolean getSelected(){
		return this.selected;
	}

	public void setSelected(boolean v){
		this.selected = v;
	}

	private Rectangle2D getToggleRect(){
		return new Rectangle2D(rw(this.x+0.75*this.w), rh(this.y+0.35*this.h), rw(0.20*this.w), rh(0.30*this.h));
	}

	public void setOnStateChanged(Runnable r){
		this.sChanged = r;
	}
}