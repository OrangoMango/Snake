// File managed by WebFX (DO NOT EDIT MANUALLY)

module websnake.application {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.graphics;
    requires webfx.platform.audio;
    requires webfx.platform.resource;

    // Exported packages
    exports com.orangomango.snake;
    exports com.orangomango.snake.game;

    // Resources packages
    opens com.orangomango.snake;

    // Provided services
    provides javafx.application.Application with com.orangomango.snake.MainApplication;

}