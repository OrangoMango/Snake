package com.orangomango.snake;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.media.*;

import java.util.*;

import dev.webfx.platform.resource.Resource;

/**
 * Perfect snake AI made in Java/JavaFX
 * @author OrangoMango [https://orangomango.github.io]
 * @version 2.0
 */
public class MainApplication extends Application{
	private static Map<String, AudioClip> sounds = new HashMap<>();
	
	static{
		sounds.put("gameStart", new AudioClip(Resource.toUrl("/audio/gameStart.wav", MainApplication.class)));
		sounds.put("gameover", new AudioClip(Resource.toUrl("/audio/gameover.wav", MainApplication.class)));
		sounds.put("gui", new AudioClip(Resource.toUrl("/audio/gui.wav", MainApplication.class)));
		sounds.put("highscore", new AudioClip(Resource.toUrl("/audio/highscore.wav", MainApplication.class)));
		sounds.put("point", new AudioClip(Resource.toUrl("/audio/point.mp3", MainApplication.class)));
	}
	
	@Override
	public void start(Stage stage){
		stage.setTitle("Snake");

		HomeScreen hs = new HomeScreen(stage);
		
		stage.setResizable(false);
		stage.setScene(hs.getScene());
		stage.getIcons().add(new Image(Resource.toUrl("/images/icon.png", MainApplication.class)));
		stage.show();
	}
	
	public static void playSound(String sound){
		sounds.get(sound).play();
	}
	
	public static void main(String[] args){
		launch(args);
	}
}
