package com.orangomango.snake.game;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.animation.*;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;
import javafx.geometry.Side;
import javafx.geometry.Point2D;

import java.util.*;

import com.orangomango.snake.HomeScreen;
import com.orangomango.snake.MainApplication;
import com.orangomango.snake.game.pathfinder.*;
import com.orangomango.snake.game.cycle.Point;

public class GameScreen{
	private static final int WIDTH = 720;
	private static final int HEIGHT = 480;
	private int frames, fps;
	private static final int FPS = 40;
	
	private Stage stage;
	private List<SnakeBody> snake = new ArrayList<>();
	private volatile Apple apple;
	private Random random = new Random();
	private volatile Side direction = Side.RIGHT;
	private volatile Side snakeDirection = Side.RIGHT;
	private Map<KeyCode, Boolean> keys = new HashMap<>();
	private int score = 0, highscore;
	private GameWorld gameWorld;
	private int timeInterval;
	private boolean showInfo = false, showAStar = false;
	private boolean ai, wrap;
	private boolean threadRunning = true;
	private Timeline loop;
	private PathFinder renderingPathFinder;
	
	public GameScreen(Stage stage, int size, int timeInterval, boolean ai, boolean wrap){
		Thread counter = new Thread(() -> {
			while (this.threadRunning){
				try {
					this.fps = this.frames;
					this.frames = 0;
					Thread.sleep(1000);
				} catch (InterruptedException ex){
					ex.printStackTrace();
				}
			}
		});
		counter.setDaemon(true);
		counter.start();
		
		this.stage = stage;
		this.timeInterval = timeInterval;
		this.ai = ai;
		this.wrap = wrap;
		SnakeBody.SIZE = Apple.SIZE = size;
	}
	
	public Scene getScene(){
		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(WIDTH-WIDTH%SnakeBody.SIZE, HEIGHT-HEIGHT%SnakeBody.SIZE);
		canvas.setFocusTraversable(true);
		canvas.setOnKeyPressed(e -> this.keys.put(e.getCode(), true));
		canvas.setOnKeyReleased(e -> this.keys.put(e.getCode(), false));
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, WIDTH, HEIGHT);
		pane.getChildren().add(canvas);
		
		this.loop = new Timeline(new KeyFrame(Duration.millis(1000.0/FPS), e -> update(gc)));
		this.loop.setCycleCount(Animation.INDEFINITE);
		this.loop.play();
		
		this.gameWorld = new GameWorld(WIDTH/SnakeBody.SIZE, HEIGHT/SnakeBody.SIZE);
		snake.add(new SnakeBody(6, 5));
		snake.add(new SnakeBody(5, 5));
		generateApple(getNext(snake.get(0)));
		
		AnimationTimer timer = new AnimationTimer(){
			@Override
			public void handle(long time){
				GameScreen.this.frames++;
			}
		};
		timer.start();
		
