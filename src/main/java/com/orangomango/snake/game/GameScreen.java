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
import com.orangomango.snake.game.cycle.Cycle;

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
			while (this.threadRunning){
				try {
					if (this.apple == null) continue;
					SnakeBody head = snake.get(0);
					SnakeBody next = getNext(head);

					if (this.ai){
						SnakeBody tail = snake.get(snake.size()-1);
						Cycle cycle = this.gameWorld.getCycle();
						Cell cell = cycle.getNextCell(head.x, head.y, this.snakeDirection);
						final boolean scoreAcceptable = this.snake.size() < this.gameWorld.getWidth()*this.gameWorld.getHeight()*0.4;

						if (!scoreAcceptable){
							System.out.println("\n\nSCORE CAP REACHED\n\n");
							Thread.sleep(5000);
						}

						// Pathfinding algorithm
						PathFinder pf = new PathFinder(this.gameWorld, head.x, head.y, apple.x, apple.y, head, tail);
						Cell foundCell = pf.getNextCell();

						if (foundCell != null && scoreAcceptable){
							setDirection(foundCell, head);
							System.out.format("A* says to %d %d\n", foundCell.getX(), foundCell.getY());
							//Thread.sleep(1000);
						} else if (cell != null){
							setDirection(cell, head);
							System.out.println("H");
							//Thread.sleep(2500);
						}

						if (foundCell == null && cell == null){
							System.out.println("Both null");
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
						snake.remove(snake.size()-1);
					}
					
					if (dead){
						MainApplication.playSound("gameover");
						Thread.sleep(25000); // Temp (1000ms)
						System.out.println("GAME OVER: "+this.score);
						resetGame();
					} else{
						snake.add(0, next);
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
		this.gameWorld = new GameWorld(WIDTH/SnakeBody.SIZE, HEIGHT/SnakeBody.SIZE);
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
		generateApple(getNext(snake.get(0)));
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
		
		for (int i = this.snake.size()-1; i >= 0; i--){
			SnakeBody sb = this.snake.get(i);
			sb.render(gc, i == 0, i == this.snake.size()-1 ? null : this.snake.get(i+1), i == 0 ? null : this.snake.get(i-1));
			this.gameWorld.set(sb.x, sb.y);
		}
		this.apple.render(gc);

		if (this.showInfo) this.gameWorld.getCycle().render(gc, SnakeBody.SIZE);
		if (this.showAStar){
			SnakeBody head = this.snake.get(0);
			SnakeBody tail = this.snake.get(this.snake.size()-1);
			PathFinder pf = new PathFinder(this.gameWorld, head.x, head.y, apple.x, apple.y, head, tail);
			pf.render(gc, false);
		}
		
		gc.setFill(Color.BLACK);
		if (this.showInfo) gc.fillText(String.format("FPS: %d, Snake direction: %s, Dir: %s", fps, this.snakeDirection, this.direction), 30, 55);
		gc.save();
		gc.setFont(new Font("Sans-serif", 20));
		gc.fillText(String.format("Score: %d, Highscore: %d"+(this.ai ? " | AI" : ""), this.score, this.highscore), 30, 40);
		gc.restore();
	}
}
