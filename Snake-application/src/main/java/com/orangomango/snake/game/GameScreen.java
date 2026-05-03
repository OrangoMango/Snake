package com.orangomango.snake.game;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.input.KeyCode;
import javafx.scene.effect.DropShadow;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.geometry.Side;
import javafx.geometry.Point2D;

import java.util.*;

import dev.webfx.platform.ast.AST;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.scheduler.Scheduler;

import com.orangomango.snake.HomeScreen;
import com.orangomango.snake.MainApplication;
import com.orangomango.snake.game.ai.*;
import com.orangomango.account.Account;

import static com.orangomango.snake.HomeScreen.WIDTH;
import static com.orangomango.snake.HomeScreen.HEIGHT;
import static com.orangomango.snake.ui.UiElement.FONT_SMALLSMALLSMALL;
import static com.orangomango.snake.ui.UiElement.FONT_SMALL;
import static com.orangomango.snake.ui.UiElement.FONT_LARGE;
import static com.orangomango.snake.HomeScreen.COLOR_EASY;
import static com.orangomango.snake.HomeScreen.COLOR_MEDIUM;
import static com.orangomango.snake.HomeScreen.COLOR_HARD;
import static com.orangomango.snake.HomeScreen.COLOR_EXTREME;

public class GameScreen{
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
	private volatile boolean paused = false, allowMovement = true, gameFinished = false;
	private Timeline loop;
	private int steps;
	private Account account;
	private String gameMode;
	
	public GameScreen(Stage stage, Account account, String gameMode, int size, int timeInterval, boolean ai, boolean wrap){
		this.stage = stage;
		this.timeInterval = timeInterval;
		this.ai = ai;
		this.wrap = wrap;
		SnakeBody.SIZE = Apple.SIZE = size;

		this.account = account;
		this.gameMode = gameMode;
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

		if (this.account != null && this.gameMode != null) {
			this.account.getAppData().onSuccess(data -> {
				this.highscore = data == null ? 0 : data.getInteger("high_" + this.gameMode, 0);
			});
		}
		
		this.loop = new Timeline(new KeyFrame(Duration.millis(1000.0/FPS), e -> update(gc)));
		this.loop.setCycleCount(Animation.INDEFINITE);
		this.loop.play();
		
		this.gameWorld = new GameWorld(WIDTH/SnakeBody.SIZE, HEIGHT/SnakeBody.SIZE, this.gameMode == null);
		snake.add(new SnakeBody(6, 5));
		snake.add(new SnakeBody(5, 5));
		generateApple(getNext(snake.get(0)));
		
		Scheduler.scheduleDelay(this.timeInterval, this::doGameLoop); // Game loop
		
		MainApplication.playSound("gameStart");
		Scene scene = new Scene(pane, WIDTH, HEIGHT);
		scene.setFill(Color.BLACK);
		return scene;
	}

