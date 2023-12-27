package com.orangomango.snake.game.cycle;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.Side;

import java.util.*;

import com.orangomango.snake.game.pathfinder.Cell;

public class Cycle{
	private int width, height;
	private int[][] map;
	private List<Point> currentLoop;
	private Point startPoint;
	private static final Map<Integer, int[]> ARCS = new HashMap<>();

	static {
		ARCS.put(8, new int[]{0, -1});
		ARCS.put(4, new int[]{1, 0});
		ARCS.put(2, new int[]{0, 1});
		ARCS.put(1, new int[]{-1, 0});
	}

	public Cycle(int w, int h){
		this.width = w;
		this.height = h;
		this.map = new int[w][h];
		buildInitialMap();
	}

	public void generate(int n){
		for (int i = 0; i < n; i++){
			Point[] sp = split();
			if (sp != null){
				modifyPath(sp);
				Point[] tu = mend(sp);
				modifyPath(tu);
			}
		}

		if (this.height % 2 == 0){
			this.map[this.startPoint.x][this.startPoint.y] |= 4;
		} else if (this.width % 2 == 0){
			this.map[this.startPoint.x][this.startPoint.y] |= 8;
		}
	}

	public Cell getNextCell(int x, int y, Side direction){
		int dx = this.map[x][y];
		if ((dx & 8) == 8 && direction != Side.BOTTOM){
			return new Cell(x+ARCS.get(8)[0], y+ARCS.get(8)[1], false);
		} else if ((dx & 4) == 4 && direction != Side.LEFT){
			return new Cell(x+ARCS.get(4)[0], y+ARCS.get(4)[1], false);
		} else if ((dx & 2) == 2 && direction != Side.TOP){
			return new Cell(x+ARCS.get(2)[0], y+ARCS.get(2)[1], false);
		} else if ((dx & 1) == 1 && direction != Side.RIGHT){
			return new Cell(x+ARCS.get(1)[0], y+ARCS.get(1)[1], false);
		} else return null;
	}

	private void modifyPath(Point[] spl){
		int pta = this.map[spl[0].x][spl[0].y];
		int ptb = this.map[spl[1].x][spl[1].y];
		if (pta == 8 || pta == 2){
			if (spl[0].x < spl[1].x){
				pta = 4;
				ptb = 1;
			} else {
				pta = 1;
				ptb = 4;
			}
		} else {
			if (spl[0].y < spl[1].y){
				pta = 2;
				ptb = 8;
			} else {
				pta = 8;
				ptb = 2;
			}
		}

		this.map[spl[0].x][spl[0].y] = pta;
		this.map[spl[1].x][spl[1].y] = ptb;
	}

	private Point move(Point point){
		if (isInside(point)){
			int[] d = ARCS.get(this.map[point.x][point.y]);
			if (d != null){
				Point moved = new Point(point.x+d[0], point.y+d[1]);
				if (isInside(moved)){
					return moved;
				}
			}
		}

		return null;
	}

	private boolean setLoop(Point start, Point stop){
		this.currentLoop = new ArrayList<>();
		Point point = start;
		while (point != null && this.currentLoop.size() <= this.width*this.height && !point.equals(stop)){
			point = move(point);
			this.currentLoop.add(point);
		}

		return point != null && point.equals(stop);
	}

	private Point[] split(){
		List<Pair<Point, Point>> candidates = new ArrayList<>();
		for (int x = 0; x < this.width; x++){
			for (int y = 0; y < this.height; y++){
				Point pt = new Point(x, y);
				int dx = this.map[x][y];
				if (dx == 8){
					Point cx = new Point(x+1, y-1);
					if (isInside(cx) && this.map[cx.x][cx.y] == 2){
						candidates.add(new Pair<>(pt, cx));
					}
				} else if (dx == 2){
					Point cx = new Point(x+1, y+1);
					if (isInside(cx) && this.map[cx.x][cx.y] == 8){
						candidates.add(new Pair<>(pt, cx));
					}
				} else if (dx == 4){
					Point cx = new Point(x+1, y+1);
					if (isInside(cx) && this.map[cx.x][cx.y] == 1){
						candidates.add(new Pair<>(pt, cx));
					}
				} else if (dx == 1){
					Point cx = new Point(x-1, y+1);
					if (isInside(cx) && this.map[cx.x][cx.y] == 4){
						candidates.add(new Pair<>(pt, cx));
					}
				}
			}
		}

		if (candidates.size() > 0){
			Random random = new Random();
			Pair<Point, Point> pair = candidates.get(random.nextInt(candidates.size()));
			if (setLoop(pair.a, pair.b)){
				return new Point[]{pair.a, pair.b};
			} else if (setLoop(pair.b, pair.a)){
				return new Point[]{pair.b, pair.a};
			}
		}

		return null;
	}

