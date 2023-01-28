package com.orangomango.snake;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.geometry.Rectangle2D;

import java.util.*;

import com.orangomango.snake.game.GameScreen;

public class HomeScreen{
	public static final int WIDTH = 720;
	public static final int HEIGHT = 480;
	private static final int FPS = 40;
	private static final Image PLAY_BUTTON = new Image(HomeScreen.class.getResourceAsStream("/play_button.png"));
	
	private Stage stage;
	private Rectangle2D playButton;
	private List<Slider> sliders = new ArrayList<>();
	private List<Checkbox> checkboxes = new ArrayList<>();
	
	public HomeScreen(Stage stage){
		this.stage = stage;
		this.playButton = new Rectangle2D(0.15*WIDTH, 0.3*HEIGHT, 0.3*WIDTH, 0.15*HEIGHT);
	}
	
	public Scene getScene(){
		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(WIDTH, HEIGHT);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, WIDTH, HEIGHT);
		pane.getChildren().add(canvas);
		
		this.sliders.add(new Slider(0.57*WIDTH, 0.2*HEIGHT, 5, 60, 25));
		this.sliders.add(new Slider(0.57*WIDTH, 0.38*HEIGHT, 45, 500, 150));
		
		this.checkboxes.add(new Checkbox(0.43*WIDTH, 0.6*HEIGHT, "AI (F2 to toggle)"));
		this.checkboxes.add(new Checkbox(0.43*WIDTH, 0.75*HEIGHT, "Wrapping"));
		
		Timeline loop = new Timeline(new KeyFrame(Duration.millis(1000.0/FPS), e -> update(gc)));
		canvas.setOnMousePressed(e -> {
			if (this.playButton.contains(e.getX(), e.getY())){
				loop.stop();
				boolean ai = false;
				GameScreen gs = new GameScreen(this.stage, this.sliders.get(0).getValue(), this.sliders.get(1).getValue(), this.checkboxes.get(0).isSelected(), this.checkboxes.get(1).isSelected());
				this.stage.setScene(gs.getScene());
				MainApplication.playSound("gui");
			} else {
				for (Checkbox box : this.checkboxes){
					if (box.contains(e.getX(), e.getY())){
						box.toggle();
						MainApplication.playSound("gui");
					}
				}
			}
		});
		canvas.setOnMouseDragged(e -> {
			for (Slider slider : this.sliders){
				double cursor = slider.contains(e.getX(), e.getY());
				if (cursor >= 0){
					slider.updateCursor(cursor);
				}
			}
		});
		canvas.setOnMouseReleased(e -> {
			for (Slider slider : this.sliders){
				double cursor = slider.contains(e.getX(), e.getY());
				if (cursor >= 0){
					MainApplication.playSound("gui");
				}
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
		
		//gc.setStroke(Color.WHITE);
		gc.drawImage(PLAY_BUTTON, this.playButton.getMinX(), this.playButton.getMinY(), this.playButton.getWidth(), this.playButton.getHeight());
		
		for (Slider slider : this.sliders){
			slider.render(gc);
		}
		
		for (Checkbox box : this.checkboxes){
			box.render(gc);
		}
	}
}