		Thread gameThread = new Thread(() -> {
			boolean debug = false;

			while (this.threadRunning){
				try {
					if (this.apple == null) continue;
					SnakeBody head = snake.get(0);
					SnakeBody next = getNext(head);

					if (this.ai){
						SnakeBody tail = snake.get(snake.size()-1);
						Cell cell = this.gameWorld.getCycle().getNextCell(head.x, head.y, this.snakeDirection);
						final boolean scoreAcceptable = this.snake.size() < this.gameWorld.getWidth()*this.gameWorld.getHeight()*0.3;

						if (!scoreAcceptable && !debug){
							System.out.println("\n\nSCORE CAP REACHED\n\n");
							Thread.sleep(5000);
							debug = true;
						}

						// Pathfinding algorithm
						PathFinder pf = new PathFinder(this.gameWorld, head.x, head.y, apple.x, apple.y, head, tail, true);
						this.renderingPathFinder = pf;
						Cell foundCell = pf.getNextCell();
						boolean specialPath = false;
						final int maxCellCount = (int)((this.gameWorld.getWidth()*this.gameWorld.getHeight()-this.snake.size())*0.75); // At least 75%
						int hamCount = 0;

						if (foundCell != null && scoreAcceptable){
							//Thread.sleep(1000);
							final int emptyCells = countEmptyCells(new Point(foundCell.getX(), foundCell.getY()));
							System.out.println("Cells: "+emptyCells+"/"+maxCellCount);
							if (emptyCells < maxCellCount && emptyCells < this.snake.size()){
								specialPath = true;
								//Thread.sleep(1500);
							} else {
								setDirection(foundCell, head);
								System.out.format("A* says to %d %d\n", foundCell.getX(), foundCell.getY());
							}
						} else if (cell != null){
							if (this.snake.stream().filter(sb -> sb.x == cell.getX() && sb.y == cell.getY()).findAny().isPresent()){
								System.out.println("\nTHIS IS A STUPID MOVE!\n");
								specialPath = true;
								//Thread.sleep(7000);
							} else {
								hamCount = countEmptyCells(new Point(cell.getX(), cell.getY()));
								System.out.println("Cells: "+hamCount+"/"+maxCellCount);
								if (hamCount < maxCellCount && hamCount < this.snake.size()){
									specialPath = true;
									//Thread.sleep(1500);
								} else {
									setDirection(cell, head);
									System.out.println("H");
								}
							}
							//Thread.sleep(2500);
						}

						if (foundCell == null && cell == null) specialPath = true;

						if (specialPath){
							System.out.println("Special path (both null or stupid move)");
							PathFinder pf2 = new PathFinder(this.gameWorld, head.x, head.y, apple.x, apple.y, head, tail, false);
							this.renderingPathFinder = pf2;
							foundCell = pf2.getNextCell();
							int cellCount = foundCell == null ? -1 : countEmptyCells(new Point(foundCell.getX(), foundCell.getY()));
							if (foundCell != null && (cellCount > maxCellCount || cellCount >= this.snake.size())){
								setDirection(foundCell, head);
							} else if (cell != null && hamCount >= this.snake.size() || hamCount > maxCellCount && this.snake.stream().filter(sb -> sb.x == cell.getX() && sb.y == cell.getY()).findAny().isEmpty()){
								setDirection(cell, head); // Hamiltonian path
							} else {
								// The snake must survive
								Cell availableCell = findAvailableCell(head);
								if (availableCell != null){
									setDirection(availableCell, head);
								}
							}
							//Thread.sleep(2000);
						}

						next = getNext(head);
					}

					boolean dead = false;
					for (int i = 0; i < snake.size(); i++){
						SnakeBody body = snake.get(i);
						if (head != body && head.x == body.x && head.y == body.y){
							dead = true;
							break;
						}
					}
					
					if (this.wrap){
						next.wrap(WIDTH/SnakeBody.SIZE, HEIGHT/SnakeBody.SIZE);
					} else if (next.outside(WIDTH/SnakeBody.SIZE, HEIGHT/SnakeBody.SIZE)){
						dead = true;
					}

					if (next.x == apple.x && next.y == apple.y){
						this.score++;
						MainApplication.playSound("point");
						generateApple(next);
					} else if (!dead){
						this.snake.remove(this.snake.size()-1);
					}
					
					if (dead){
						MainApplication.playSound("gameover");
						System.out.println("GAME OVER: "+this.score);
						Thread.sleep(50000); // Temp (1000ms)
						resetGame();
					} else{
						this.snake.add(0, next);
						this.snakeDirection = this.direction;
					}
					
					Thread.sleep(this.timeInterval);
				} catch (InterruptedException ex){
					ex.printStackTrace();
				}
			}
		});
		gameThread.setDaemon(true);
		gameThread.start();
		
