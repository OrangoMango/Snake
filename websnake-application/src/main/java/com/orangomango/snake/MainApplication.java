package com.orangomango.snake;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.Screen;

import dev.webfx.platform.audio.Audio;

import java.util.*;

public class MainApplication extends Application{
	private static Map<String, Audio> sounds = new HashMap<>();
	public static final int WIDTH = (int)(Screen.getPrimary().getVisualBounds().getWidth()*0.85);
	public static final int HEIGHT = (int)(Screen.getPrimary().getVisualBounds().getHeight()*0.85);
	
	@Override
	public void start(Stage stage){
		stage.setTitle("Snake");

		sounds.put("gameStart", WebfxUtil.loadAudio("gameStart.wav"));
		sounds.put("gameover", WebfxUtil.loadAudio("gameover.wav"));
		sounds.put("gui", WebfxUtil.loadAudio("gui.wav"));
		sounds.put("highscore", WebfxUtil.loadAudio("highscore.wav"));
		sounds.put("point", WebfxUtil.loadAudio("point.mp3"));

		HomeScreen hs = new HomeScreen(stage);
		
		stage.setResizable(false);
		stage.setScene(hs.getScene());
		stage.show();
	}

	public static void playSound(String sound){
		Audio audio = sounds.get(sound);
		audio.play();
	}
	
	public static void main(String[] args){
		launch(args);
	}
}
