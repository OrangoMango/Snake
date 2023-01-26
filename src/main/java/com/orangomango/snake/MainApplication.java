package com.orangomango.snake;

import javafx.application.Application;
import javafx.stage.Stage;

import com.orangomango.snake.game.GameScreen;

public class MainApplication extends Application{
	@Override
	public void start(Stage stage){
		stage.setTitle("Snake");
		
		boolean ai = false;
		
		GameScreen gs = new GameScreen(25, ai ? 130 : 200, ai);
		
		stage.setResizable(false);
		stage.setScene(gs.getScene());
		stage.show();
	}
	
	public static void main(String[] args){
		launch(args);
	}
}
