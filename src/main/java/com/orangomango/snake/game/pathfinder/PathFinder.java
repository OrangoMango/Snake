package com.orangomango.snake.game.pathfinder;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.*;

import com.orangomango.snake.game.GameWorld;

public class PathFinder implements Iterable<Cell>{
	private final int startX, startY, endX, endY;
	private final Cell[][] map;
	private final List<Cell> moves = new ArrayList<>();
	private final List<Cell> openCells = new ArrayList<>();
	private Cell currentCell;
	private boolean endReached = false;

	public PathFinder(GameWorld gw, int sx, int sy, int ex, int ey){
		this.map = new Cell[gw.getWidth()][gw.getHeight()];
		loadMap(gw);
		this.startX = sx;
		this.startY = sy;
		this.endX = ex;
		this.endY = ey;
		this.map[this.startX][this.startY].setStart();
		this.map[this.endX][this.endY].setEnd();
		currentCell = this.map[this.startX][this.startY];
		calculateCosts();
		calculate();
	}

	private void calculateCosts(){
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				Cell cell = map[i][j];
				int distanceX = Math.abs(cell.getX()-map[startX][startY].getX());
				int distanceY = Math.abs(cell.getY()-map[startX][startY].getY());
				cell.gCost = distanceX+distanceY;

				distanceX = Math.abs(cell.getX()-map[endX][endY].getX());
				distanceY = Math.abs(cell.getY()-map[endX][endY].getY());
				cell.hCost = distanceX+distanceY;
				cell.fCost = cell.gCost+cell.hCost;
			}
		}
	}

	private void openCell(int xp, int yp){
		Cell c = map[xp][yp];
		if (!c.solid && !c.visited && !c.open){
			c.open = true;
			c.parent = currentCell;
			openCells.add(c);
		}
	}

	private void calculate(){
		// Path-finding algorithm
		while (!endReached){
			int cx = currentCell.getX();
			int cy = currentCell.getY();

			boolean removed = openCells.remove(currentCell);
			currentCell.visited = true;

			if (isInMap(cx, cy-1)) openCell(cx, cy-1);
			if (isInMap(cx+1, cy)) openCell(cx+1, cy);
			if (isInMap(cx, cy+1)) openCell(cx, cy+1);
			if (isInMap(cx-1, cy)) openCell(cx-1, cy);

			int bestCellIndex = -1;
			int bestFCost = -1;
			for (int i = 0; i < openCells.size(); i++){
				if (i == 0){
					bestCellIndex = 0;
					bestFCost = openCells.get(i).fCost;
				} else if (openCells.get(i).fCost < bestFCost){
					bestFCost = openCells.get(i).fCost;
					bestCellIndex = i;
				} else if (openCells.get(i).fCost == bestFCost){
					if (openCells.get(i).hCost < openCells.get(bestCellIndex).hCost){
						bestCellIndex = i;
					}
				}
			}

			if (bestCellIndex >= 0){
				currentCell = openCells.get(bestCellIndex);
				endReached = currentCell.end;
			} else {
				break;
			}
		}

		if (endReached){
			Cell cell = this.map[endX][endY];
			while (cell != this.map[startX][startY]){
				cell.path = true;
				this.moves.add(cell);
				cell = cell.parent;
			}
			Collections.reverse(this.moves);
		}
	}

	private boolean isInMap(int x, int y){
		return x >= 0 && y >= 0 && x < map.length && y < map[0].length;
	}

	@Override
	public Iterator<Cell> iterator(){
		return moves.iterator();
	}

	private void loadMap(GameWorld gw){
		for (int i = 0; i < gw.getWidth(); i++){
			for (int j = 0; j < gw.getHeight(); j++){
				this.map[i][j] = new Cell(i, j, gw.isSolid(i, j));
			}
		}
	}
	
	/*public void render(GraphicsContext gc, boolean showText){
		if (!showText) gc.setGlobalAlpha(0.3);
		for (int i = 0; i < map.length; i++){
			for (int j = 0; j < map[0].length; j++){
				map[i][j].render(gc, showText);
			}
		}
		gc.setGlobalAlpha(1);
	}*/
}
