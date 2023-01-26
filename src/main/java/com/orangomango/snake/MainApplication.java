package com.orangomango.snake;

import javafx.application.Application;
import javafx.stage.Stage;

public class MainApplication extends Application{
	@Override
	public void start(Stage stage){
		stage.setTitle("Snake");

		HomeScreen hs = new HomeScreen(stage);
		
		stage.setResizable(false);
		stage.setScene(hs.getScene());
		stage.show();
	}
	
	public static void main(String[] args){
		launch(args);
	}
}
