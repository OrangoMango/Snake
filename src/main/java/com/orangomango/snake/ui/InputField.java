package com.orangomango.snake.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.Font;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.geometry.Rectangle2D;

public class InputField extends UiElement implements MouseSensible{
	private String placeholder;
	private boolean focus = false;
	private String text = "";
	private boolean password = false;

	public InputField(double x, double y, double w, double h, String placeholder){
		super(x, y, w, h);
		this.placeholder = placeholder;
	}

	@Override
	public void onClick(double ex, double ey){
		Rectangle2D rect = new Rectangle2D(rw(this.x), rh(this.y), rw(this.w), rh(this.h));
		if (rect.contains(ex, ey)){
			this.focus = true;
		} else {
			this.focus = false;
		}
	}

	public void keyPress(KeyEvent e){
		if (this.focus){
			if (e.getCode() == KeyCode.BACK_SPACE){
				this.text = this.text.length() == 0 ? "" : this.text.substring(0, this.text.length()-1);
			}
		}	
	}

	public void keyType(KeyEvent e){
		if (this.focus){
			String character = e.getCharacter();
			if (!character.isEmpty() && !character.contains("\r") && !character.contains("\b") && !character.contains("\t")){
				this.text += character;
			}
		}
	}

	public void setPasswordField(boolean value){
		this.password = value;
	}

	public void setText(String value){
		this.text = value;
	}

	public String getText(){
		return this.text;
	}

	@Override
	public void render(GraphicsContext gc){
		gc.setFill(Color.web("#001219"));
		gc.fillRoundRect(rw(this.x), rh(this.y), rw(this.w), rh(this.h), rh(0.035), rh(0.035));

		gc.setStroke(Color.web(this.focus ? "#10b981" : "#1e293b"));
		gc.setLineWidth(rh(0.0035));
		gc.strokeRoundRect(rw(this.x), rh(this.y), rw(this.w), rh(this.h), rh(0.035), rh(0.035));

		gc.setFont(UiElement.FONT_SMALLSMALL);
		gc.setTextAlign(TextAlignment.LEFT);
		gc.setFill(this.text == "" ? Color.web("#475569") : Color.web("#f8fafc"));
		final String displayText = this.password ? "*".repeat(this.text.length()) : this.text;
		gc.fillText(this.text == "" ? this.placeholder : displayText, rw(this.x+0.05*this.w), rh(this.y+0.65*this.h));

		final double textWidth = getTextWidth(displayText, UiElement.FONT_SMALLSMALL);
		if (this.focus && System.currentTimeMillis() % 1000 < 500) {
		    gc.setStroke(Color.web("#10b981"));
		    gc.setLineWidth(2);
		    final double cursorX = rw(this.x+0.05*this.w) + textWidth + 2;
		    gc.strokeLine(cursorX, rh(this.y+0.25*this.h), cursorX, rh(this.y+0.75*this.h));
		}

	}

	private static double getTextWidth(String text, Font font) {
	    javafx.scene.text.Text helper = new javafx.scene.text.Text(text);
	    helper.setFont(font);
	    return helper.getLayoutBounds().getWidth();
	}
}