	private void doGameLoop(){
		if (!this.paused){
			SnakeBody head = null;
			synchronized (this){
				head = this.snake.get(0);
			}

			if (this.ai){
				final Cycle cycle = this.gameWorld.getCycle();
				final Point nextPoint = cycle.getNextPoint(head.x, head.y);
				if (this.apple == null){
					setDirection(nextPoint, head); // The game has finished so just follow the tail
				} else {
					Point bestPoint = getBestPoint(head);
					if ((bestPoint.equals(nextPoint) && this.snake.stream().filter(sb -> sb.x == bestPoint.x && sb.y == bestPoint.y).findAny().isEmpty()) || isSafe(bestPoint)){
						setDirection(bestPoint, head);
					} else {
						setDirection(nextPoint, head);
					}
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
				if (this.apple != null && next.x == this.apple.x && next.y == this.apple.y){
					this.score++;
					MainApplication.playSound("point");
					generateApple(next);
				} else if (!dead){
					this.snake.remove(this.snake.size()-1);
				}
				
				if (dead){
					MainApplication.playSound("gameover");
					this.paused = true;
					this.gameFinished = true;

					if (this.account != null && this.gameMode != null && this.score > this.highscore){
						this.account.updateLeaderboard(this.gameMode, new int[]{this.score, -this.steps});
					}

					Scheduler.scheduleDelay(2500, this::resetGame);
				} else{
					this.snake.add(0, next);
				}
			}

			if (this.apple != null) this.steps++;
			this.allowMovement = true;
		}
		if (this.threadRunning) Scheduler.scheduleDelay(this.timeInterval, this::doGameLoop);
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
		GameWorld temp = new GameWorld(this.gameWorld.getWidth(), this.gameWorld.getHeight(), false);
		temp.setCycle(this.gameWorld.getCycle());
		this.gameWorld = temp;
		
		synchronized (this){
			this.snake.clear();
			this.snake.add(new SnakeBody(6, 5));
			this.snake.add(new SnakeBody(5, 5));
		}
		this.direction = Side.RIGHT;
		if (this.score > this.highscore){
			this.highscore = this.score;
			MainApplication.playSound("highscore");

			if (this.account != null && this.gameMode != null){
				this.account.getAppData().onSuccess(readData -> {
					AstObject data = readData == null ? AST.createObject() : AST.createObject(readData);
					data.set("high_" + this.gameMode, this.highscore);
					this.account.setAppData(data);
				});
			}
		}
		this.score = 0;
		this.steps = 0;
		this.gameFinished = false;
		MainApplication.playSound("gameStart");
		generateApple(getNext(snake.get(0)));
		this.paused = false;
	}
	
	private void generateApple(SnakeBody next){
		Apple apple = new Apple(random.nextInt(this.gameWorld.getWidth()), random.nextInt(this.gameWorld.getHeight()));
		for (int i = 0; i < this.snake.size(); i++){
			SnakeBody sb = this.snake.get(i);
			if ((sb.x == apple.x && sb.y == apple.y) || (next.x == apple.x && next.y == apple.y)){
				if (this.snake.size() < this.gameWorld.getWidth()*this.gameWorld.getHeight()-1){
					generateApple(next);
					return;
				} else {
					apple = null;
					Scheduler.scheduleDelay(6000, this::resetGame);
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

	private static String formatString(String text, Object... objects){
		String[] parts = text.split("%s");
		StringBuilder output = new StringBuilder();
		for (int i = 0; i < parts.length; i++){
			output.append(parts[i]);
			if (i < objects.length){
				output.append(objects[i].toString());
			}
		}

		return output.toString();
	}
	
	private void update(GraphicsContext gc){
		gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
		gc.setFill(Color.web("#020617"));
		gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

		gc.setFill(Color.web("#1e293b"));
		gc.setLineWidth(1.2);
		for (int i = 0; i < gc.getCanvas().getWidth(); i++){
			gc.strokeLine(i*SnakeBody.SIZE, 0, i*SnakeBody.SIZE, gc.getCanvas().getHeight());
			gc.strokeLine(0, i*SnakeBody.SIZE, gc.getCanvas().getWidth(), i*SnakeBody.SIZE);
		}
		
		if (this.allowMovement){
			if (keys.getOrDefault(KeyCode.UP, false) && this.direction != Side.BOTTOM){
				this.direction = Side.TOP;
				this.allowMovement = false;
				keys.put(KeyCode.UP, false);
			} else if (keys.getOrDefault(KeyCode.DOWN, false) && this.direction != Side.TOP){
				this.direction = Side.BOTTOM;
				this.allowMovement = false;
				keys.put(KeyCode.DOWN, false);
			} else if (keys.getOrDefault(KeyCode.RIGHT, false) && this.direction != Side.LEFT){
				this.direction = Side.RIGHT;
				this.allowMovement = false;
				keys.put(KeyCode.RIGHT, false);
			} else if (keys.getOrDefault(KeyCode.LEFT, false) && this.direction != Side.RIGHT){
				this.direction = Side.LEFT;
				this.allowMovement = false;
				keys.put(KeyCode.LEFT, false);
			}
		}

		// DEBUG KEYS
		if (this.gameMode == null){
			if (keys.getOrDefault(KeyCode.I, false)){
				this.showInfo = !this.showInfo;
				keys.put(KeyCode.I, false);
			} else if (keys.getOrDefault(KeyCode.H, false)){
				this.ai = !this.ai;
				keys.put(KeyCode.H, false);
			} else if (keys.getOrDefault(KeyCode.O, false)){
				this.timeInterval = Math.max(this.timeInterval-5, 5);
				keys.put(KeyCode.O, false);
			} else if (keys.getOrDefault(KeyCode.P, false)){
				this.timeInterval += 5;
				keys.put(KeyCode.P, false);
			}
		}

		if (keys.getOrDefault(KeyCode.ESCAPE, false)){
			this.threadRunning = false;
			this.loop.stop();
			HomeScreen hs = new HomeScreen(this.stage);
			this.stage.setScene(hs.getScene());
			return;
		} else if (keys.getOrDefault(KeyCode.SPACE, false)){
			this.paused = !this.paused;
			keys.put(KeyCode.SPACE, false);
		}
		
		synchronized (this){
			for (int i = this.snake.size()-1; i >= 0; i--){
				SnakeBody sb = this.snake.get(i);
				sb.render(gc, true, i == 0, i == this.snake.size()-1 ? null : this.snake.get(i+1), i == 0 ? null : this.snake.get(i-1));
			}

			// Draw a second time to avoid internal dropshadow effects
			for (int i = this.snake.size()-1; i >= 0; i--){
				SnakeBody sb = this.snake.get(i);
				sb.render(gc, false, i == 0, i == this.snake.size()-1 ? null : this.snake.get(i+1), i == 0 ? null : this.snake.get(i-1));
			}
		}

		if (this.apple != null) this.apple.render(gc);
		gc.setFill(Color.WHITE);
		if (this.showInfo){
			this.gameWorld.getCycle().render(gc, SnakeBody.SIZE);
			gc.fillText(formatString("Direction: %s, TimeInterval: %s, Paused: %s, Steps: %s", this.direction, this.timeInterval, this.paused, this.steps), 30, 55);
		}
		gc.save();
		gc.setFont(FONT_SMALL);
		gc.setEffect(new DropShadow(10, Color.WHITE));
		gc.fillText(formatString("Score: %s, Highscore: %s"+(this.ai ? " | AI" : ""), this.score, this.highscore), 30, 40);
		gc.setFont(FONT_SMALLSMALLSMALL);
		gc.fillText("O - Faster | P - Slower | I - Debug | H - AutoPlay | SPACE - Pause", 30, gc.getCanvas().getHeight()-30);

		if (this.gameMode != null){
			switch (this.gameMode){
				case "easy":
					gc.setFill(COLOR_EASY);
					gc.setEffect(new DropShadow(25, Color.web("#34d399")));
					break;
				case "medium":
					gc.setFill(COLOR_MEDIUM);
					gc.setEffect(new DropShadow(25, Color.web("#60a5fa")));
					break;
				case "hard":
					gc.setFill(COLOR_HARD);
					gc.setEffect(new DropShadow(25, Color.web("#f87171")));
					break;
				case "extreme":
					gc.setFill(COLOR_EXTREME);
					gc.setEffect(new DropShadow(25, Color.web("#c084fc")));
					break;
			}

			gc.setTextAlign(TextAlignment.RIGHT);
			gc.setFont(FONT_SMALL);
			gc.fillText(this.gameMode, gc.getCanvas().getWidth()-30, 40);
		}

		gc.restore();

		if (this.paused || this.gameFinished){
			gc.save();
			gc.setFill(Color.BLACK);
			gc.setGlobalAlpha(0.6);
			gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
			gc.setGlobalAlpha(1.0);
			gc.setFill(Color.WHITE);
			gc.setFont(FONT_LARGE);
			gc.setTextAlign(TextAlignment.CENTER);
			gc.fillText(this.gameFinished ? "GAMEOVER" : "GAME PAUSED", gc.getCanvas().getWidth()/2.0, gc.getCanvas().getHeight()/2.0);
			gc.restore();
		}
	}
}