package com.orangomango.snake.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class SnakeBody{
	public static int SIZE = 25;
	private static Image IMAGE = new Image(SnakeBody.class.getResourceAsStream("/snake.png"));
	
	public int x, y;
	
	public SnakeBody(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public void render(GraphicsContext gc, boolean head, SnakeBody next, SnakeBody prev){
		int rotation = 0;
		int imgIndex = 0;
		boolean flip = false;
		if (head){
			imgIndex = 3;
		} else {
			if (next == null){
				imgIndex = 2;
			} else {
				if (prev == null){
					imgIndex = 0;
				} else {
					imgIndex = prev.x == next.x || prev.y == next.y ? 0 : 1;
				}
			}
		}

		if (imgIndex == 1){
			if (next.y-this.y == -1){
				rotation = 0;
				flip = prev.x-this.x == 1;
			} else if (next.x-this.x == 1){
				rotation = 90;
				flip = prev.y-this.y == 1;
			} else if (next.y-this.y == 1){
				rotation = 180;
				flip = prev.x-this.x == -1;
			} else if (next.x-this.x == -1){
				rotation = 270;
				flip = prev.y-this.y == -1;
			}
		} else if (imgIndex == 0 || imgIndex == 2 || imgIndex == 3){
			if (next != null){
				if (next.y-this.y == -1){
					rotation = 90;
				} else if (next.x-this.x == 1){
					rotation = 180;
				} else if (next.y-this.y == 1){
					rotation = 270;
				} else if (next.x-this.x == -1){
					rotation = 0;
				}
			} else if (prev != null){
				if (prev.y-this.y == -1){
					rotation = 90;
				} else if (prev.x-this.x == 1){
					rotation = 180;
				} else if (prev.y-this.y == 1){
					rotation = 270;
				} else if (prev.x-this.x == -1){
					rotation = 0;
				}
			}
		}

		gc.save();
		gc.translate((this.x+0.5)*SIZE, (this.y+0.5)*SIZE);
		gc.rotate(rotation);
		gc.drawImage(IMAGE, 1+imgIndex*34, 1, 32, 32, -SIZE*0.5+(flip ? SIZE : 0), -SIZE*0.5, SIZE*(flip ? -1 : 1), SIZE);
		gc.restore();
	}
	
	public void wrap(int w, int h){
		if (this.x >= w){
			this.x = 0;
		}
		if (this.x < 0){
			this.x = w-1;
		}
		if (this.y < 0){
			this.y = h-1;
		}
		if (this.y >= h){
			this.y = 0;
		}
	}
	
	public boolean outside(int w, int h){
		if (this.x >= w){
			return true;
		}
		if (this.x < 0){
			return true;
		}
		if (this.y < 0){
			return true;
		}
		if (this.y >= h){
			return true;
		}
		return false;
	}
}