		MainApplication.playSound("gameStart");
		Scene scene = new Scene(pane, WIDTH, HEIGHT);
		scene.setFill(Color.BLACK);
		return scene;
	}

	private Cell findAvailableCell(SnakeBody head){
		Cell n = this.gameWorld.isInsideMap(head.x, head.y-1) ? new Cell(head.x, head.y-1, false) : null;
		Cell e = this.gameWorld.isInsideMap(head.x+1, head.y) ? new Cell(head.x+1, head.y, false) : null;
		Cell s = this.gameWorld.isInsideMap(head.x, head.y+1) ? new Cell(head.x, head.y+1, false) : null;
		Cell w = this.gameWorld.isInsideMap(head.x-1, head.y) ? new Cell(head.x-1, head.y, false) : null;

		System.out.println("No other solution...");
		Cell bestCell = null;
		int emptyCells = Integer.MIN_VALUE;
		Cell[] cells = new Cell[]{n, e, s, w};
		for (int i = 0; i < 4; i++){
			Cell cell = cells[i];
			if (cell != null && this.snake.stream().filter(sb -> sb.x == cell.getX() && sb.y == cell.getY()).findAny().isEmpty()){
				int empty = countEmptyCells(new Point(cell.getX(), cell.getY()));
				if (empty > emptyCells){
					emptyCells = empty;
					bestCell = cell;
				}
			}
		}
		//try { Thread.sleep(2000); } catch (InterruptedException ex){}

		return bestCell;
	}

	private int countEmptyCells(Point start){
		List<Point> visited = new ArrayList<>();
		List<Point> tiles = new ArrayList<>();
		tiles.add(start);

		while (tiles.size() != 0){
			Point tile = tiles.remove(0);
			if (visited.contains(tile)) continue;
			visited.add(tile);

			Point n = this.gameWorld.isInsideMap(tile.x, tile.y-1) ? new Point(tile.x, tile.y-1) : null;
			Point e = this.gameWorld.isInsideMap(tile.x+1, tile.y) ? new Point(tile.x+1, tile.y) : null;
			Point s = this.gameWorld.isInsideMap(tile.x, tile.y+1) ? new Point(tile.x, tile.y+1) : null;
			Point w = this.gameWorld.isInsideMap(tile.x-1, tile.y) ? new Point(tile.x-1, tile.y) : null;

			if (n != null && !visited.contains(n) && this.snake.stream().filter(sb -> sb.x == n.x && sb.y == n.y).findAny().isEmpty()){
				tiles.add(n);
			}
			if (e != null && !visited.contains(e) && this.snake.stream().filter(sb -> sb.x == e.x && sb.y == e.y).findAny().isEmpty()){
				tiles.add(e);
			}
			if (s != null && !visited.contains(s) && this.snake.stream().filter(sb -> sb.x == s.x && sb.y == s.y).findAny().isEmpty()){
				tiles.add(s);
			}
			if (w != null && !visited.contains(w) && this.snake.stream().filter(sb -> sb.x == w.x && sb.y == w.y).findAny().isEmpty()){
				tiles.add(w);
			}

			/*try {
				for (int y = 0; y < this.gameWorld.getHeight(); y++){
					for (int x = 0; x < this.gameWorld.getWidth(); x++){
						final Point p = new Point(x, y);
						String str = visited.contains(p) ? "o" : ".";
						if (this.snake.stream().filter(sb -> sb.x == p.x && sb.y == p.y).findAny().isPresent()){
							str = "x";
						}
						System.out.print(str);
					}
					System.out.println();
				}
				System.out.println();
				Thread.sleep(100);
			} catch (InterruptedException ex){
				ex.printStackTrace();
			}*/
		}

		return visited.size();
	}
	
	private SnakeBody getNext(SnakeBody head){
		switch (this.direction){
			case TOP:
				return new SnakeBody(head.x, head.y-1);
			case BOTTOM:
				return new SnakeBody(head.x, head.y+1);
			case LEFT:
				return new SnakeBody(head.x-1, head.y);
			case RIGHT:
				return new SnakeBody(head.x+1, head.y);
			default:
				return null;
		}
	}
	
	private void resetGame(){
		/*this.gameWorld = new GameWorld(WIDTH/SnakeBody.SIZE, HEIGHT/SnakeBody.SIZE);
		snake.clear();
		snake.add(new SnakeBody(6, 5));
		snake.add(new SnakeBody(5, 5));
		this.direction = Side.RIGHT;
		this.snakeDirection = Side.RIGHT;
		if (this.score > this.highscore){
			this.highscore = this.score;
			MainApplication.playSound("highscore");
		}
		this.score = 0;
		MainApplication.playSound("gameStart");
		generateApple(getNext(snake.get(0)));*/
		System.exit(0);
	}
	
	private List<Apple> getCells(int x, int y){
		List<Apple> output = new ArrayList<>();
		for (int i = 0; i < WIDTH/(int)Apple.SIZE; i++){
			for (int j = 0; j < HEIGHT/(int)Apple.SIZE; j++){
				boolean ok = true;
				for (int k = 0; k < this.snake.size(); k++){
					SnakeBody sb = this.snake.get(k);
					if (sb.x == i && sb.y == j){
						ok = false;
						break;
					}
				}
				if (ok){
					output.add(new Apple(i, j));
				}
			}
		}
		Point2D start = new Point2D(x, y);
		output.sort((a, b) -> {
			double distance1 = start.distance(new Point2D(a.x, a.y));
			double distance2 = start.distance(new Point2D(b.x, b.y));
			return -Double.compare(distance1, distance2);
		});
		return output;
	}
	
	private void generateApple(SnakeBody next){
		Apple apple = new Apple(random.nextInt(WIDTH/(int)Apple.SIZE), random.nextInt(HEIGHT/(int)Apple.SIZE));
		for (int i = 0; i < this.snake.size(); i++){
			SnakeBody sb = this.snake.get(i);
			if ((sb.x == apple.x && sb.y == apple.y) || (next.x == apple.x && next.y == apple.y)){
				if (this.gameWorld.isBoardFull()){
					resetGame();
				} else {
					generateApple(next);
				}
				return;
			}
		}
		this.apple = apple;
	}
	
	private void setDirection(Cell cell, SnakeBody head){
		if (cell.getX() > head.x && this.snakeDirection != Side.LEFT){
			this.direction = Side.RIGHT;
		}
		if (cell.getX() < head.x && this.snakeDirection != Side.RIGHT){
			this.direction = Side.LEFT;
		}
		if (cell.getY() > head.y && this.snakeDirection != Side.TOP){
			this.direction = Side.BOTTOM;
		}
		if (cell.getY() < head.y && this.snakeDirection != Side.BOTTOM){
			this.direction = Side.TOP;
		}
	}
	
	private void update(GraphicsContext gc){
		gc.clearRect(0, 0, WIDTH, HEIGHT);
		this.gameWorld.clear();
		gc.setFill(Color.web("#31FFB2"));
		gc.fillRect(0, 0, WIDTH, HEIGHT);
		
		if (keys.getOrDefault(KeyCode.UP, false) && this.snakeDirection != Side.BOTTOM){
			this.direction = Side.TOP;
			keys.put(KeyCode.UP, false);
		} else if (keys.getOrDefault(KeyCode.DOWN, false) && this.snakeDirection != Side.TOP){
			this.direction = Side.BOTTOM;
			keys.put(KeyCode.DOWN, false);
		} else if (keys.getOrDefault(KeyCode.RIGHT, false) && this.snakeDirection != Side.LEFT){
			this.direction = Side.RIGHT;
			keys.put(KeyCode.RIGHT, false);
		} else if (keys.getOrDefault(KeyCode.LEFT, false) && this.snakeDirection != Side.RIGHT){
			this.direction = Side.LEFT;
			keys.put(KeyCode.LEFT, false);
		} else if (keys.getOrDefault(KeyCode.F1, false)){
			this.showInfo = !this.showInfo;
			keys.put(KeyCode.F1, false);
		} else if (keys.getOrDefault(KeyCode.F2, false)){
			this.ai = !this.ai;
			keys.put(KeyCode.F2, false);
		} else if (keys.getOrDefault(KeyCode.ESCAPE, false)){
			this.threadRunning = false;
			this.loop.stop();
			HomeScreen hs = new HomeScreen(this.stage);
			this.stage.setScene(hs.getScene());
			return;
		} else if (keys.getOrDefault(KeyCode.F3, false)){
			this.showAStar = !this.showAStar;
			keys.put(KeyCode.F3, false);
		}

		// DEBUG KEYS
		if (keys.getOrDefault(KeyCode.O, false)){
			this.timeInterval -= 5;
			keys.put(KeyCode.O, false);
		} else if (keys.getOrDefault(KeyCode.P, false)){
			this.timeInterval += 5;
			keys.put(KeyCode.P, false);
		}
		
		for (int i = this.snake.size()-1; i >= 0; i--){
			SnakeBody sb = this.snake.get(i);
			sb.render(gc, i == 0, i == this.snake.size()-1 ? null : this.snake.get(i+1), i == 0 ? null : this.snake.get(i-1));
			this.gameWorld.set(sb.x, sb.y);
		}
		this.apple.render(gc);

		if (this.showInfo) this.gameWorld.getCycle().render(gc, SnakeBody.SIZE);
		if (this.showAStar && this.renderingPathFinder != null){
			this.renderingPathFinder.render(gc, false);
		}
		
		gc.setFill(Color.BLACK);
		if (this.showInfo){
			SnakeBody head = this.snake.get(0);
			gc.fillText(String.format("FPS: %d, Snake direction: %s, Dir: %s, TimeInterval: %d", fps, this.snakeDirection, this.direction, this.timeInterval), 30, 55);
		}
		gc.save();
		gc.setFont(new Font("Sans-serif", 20));
		gc.fillText(String.format("Score: %d, Highscore: %d"+(this.ai ? " | AI" : ""), this.score, this.highscore), 30, 40);
		gc.restore();
	}
}
