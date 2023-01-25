package com.orangomango.snake;

import javafx.application.Application;
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

import java.util.*;

public class MainApplication extends Application{
	private static final int WIDTH = 720;
	private static final int HEIGHT = 480;
	private int frames, fps;
	private static final int FPS = 60;
	
	private List<SnakeBody> snake = new ArrayList<>();
	private volatile Apple apple;
	private Random random = new Random();
	private volatile Side direction = Side.RIGHT;
	private volatile Side snakeDirection = Side.RIGHT;
	private Map<KeyCode, Boolean> keys = new HashMap<>();
	private int score = 0;
	private GameWorld gameWorld;
	
	@Override
	public void start(Stage stage){
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

		stage.setTitle("Snake");
		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(WIDTH, HEIGHT);
		canvas.setFocusTraversable(true);
		canvas.setOnKeyPressed(e -> this.keys.put(e.getCode(), true));
		canvas.setOnKeyReleased(e -> this.keys.put(e.getCode(), false));
		canvas.setOnMousePressed(e -> {
			this.apple = new Apple((int)e.getX()/SnakeBody.SIZE, (int)e.getY()/SnakeBody.SIZE);
		});
		GraphicsContext gc = canvas.getGraphicsContext2D();
		pane.getChildren().add(canvas);
		
		Timeline loop = new Timeline(new KeyFrame(Duration.millis(1000.0/FPS), e -> update(gc)));
		loop.setCycleCount(Animation.INDEFINITE);
		loop.play();
		
		this.gameWorld = new GameWorld(WIDTH/SnakeBody.SIZE, HEIGHT/SnakeBody.SIZE);
		//snake.add(new SnakeBody(8, 5));
		//snake.add(new SnakeBody(7, 5));
		snake.add(new SnakeBody(6, 5));
		snake.add(new SnakeBody(5, 5));
		generateApple();
		
		AnimationTimer timer = new AnimationTimer(){
			@Override
			public void handle(long time){
				MainApplication.this.frames++;
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
						Thread.sleep(10000);
						resetGame();
					} else{
						snake.add(0, next);
						this.snakeDirection = this.direction;
					}
					
					Thread.sleep(150);
				} catch (InterruptedException ex){
					ex.printStackTrace();
				}
			}
		});
		gameThread.setDaemon(true);
		gameThread.start();
		
		stage.setScene(new Scene(pane, WIDTH, HEIGHT));
		stage.show();
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
		this.score = 0;
		generateApple();
	}
	
	private void generateApple(){
		/*Apple apple = new Apple(random.nextInt(WIDTH/(int)Apple.SIZE), random.nextInt(HEIGHT/(int)Apple.SIZE));
		for (int i = 0; i < this.snake.size(); i++){
			SnakeBody sb = this.snake.get(i);
			if (sb.x == apple.x && sb.y == apple.y){
				generateApple();
				return;
			}
		}
		this.apple = apple;*/
		this.apple = null;
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
		} 
		
		for (int i = 0; i < this.snake.size(); i++){
			SnakeBody sb = this.snake.get(i);
			sb.render(gc, i == 0);
			this.gameWorld.set(sb.x, sb.y);
		}
		//if (this.apple != null) this.apple.render(gc);
		
		if (this.apple != null){
			SnakeBody head = snake.get(0);
			SnakeBody next = getNext(head);
			PathFinder pf = new PathFinder(this.gameWorld, head.x, head.y, apple.x, apple.y);
			pf.render(gc, false);
			for (Cell cell : pf){
				//System.out.println(cell);
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
				gc.setFill(Color.RED);
				gc.fillRect(cell.getX()*SnakeBody.SIZE, cell.getY()*SnakeBody.SIZE, SnakeBody.SIZE, SnakeBody.SIZE);
				break;
			}
		}
		
		gc.setFill(Color.WHITE);
		gc.fillText(String.format("FPS: %d, Snake direction: %s, Dir: %s", fps, this.snakeDirection, this.direction), 30, 55);
		gc.save();
		gc.setFont(new Font("Sans-serif", 20));
		gc.fillText(String.format("Score: %d", this.score), 30, 40);
		gc.restore();
	}
	
	public static void main(String[] args){
		launch(args);
	}
}
