package com.orangomango.snake.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.Rectangle2D;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import static com.orangomango.snake.HomeScreen.HEIGHT;
import com.orangomango.snake.MainApplication;

public class Button extends UiElement implements MouseSensible{
	private Runnable onClick;
	private String text;
	private Color textColor, color, borderColor;
	private Font font;
	private boolean onHover = false;
	private double borderSize = 0.0035;

	private boolean bouncing;
	private long lastBounce;
	private int bounceCount, bouncingDir;
	private double bouncingX;

	public Button(Color color, Color bcolor, double x, double y, double w, double h, String text, Font bFont, Color tColor, Runnable onClick){
		super(x, y, w, h);
		this.onClick = onClick;
		this.text = text;
		this.textColor = tColor;
		this.font = bFont;
		this.color = color;
		this.borderColor = bcolor;
	}

	@Override
	public void onClick(double ex, double ey){
		Rectangle2D rect = new Rectangle2D(rw(this.x), rh(this.y), rw(this.w), rh(this.h));
		if (rect.contains(ex, ey)){
			this.onClick.run();
			MainApplication.playSound("gui");
		}
	}

	@Override
	public void onHover(double ex, double ey){
		Rectangle2D rect = new Rectangle2D(rw(this.x), rh(this.y), rw(this.w), rh(this.h));
		this.onHover = rect.contains(ex, ey);
	}

	@Override
	public void render(GraphicsContext gc){
		final double sizeFactor = this.onHover ? 1.05 : 1;
		final double adj = this.onHover ? 0.025 : 0;

		gc.save();
		gc.translate(rw(this.bouncingX), 0);

		long now = System.currentTimeMillis();
		if (this.bouncing && now-this.lastBounce > 10){
			this.bouncingX += 0.003 * this.bouncingDir;
			if (Math.abs(this.bouncingX) >= 0.009){
				this.bouncingDir *= -1;
				this.bounceCount++;
			}
			this.lastBounce = now;

			if (this.bounceCount == 2){
				this.bouncing = false;
				this.bouncingX = 0;
			}
		}

		gc.setFill(this.color);
		gc.fillRoundRect(rw(this.x-adj*this.w), rh(this.y-adj*this.h), rw(this.w * sizeFactor), rh(this.h * sizeFactor), rh(0.035), rh(0.035));
		gc.setStroke(this.borderColor);
		gc.setLineWidth(rh(this.borderSize));
		gc.strokeRoundRect(rw(this.x-adj*this.w), rh(this.y-adj*this.h), rw(this.w * sizeFactor), rh(this.h * sizeFactor), rh(0.035), rh(0.035));

		gc.setFill(this.textColor);
		gc.setFont(this.font);
		gc.setTextAlign(TextAlignment.CENTER);
		gc.fillText(this.text, rw(this.x-adj*this.w+this.w*0.5*sizeFactor), rh(this.y-adj*this.h+this.h*0.65*sizeFactor));

		gc.restore();
	}

	public void bounce(){
		this.bouncing = true;
		this.bouncingX = 0;
		this.bounceCount = 0;
		this.bouncingDir = -1;
		this.lastBounce = System.currentTimeMillis();
	}

	public void setBorderSize(double v){
		this.borderSize = v;
	}

	public void setStyle(Color color, Color bColor, String text, Color tColor){
		if (color != null) this.color = color;
		if (bColor != null) this.borderColor = bColor;
		if (text != null) this.text = text;
		if (tColor != null) this.textColor = tColor;
	}
}