	private Point[] mend(Point[] sp){
		List<Pair<Point, Point>> candidates = new ArrayList<>();
		for (int x = 0; x < this.width; x++){
			for (int y = 0; y < this.height; y++){
				Point pt = new Point(x, y);
				int dx = this.map[x][y];
				boolean lx = this.currentLoop.contains(pt);
				if (dx == 8){
					Point cx = new Point(x+1, y-1);
					boolean rx = this.currentLoop.contains(cx);
					if (isInside(cx) && this.map[cx.x][cx.y] == 2 && rx != lx){
						candidates.add(new Pair<>(pt, cx));
					}
				} else if (dx == 2){
					Point cx = new Point(x+1, y+1);
					boolean rx = this.currentLoop.contains(cx);
					if (isInside(cx) && this.map[cx.x][cx.y] == 8 && rx != lx){
						candidates.add(new Pair<>(pt, cx));
					}
				} else if (dx == 4){
					Point cx = new Point(x+1, y+1);
					boolean rx = this.currentLoop.contains(cx);
					if (isInside(cx) && this.map[cx.x][cx.y] == 1 && rx != lx){
						candidates.add(new Pair<>(pt, cx));
					}
				} else if (dx == 1){
					Point cx = new Point(x-1, y+1);
					boolean rx = this.currentLoop.contains(cx);
					if (isInside(cx) && this.map[cx.x][cx.y] == 4 && rx != lx){
						candidates.add(new Pair<>(pt, cx));
					}
				}
			}
		}

		if (candidates.contains(new Pair<>(sp[0], sp[1]))){
			candidates.remove(new Pair<>(sp[0], sp[1]));
		}

		if (candidates.size() > 0){
			Random random = new Random();
			Pair<Point, Point> pair = candidates.get(random.nextInt(candidates.size()));
			return new Point[]{pair.a, pair.b};
		} else {
			return sp;
		}
	}

	private void buildInitialMap(){
		if (this.width % 2 == 0 || this.height % 2 == 0){
			for (int x = 0; x < this.width; x++){
				for (int y = 0; y < this.height; y++){
					map[x][y] = getZigZag(x, y);
				}
			}

			if (this.height % 2 == 0){
				this.startPoint = new Point(0, this.height-1);
			} else if (this.width % 2 == 0){
				this.startPoint = new Point(this.width-1, 1);
			}
		} else {
			throw new IllegalStateException("Hamiltonian cycle can't be generated");
		}
	}

	private boolean isInside(Point p){
		return p.x >= 0 && p.y >= 0 && p.x < this.width && p.y < this.height;
	}

	private int getZigZag(int x, int y){
		if (this.height % 2 == 0){
			if ((x == 1 && y % 2 == 0 && y > 0) || (x == this.width-1 && y % 2 == 1)){
				return 8;
			}
			if (x == 0){
				if (y == 0){
					return 6;
				} else if (y != this.height-1){
					return 2;
				}
			}
			return y % 2 == 0 ? 1 : 4;
		} else if (this.width % 2 == 0){
			if ((y == 1 && x % 2 == 1) || (y == this.height-1 && x % 2 == 0)){
				return 4;
			}
			if (y == 0){
				if (x == 0){
					return 2;
				} else {
					return 1;
				}
			}
			return x % 2 == 0 ? 2 : 8;
		} else {
			throw new IllegalStateException();
		}
	}

	public void print(){
		StringBuilder builder = new StringBuilder();
		for (int y = 0; y < this.height; y++){
			for (int x = 0; x < this.width; x++){
				if ((this.map[x][y] & 8) == 8){
					builder.append(" v");
				} else if (y > 0 && (this.map[x][y-1] & 2) == 2){
					builder.append(" ^");
				} else {
					builder.append("  ");
				}
			}
			builder.append("\n");
			for (int x = 0; x < this.width; x++){
				String sym = this.startPoint.equals(new Point(x, y)) ? "S" : "O";
				if ((this.map[x][y] & 1) == 1){
					builder.append(">"+sym);
				} else if (x > 0 && (this.map[x-1][y] & 4) == 4){
					builder.append("<"+sym);
				} else {
					builder.append(" "+sym);
				}
			}
			builder.append("\n");
		}

		System.out.println(builder.toString());
	}

	public void render(GraphicsContext gc, int size){
		gc.save();
		gc.setLineWidth(3);
		for (int x = 0; x < this.width; x++){
			for (int y = 0; y < this.height; y++){
				int dx = this.map[x][y];
				if ((dx & 8) == 8){
					gc.setStroke(Color.WHITE);
					gc.strokeLine((x+0.5)*size, (y+0.5)*size, (x+0.5)*size, y*size);
				}
				if ((dx & 4) == 4){
					gc.setStroke(Color.RED);
					gc.strokeLine((x+0.5)*size, (y+0.5)*size, (x+1)*size, (y+0.5)*size);
				}
				if ((dx & 2) == 2){
					gc.setStroke(Color.GREEN);
					gc.strokeLine((x+0.5)*size, (y+0.5)*size, (x+0.5)*size, (y+1)*size);
				}
				if ((dx & 1) == 1){
					gc.setStroke(Color.BLUE);
					gc.strokeLine((x+0.5)*size, (y+0.5)*size, x*size, (y+0.5)*size);
				}
			}
		}
		gc.restore();
	}
}
