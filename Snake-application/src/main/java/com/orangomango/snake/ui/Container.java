package com.orangomango.snake.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class Container extends UiElement{
	private Color titleColor, color, borderColor;
	private Font titleFont;
	private String titleText;

	public Container(Color color, Color bcolor, double x, double y, double w, double h, Color titleColor, Font titleFont, String titleText){
		super(x, y, w, h);
		this.color = color;
		this.borderColor = bcolor;
		this.titleColor = titleColor;
		this.titleFont = titleFont;
		this.titleText = titleText;
	}

	@Override
	public void render(GraphicsContext gc){
		gc.setFill(this.color);
		gc.fillRoundRect(rw(this.x), rh(this.y), rw(this.w), rh(this.h), rh(0.035), rh(0.035));
		gc.setStroke(this.borderColor);
		gc.setLineWidth(rh(0.0035));
		gc.strokeRoundRect(rw(this.x), rh(this.y), rw(this.w), rh(this.h), rh(0.035), rh(0.035));

		if (this.titleText != null){
			gc.setFill(this.titleColor);
			gc.setTextAlign(TextAlignment.CENTER);
			gc.setFont(this.titleFont);
			gc.fillText(this.titleText, rw(this.x+this.w*0.5), rh(this.y+0.053));
		}
	}
}