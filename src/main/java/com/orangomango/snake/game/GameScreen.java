package com.orangomango.snake.game;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.animation.*;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;
import javafx.geometry.Side;

import java.util.*;

public class GameScreen{
	private static final int WIDTH = 720;
	private static final int HEIGHT = 480;
	private int frames, fps;
	private static final int FPS = 40;
	
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
	private boolean ai;
	
	public GameScreen(int size, int timeInterval, boolean ai){
		Thread counter = new Thread(() -> {
			while (true){
				try {
					this.fps = Math.min(this.frames, FPS);
					this.frames = 0;
					Thread.sleep(1000);
				} catch (InterruptedException ex){
					ex.printStackTrace();
				}
			}
		});
		counter.setDaemon(true);
		counter.start();
		
		this.timeInterval = timeInterval;
		this.ai = ai;
		SnakeBody.SIZE = Apple.SIZE = size;
	}
	
	public Scene getScene(){
		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(WIDTH, HEIGHT);
		canvas.setFocusTraversable(true);
		canvas.setOnKeyPressed(e -> this.keys.put(e.getCode(), true));
		canvas.setOnKeyReleased(e -> this.keys.put(e.getCode(), false));
		GraphicsContext gc = canvas.getGraphicsContext2D();
		pane.getChildren().add(canvas);
		
		Timeline loop = new Timeline(new KeyFrame(Duration.millis(1000.0/FPS), e -> update(gc)));
		loop.setCycleCount(Animation.INDEFINITE);
		loop.play();
		
		this.gameWorld = new GameWorld(WIDTH/SnakeBody.SIZE, HEIGHT/SnakeBody.SIZE);
		snake.add(new SnakeBody(6, 5));
		snake.add(new SnakeBody(5, 5));
		generateApple();
		
		AnimationTimer timer = new AnimationTimer(){
			@Override
			public void handle(long time){
				GameScreen.this.frames++;
			}
		};
		timer.start();
		
		Thread gameThread = new Thread(() -> {
			while (true){
				try {
					if (this.apple == null) continue;
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
					
					if (next.x == apple.x && next.y == apple.y){
						this.score++;
						generateApple();
					} else if (!dead){
						snake.remove(snake.size()-1);
					}
					next.wrap(WIDTH/SnakeBody.SIZE, HEIGHT/SnakeBody.SIZE);
					
					if (dead){
						Thread.sleep(5000);
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
		
		return new Scene(pane, WIDTH, HEIGHT);
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
		if (this.score > this.highscore) this.highscore = this.score;
		this.score = 0;
		generateApple();
	}
	
	private void generateApple(){
		Apple apple = new Apple(random.nextInt(WIDTH/(int)Apple.SIZE), random.nextInt(HEIGHT/(int)Apple.SIZE));
		for (int i = 0; i < this.snake.size(); i++){
			SnakeBody sb = this.snake.get(i);
			if (sb.x == apple.x && sb.y == apple.y){
				generateApple();
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
		}
		
		for (int i = 0; i < this.snake.size(); i++){
			SnakeBody sb = this.snake.get(i);
			sb.render(gc, i == 0);
			this.gameWorld.set(sb.x, sb.y);
		}
		this.apple.render(gc);

		if (this.ai){
			SnakeBody head = snake.get(0);
			SnakeBody next = getNext(head);
			PathFinder pf = new PathFinder(this.gameWorld, head.x, head.y, apple.x, apple.y);
			//pf.render(gc, false);
			Cell cell = pf.iterator().hasNext() ? pf.iterator().next() : null;
			if (cell != null){
				setDirection(cell, head);
			}
		}
		
		gc.setFill(Color.WHITE);
		if (this.showInfo) gc.fillText(String.format("FPS: %d, Snake direction: %s, Dir: %s", fps, this.snakeDirection, this.direction), 30, 55);
		gc.save();
		gc.setFont(new Font("Sans-serif", 20));
		gc.fillText(String.format("Score: %d, Highscore: %d", this.score, this.highscore), 30, 40);
		gc.restore();
	}
}
