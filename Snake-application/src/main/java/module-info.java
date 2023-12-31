// File managed by WebFX (DO NOT EDIT MANUALLY)

module Snake.application {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.media;
    requires webfx.platform.resource;
    requires webfx.platform.scheduler;

    // Exported packages
    exports com.orangomango.snake;
    exports com.orangomango.snake.game;
    exports com.orangomango.snake.game.ai;

    // Resources packages
    opens audio;
    opens images;

    // Provided services
    provides javafx.application.Application with com.orangomango.snake.MainApplication;

}