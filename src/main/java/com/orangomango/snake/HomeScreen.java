package com.orangomango.snake;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.geometry.Rectangle2D;

import com.orangomango.snake.game.GameScreen;

public class HomeScreen{
	private static final int WIDTH = 720;
	private static final int HEIGHT = 480;
	private static final int FPS = 40;
	
	private Stage stage;
	private Rectangle2D playButton;
	
	public HomeScreen(Stage stage){
		this.stage = stage;
		this.playButton = new Rectangle2D(100, 150, 200, 75);
	}
	
	public Scene getScene(){
		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(WIDTH, HEIGHT);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, WIDTH, HEIGHT);
		pane.getChildren().add(canvas);
		
		Timeline loop = new Timeline(new KeyFrame(Duration.millis(1000.0/FPS), e -> update(gc)));
		canvas.setOnMousePressed(e -> {
			if (this.playButton.contains(e.getX(), e.getY())){
				loop.stop();
				boolean ai = false;
				GameScreen gs = new GameScreen(this.stage, 25, 200, ai, false);
				this.stage.setScene(gs.getScene());
			}
		});
		loop.setCycleCount(Animation.INDEFINITE);
		loop.play();
		
		return new Scene(pane, WIDTH, HEIGHT);
	}
	
	private void update(GraphicsContext gc){
		gc.clearRect(0, 0, WIDTH, HEIGHT);
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, WIDTH, HEIGHT);
		
		gc.setStroke(Color.WHITE);
		gc.strokeRect(this.playButton.getMinX(), this.playButton.getMinY(), this.playButton.getWidth(), this.playButton.getHeight());
	}
}
