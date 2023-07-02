package com.orangomango.snake;

import javafx.scene.image.Image;

import dev.webfx.platform.audio.Audio;
import dev.webfx.platform.audio.AudioService;
import dev.webfx.platform.resource.Resource;

public final class WebfxUtil{
    public static Audio loadAudio(String audio){
		return AudioService.loadSound(Resource.toUrl(audio, WebfxUtil.class));
	}

    public static Image loadImage(String image){
		return new Image(Resource.toUrl(image, WebfxUtil.class), true);
	}
}