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

public class GameScreen{
	private static final int WIDTH = MainApplication.WIDTH;
	private static final int HEIGHT = MainApplication.HEIGHT;
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
	private boolean showInfo = false;
	private boolean ai, wrap;
	private boolean threadRunning = true;
	private Timeline loop, gameThread;
	private Apple targetCell;
	
	public GameScreen(Stage stage, int size, int timeInterval, boolean ai, boolean wrap){
		/*Thread counter = new Thread(() -> {
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
		counter.start();*/
		
		this.stage = stage;
		this.timeInterval = timeInterval;
		this.ai = ai;
		this.wrap = wrap;
		SnakeBody.SIZE = Apple.SIZE = size;
	}
	
	public Scene getScene(){
		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(WIDTH-WIDTH%SnakeBody.SIZE, HEIGHT-HEIGHT%SnakeBody.SIZE);
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
		
		/*AnimationTimer timer = new AnimationTimer(){
			@Override
			public void handle(long time){
				GameScreen.this.frames++;
			}
		};
		timer.start();*/
		
		gameThread = new Timeline(new KeyFrame(Duration.millis(this.timeInterval), e -> {
			if (this.apple == null) return;
			if (!this.threadRunning){
				gameThread.stop();
				return;
			}
			SnakeBody head = snake.get(0);				
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
			
			if (next.x == apple.x && next.y == apple.y){
				this.score++;
				MainApplication.playSound("point");
				generateApple(next);
			} else if (!dead){
				snake.remove(snake.size()-1);
			}
			
			if (dead){
				MainApplication.playSound("gameover");
				//Thread.sleep(1000);
				System.out.println("GAME OVER: "+this.score);
				resetGame();
			} else{
				snake.add(0, next);
				this.snakeDirection = this.direction;
			}
		}));
		gameThread.setCycleCount(Animation.INDEFINITE);
		gameThread.play();
		
		MainApplication.playSound("gameStart");
		Scene scene = new Scene(pane, WIDTH, HEIGHT);
		scene.setOnKeyPressed(e -> this.keys.put(e.getCode(), true));
		scene.setOnKeyReleased(e -> this.keys.put(e.getCode(), false));
		return scene;
	}
	
	private SnakeBody getNext(SnakeBody head){
		SnakeBody next = null;
		switch (this.direction){
			case TOP:
				next = new SnakeBody(head.x, head.y-1);
				break;
			case BOTTOM:
				next = new SnakeBody(head.x, head.y+1);
				break;
			case LEFT:
				next = new SnakeBody(head.x-1, head.y);
				break;
			case RIGHT:
				next = new SnakeBody(head.x+1, head.y);
				break;
		}
		return next;
	}
	
	private void resetGame(){
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
				generateApple(next);
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
		gc.setFill(Color.BLACK);
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
		}
		
		for (int i = 0; i < this.snake.size(); i++){
			SnakeBody sb = this.snake.get(i);
			sb.render(gc, (double)i/this.snake.size());
			this.gameWorld.set(sb.x, sb.y);
		}
		this.apple.render(gc);

		if (this.ai){
			SnakeBody head = snake.get(0);
			SnakeBody nextMove = getNext(head);
			PathFinder pf = new PathFinder(this.gameWorld, head.x, head.y, apple.x, apple.y);
			//pf.render(gc, false);
			Cell cell = pf.iterator().hasNext() ? pf.iterator().next() : null;
			if (cell != null){
				//if (targetCell != null) this.timeInterval /= 2;
				targetCell = null;
				setDirection(cell, head);
			} else if (targetCell == null || (targetCell != null && nextMove.x == targetCell.x && nextMove.y == targetCell.y)){
				List<Apple> cells = getCells(head.x, head.y);
				Cell next = null;
				int i = 0;
				do {
					targetCell = cells.get(i);
					pf = new PathFinder(this.gameWorld, head.x, head.y, targetCell.x, targetCell.y);
					next = pf.iterator().hasNext() ? pf.iterator().next() : null;
					i++;
				} while (next == null && i < cells.size());
				//this.timeInterval *= 2;
				if (next != null) setDirection(next, head);
			} else {
				pf = new PathFinder(this.gameWorld, head.x, head.y, targetCell.x, targetCell.y);
				//pf.render(gc, false);
				Cell next = pf.iterator().hasNext() ? pf.iterator().next() : null;
				if (next != null) setDirection(next, head);
			}
		}
		
		gc.setFill(Color.WHITE);
		//if (this.showInfo) gc.fillText(String.format("FPS: %d, Snake direction: %s, Dir: %s", fps, this.snakeDirection, this.direction), 30, 55);
		gc.save();
		gc.setFont(new Font("Sans-serif", 20));
		gc.fillText("Score: "+this.score+", Highscore: "+this.highscore+(this.ai ? " | AI" : ""), 30, 40);
		gc.restore();
	}
}
