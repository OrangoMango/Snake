package com.orangomango.snake.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.scene.effect.DropShadow;
import javafx.util.Pair;
import javafx.geometry.Rectangle2D;

import java.util.ArrayList;

import com.orangomango.snake.MainApplication;

public class MultistateButton extends UiElement implements MouseSensible{
	private Color color;
	private ArrayList<Pair<String, Color>> states = new ArrayList<>();
	private ArrayList<Runnable> onStateChanged = new ArrayList<>();
	private int currentSelection = 0;
	private boolean disabled = false;
	private Runnable sChanged = null;

	public MultistateButton(Color color, double x, double y, double w, double h){
		super(x, y, w, h);
		this.color = color;
	}

	@Override
	public void onClick(double ex, double ey){
		Rectangle2D rect = new Rectangle2D(rw(this.x), rh(this.y), rw(this.w), rh(this.h));
		if (rect.contains(ex, ey)){
			this.currentSelection = (this.currentSelection + 1) % this.states.size();
			this.onStateChanged.get(this.currentSelection).run();
			if (this.sChanged != null) this.sChanged.run();
			MainApplication.playSound("gui");
		}
	}

	public void addState(String text, Color color, Runnable r){
		this.states.add(new Pair<String, Color>(text, color));
		this.onStateChanged.add(r);
	}

	@Override
	public void render(GraphicsContext gc){
		Pair<String, Color> selected = this.states.get(this.currentSelection);
		if (this.disabled) selected = new Pair<String, Color>(selected.getKey(), Color.web("#334155"));

		gc.save();
		gc.setFill(this.color);
		gc.setEffect(new DropShadow(15, selected.getValue()));
		gc.fillRoundRect(rw(this.x), rh(this.y), rw(this.w), rh(this.h), rh(0.035), rh(0.035));
		gc.setStroke(selected.getValue());
		gc.setLineWidth(rh(0.0035));
		gc.strokeRoundRect(rw(this.x), rh(this.y), rw(this.w), rh(this.h), rh(0.035), rh(0.035));
		gc.restore();

		gc.setFill(selected.getValue());
		gc.setFont(UiElement.FONT_SMALL);
		gc.setTextAlign(TextAlignment.CENTER);
		gc.fillText(selected.getKey(), rw(this.x+0.5*this.w), rh(this.y+0.45*this.h));

		final double dotRadius = 0.05*this.h;
		final double dotSpacing = 0.1*this.w;
		final double totalWidth = (this.states.size()-1)*dotSpacing;

		for (int i = 0; i < this.states.size(); i++){
			gc.setFill(this.currentSelection == i ? selected.getValue() : Color.web("#1e293b"));
			gc.fillOval(rw(this.x + (this.w-totalWidth)*0.5 + i*dotSpacing) - rh(dotRadius), rh(this.y+0.75*this.h - dotRadius), rh(dotRadius*2), rh(dotRadius*2));
		}
	}

	public void setState(int index){
		this.currentSelection = index % this.states.size();
	}

	public void setDisabled(boolean value){
		this.disabled = value;
	}

	public void setOnStateChanged(Runnable r){
		this.sChanged = r;
	}
}