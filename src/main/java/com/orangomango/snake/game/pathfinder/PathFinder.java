package com.orangomango.snake.game.pathfinder;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.*;

import com.orangomango.snake.game.GameWorld;
import com.orangomango.snake.game.SnakeBody;

public class PathFinder{
	private final int startX, startY, endX, endY;
	private final Cell[][] map;
	private Cell nextCell = null;

	public PathFinder(GameWorld gw, int sx, int sy, int ex, int ey, SnakeBody head, SnakeBody tail){
		this.map = new Cell[gw.getWidth()][gw.getHeight()];
		this.startX = sx;
		this.startY = sy;
		this.endX = ex;
		this.endY = ey;
		loadMap(gw, head, tail);
		this.map[this.startX][this.startY].setStart();
		this.map[this.endX][this.endY].setEnd();
		calculate();
	}

	private void calculate(){
		List<Cell> cells = new ArrayList<>();
		cells.add(this.map[this.startX][this.startY]);
		boolean endReached = false;

		while (cells.size() != 0){
			Cell cell = cells.remove(0);
			if (cell.visited) continue;
			cell.visited = true;

			final int cx = cell.getX();
			final int cy = cell.getY();

			if (cx == this.endX && cy == this.endY){
				endReached = true;
				break;
			}

			Cell n = getCellAt(cx, cy-1);
			Cell e = getCellAt(cx+1, cy);
			Cell s = getCellAt(cx, cy+1);
			Cell w = getCellAt(cx-1, cy);

			boolean danger = false;
			int selected = -1;
			Random random = new Random();
			if ((n == null || n.solid) && (e == null || e.solid) && (s == null || s.solid) && (w == null || w.solid)){
				danger = true;
				selected = random.nextInt(4);
			}
			
			if (n != null && !n.visited && (!n.solid || (danger && n.soft && selected == 0))){
				n.parent = cell;
				cells.add(n);
			}
			if (e != null && !e.visited && (!e.solid || (danger && e.soft && selected == 1))){
				e.parent = cell;
				cells.add(e);
			}
			if (s != null && !s.visited && (!s.solid || (danger && s.soft && selected == 2))){
				s.parent = cell;
				cells.add(s);
			}
			if (w != null && !w.visited && (!w.solid || (danger && w.soft && selected == 3))){
				w.parent = cell;
				cells.add(w);
			}
		}

		if (endReached){
			Cell cell = this.map[endX][endY];
			while (cell != null){
				cell.path = true;
				if (cell.parent != null && cell.parent.parent == null){
					this.nextCell = cell;
					this.nextCell.marked = true;
				}
				cell = cell.parent;
			}
		} else {
			System.out.println("> Could not find a path");
		}
	}

	private Cell getCellAt(int x, int y){
		if (x >= 0 && y >= 0 && x < this.map.length && y < this.map[0].length){
			return this.map[x][y];
		} else return null;
	}

	public Cell getNextCell(){
		return this.nextCell;
	}

	private boolean isAcceptable(GameWorld gw, Cell cell, SnakeBody h, SnakeBody t){
		int tail = gw.getCycle().getIndex(t.x, t.y);
		int head = gw.getCycle().getIndex(h.x, h.y);
		int pos = gw.getCycle().getIndex(cell.getX(), cell.getY());
		int food = gw.getCycle().getIndex(this.endX, this.endY);
		return (pos < tail || pos > head) && (head > food || pos < food);
	}

	private void loadMap(GameWorld gw, SnakeBody head, SnakeBody tail){
		for (int i = 0; i < gw.getWidth(); i++){
			for (int j = 0; j < gw.getHeight(); j++){
				this.map[i][j] = new Cell(i, j, gw.isSolid(i, j));
				if (!isAcceptable(gw, this.map[i][j], head, tail)){
					if (!this.map[i][j].solid) this.map[i][j].soft = true;
					this.map[i][j].solid = true;
				}
			}
		}
	}
	
	// Debug
	public void render(GraphicsContext gc, boolean showText){
		if (!showText) gc.setGlobalAlpha(0.3);
		for (int i = 0; i < map.length; i++){
			for (int j = 0; j < map[0].length; j++){
				map[i][j].render(gc, showText);
			}
		}
		gc.setGlobalAlpha(1);
	}
}
