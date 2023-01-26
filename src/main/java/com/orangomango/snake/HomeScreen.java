package com.orangomango.snake;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.animation.*;
import javafx.util.Duration;

import com.orangomango.snake.game.GameScreen;

public class HomeScreen{
	private static final int WIDTH = 720;
	private static final int HEIGHT = 480;
	private static final int FPS = 40;
	
	private Stage stage;
	
	public HomeScreen(Stage stage){
		this.stage = stage;
	}
	
	public Scene getScene(){
		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(WIDTH, HEIGHT);
		canvas.setOnMousePressed(e -> {
			boolean ai = false;
			GameScreen gs = new GameScreen(25, 200, ai);
			this.stage.setScene(gs.getScene());
		});
		GraphicsContext gc = canvas.getGraphicsContext2D();
		pane.getChildren().add(canvas);
		
		Timeline loop = new Timeline(new KeyFrame(Duration.millis(1000.0/FPS), e -> update(gc)));
		loop.setCycleCount(Animation.INDEFINITE);
		loop.play();
		
		return new Scene(pane, WIDTH, HEIGHT);
	}
	
	private void update(GraphicsContext gc){
		gc.clearRect(0, 0, WIDTH, HEIGHT);
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, WIDTH, HEIGHT);
	}
}
