package com.orangomango.snake;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.media.*;

import java.util.*;

/**
 * Perfect snake AI made in Java/JavaFX
 * @author OrangoMango [https://orangomango.github.io]
 * @version 2.0
 */
public class MainApplication extends Application{
	private static Map<String, Media> sounds = new HashMap<>();
	
	static{
		sounds.put("gameStart", new Media(MainApplication.class.getResource("/gameStart.wav").toString()));
		sounds.put("gameover", new Media(MainApplication.class.getResource("/gameover.wav").toString()));
		sounds.put("gui", new Media(MainApplication.class.getResource("/gui.wav").toString()));
		sounds.put("highscore", new Media(MainApplication.class.getResource("/highscore.wav").toString()));
		sounds.put("point", new Media(MainApplication.class.getResource("/point.mp3").toString()));
	}
	
	@Override
	public void start(Stage stage){
		stage.setTitle("Snake");

		HomeScreen hs = new HomeScreen(stage);
		
		stage.setResizable(false);
		stage.setScene(hs.getScene());
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
		stage.show();
	}
	
	public static void playSound(String sound){
		new Thread(() -> {
			AudioClip player = new AudioClip(sounds.get(sound).getSource());
			player.play();
		}).start();
	}
	
	public static void main(String[] args){
		launch(args);
	}
}
