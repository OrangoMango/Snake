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
import com.orangomango.snake.game.ai.*;

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
	private Map<KeyCode, Boolean> keys = new HashMap<>();
	private int score = 0, highscore;
	private GameWorld gameWorld;
	private int timeInterval;
	private boolean showInfo = false;
	private boolean ai, wrap;
	private boolean threadRunning = true;
	private volatile boolean paused = false;
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
					if (this.apple == null || this.paused) continue;
					SnakeBody head = this.snake.get(0);

					if (this.ai){
						Point bestPoint = getBestPoint(head);
						final Cycle cycle = this.gameWorld.getCycle();
						final Point nextPoint = cycle.getNextPoint(head.x, head.y);
						if ((bestPoint.equals(nextPoint) && this.snake.stream().filter(sb -> sb.x == bestPoint.x && sb.y == bestPoint.y).findAny().isEmpty()) || isSafe(bestPoint)){
							setDirection(bestPoint, head);
						} else {
							setDirection(nextPoint, head);
						}
					}
					
					SnakeBody next = getNext(head);
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

					synchronized (this){
						if (next.x == this.apple.x && next.y == this.apple.y){
							this.score++;
							MainApplication.playSound("point");
							generateApple(next);
						} else if (!dead){
							this.snake.remove(this.snake.size()-1);
						}
						
						if (dead){
							MainApplication.playSound("gameover");
							System.out.println("GAME OVER: "+this.score);
							Thread.sleep(2500);
							resetGame();
						} else{
							this.snake.add(0, next);
						}
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

	private boolean isSafe(Point point){
		List<SnakeBody> temp = new ArrayList<>(this.snake);
		if (temp.stream().filter(sb -> sb.x == point.x && sb.y == point.y).findAny().isEmpty()){
			temp.remove(temp.size()-1);
			temp.add(0, new SnakeBody(point.x, point.y));
			for (int i = 0; i < this.snake.size()+5; i++){
				SnakeBody pseudoHead = temp.get(0);
				Point pseudoPoint = this.gameWorld.getCycle().getNextPoint(pseudoHead.x, pseudoHead.y);
				if (temp.stream().filter(sb -> sb.x == pseudoPoint.x && sb.y == pseudoPoint.y).findAny().isPresent()){
					return false;
				} else {
					temp.remove(temp.size()-1);
					temp.add(0, new SnakeBody(pseudoPoint.x, pseudoPoint.y));
				}
			}
			return true;
		} else {
			return false;
		}
	}

	private Point getBestPoint(SnakeBody head){
		int[][] dirs = new int[][]{{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
		Point[] options = new Point[4];
		for (int i = 0; i < 4; i++){
			Point newPoint = new Point(head.x+dirs[i][0], head.y+dirs[i][1]);
			options[i] = this.gameWorld.isInsideMap(newPoint.x, newPoint.y) ? newPoint : null;
		}

		final Cycle cycle = this.gameWorld.getCycle();
		final int appleIndex = cycle.getIndex(this.apple.x, this.apple.y);
		int minDistance = Integer.MAX_VALUE;
		Point bestPoint = null;
		for (int i = 0; i < 4; i++){
			Point opt = options[i];
			if (opt != null){
				int distance = cycle.getCost(cycle.getIndex(opt.x, opt.y), appleIndex);
				if (distance < minDistance){
					minDistance = distance;
					bestPoint = opt;
				}
			}
		}

		return bestPoint;
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
		if (this.score > this.highscore){
			this.highscore = this.score;
			MainApplication.playSound("highscore");
		}
		this.score = 0;
		MainApplication.playSound("gameStart");
		generateApple(getNext(snake.get(0)));
	}
	
	private void generateApple(SnakeBody next){
		Apple apple = new Apple(random.nextInt(WIDTH/(int)Apple.SIZE), random.nextInt(HEIGHT/(int)Apple.SIZE));
		for (int i = 0; i < this.snake.size(); i++){
			SnakeBody sb = this.snake.get(i);
			if ((sb.x == apple.x && sb.y == apple.y) || (next.x == apple.x && next.y == apple.y)){
				if (this.snake.size() < this.gameWorld.getWidth()*this.gameWorld.getHeight()-1){
					generateApple(next);
					return;
				} else {
					apple = null;
					System.out.println("GAME OVER: "+this.score);
					break;
				}
			}
		}
		this.apple = apple;
	}
	
	private void setDirection(Point point, SnakeBody head){
		if (point.x > head.x && this.direction != Side.LEFT){
			this.direction = Side.RIGHT;
		}
		if (point.x < head.x && this.direction != Side.RIGHT){
			this.direction = Side.LEFT;
		}
		if (point.y > head.y && this.direction != Side.TOP){
			this.direction = Side.BOTTOM;
		}
		if (point.y < head.y && this.direction != Side.BOTTOM){
			this.direction = Side.TOP;
		}
	}
	
	private void update(GraphicsContext gc){
		gc.clearRect(0, 0, WIDTH, HEIGHT);
		gc.setFill(Color.web("#31FFB2"));
		gc.fillRect(0, 0, WIDTH, HEIGHT);
		
		if (keys.getOrDefault(KeyCode.UP, false) && this.direction != Side.BOTTOM){
			this.direction = Side.TOP;
			keys.put(KeyCode.UP, false);
		} else if (keys.getOrDefault(KeyCode.DOWN, false) && this.direction != Side.TOP){
			this.direction = Side.BOTTOM;
			keys.put(KeyCode.DOWN, false);
		} else if (keys.getOrDefault(KeyCode.RIGHT, false) && this.direction != Side.LEFT){
			this.direction = Side.RIGHT;
			keys.put(KeyCode.RIGHT, false);
		} else if (keys.getOrDefault(KeyCode.LEFT, false) && this.direction != Side.RIGHT){
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
		} else if (keys.getOrDefault(KeyCode.SPACE, false)){
			this.paused = !this.paused;
			keys.put(KeyCode.SPACE, false);
		}

		// DEBUG KEYS
		if (keys.getOrDefault(KeyCode.O, false)){
			this.timeInterval = Math.max(this.timeInterval-5, 0);
			keys.put(KeyCode.O, false);
		} else if (keys.getOrDefault(KeyCode.P, false)){
			this.timeInterval += 5;
			keys.put(KeyCode.P, false);
		}
		
		synchronized (this){
			for (int i = this.snake.size()-1; i >= 0; i--){
				SnakeBody sb = this.snake.get(i);
				sb.render(gc, i == 0, i == this.snake.size()-1 ? null : this.snake.get(i+1), i == 0 ? null : this.snake.get(i-1));
			}
		}

		if (this.apple != null) this.apple.render(gc);
		gc.setFill(Color.BLACK);
		if (this.showInfo){
			this.gameWorld.getCycle().render(gc, SnakeBody.SIZE);
			gc.fillText(String.format("FPS: %d, Direction: %s, TimeInterval: %d, Paused: %s", fps, this.direction, this.timeInterval, this.paused), 30, 55);
		}
		gc.save();
		gc.setFont(new Font("Sans-serif", 20));
		gc.fillText(String.format("Score: %d, Highscore: %d"+(this.ai ? " | AI" : ""), this.score, this.highscore), 30, 40);
		gc.restore();
	}
}
