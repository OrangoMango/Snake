package com.orangomango.snake.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.effect.DropShadow;

public class SnakeBody {
    public static int SIZE = 25;
    private static Image IMAGE = new Image(SnakeBody.class.getResourceAsStream("/snake.png"));

    public int x, y;

    public SnakeBody(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void render(GraphicsContext gc, boolean head, SnakeBody next, SnakeBody prev){
        int rotation = 0;
        int imgIndex = 0;

        if (head){
            imgIndex = 3;
        } else if (next == null){
            imgIndex = 2;
        } else if (prev == null){
            imgIndex = 0;
        } else {
            boolean straight = (getWrappedDist(prev.x, next.x) == 0 || getWrappedDist(prev.y, next.y) == 0);
            imgIndex = straight ? 0 : 1;
        }

        if (imgIndex == 1){
            int dx1 = getWrappedDist(prev.x, this.x);
            int dy1 = getWrappedDist(prev.y, this.y);
            int dx2 = getWrappedDist(next.x, this.x);
            int dy2 = getWrappedDist(next.y, this.y);

            if ((dx1 == -1 && dy2 == -1) || (dx2 == -1 && dy1 == -1)) rotation = 0;    // LEFT & UP
            else if ((dy1 == -1 && dx2 == 1) || (dy2 == -1 && dx1 == 1)) rotation = 90;   // UP & RIGHT
            else if ((dx1 == 1 && dy2 == 1) || (dx2 == 1 && dy1 == 1)) rotation = 180;  // RIGHT & DOWN
            else if ((dy1 == 1 && dx2 == -1) || (dy2 == 1 && dx1 == -1)) rotation = 270;  // DOWN & LEFT
        } else {
            SnakeBody target = (next != null) ? next : prev;
            if (target != null){
                int dx = getWrappedDist(target.x, this.x);
                int dy = getWrappedDist(target.y, this.y);

                if (imgIndex == 0){
                    rotation = (dx != 0) ? 0 : 90;
                } else {
                    if (dx == -1) rotation = 0;
                    else if (dy == -1) rotation = 90;
                    else if (dx == 1) rotation = 180;
                    else if (dy == 1) rotation = 270;
                }
            }
        }

        gc.save();
        double renderX = Math.round(this.x * SIZE);
        double renderY = Math.round(this.y * SIZE);
        
        gc.translate(renderX + SIZE / 2.0, renderY + SIZE / 2.0);
        gc.rotate(rotation);
        
        gc.setEffect(new DropShadow(10, Color.web("#10b981")));
        
        gc.drawImage(IMAGE, 1+imgIndex*34, 1, 32, 32, -SIZE / 2.0, -SIZE / 2.0, SIZE, SIZE);
        gc.restore();
    }

    private static int getWrappedDist(int a, int b){
        int d = a - b;
        if (Math.abs(d) > 1){
            return d > 0 ? -1 : 1;
        }
        return d;
    }

    public void wrap(int w, int h){
        if (this.x >= w) this.x = 0;
        if (this.x < 0) this.x = w - 1;
        if (this.y < 0) this.y = h - 1;
        if (this.y >= h) this.y = 0;
    }

    public boolean outside(int w, int h){
        return (this.x >= w || this.x < 0 || this.y < 0 || this.y >= h);
    }
}