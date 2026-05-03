// File managed by WebFX (DO NOT EDIT MANUALLY)

module Snake.application {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.media;
    requires webfx.kit.launcher;
    requires webfx.platform.ast;
    requires webfx.platform.async;
    requires webfx.platform.console;
    requires webfx.platform.fetch;
    requires webfx.platform.resource;
    requires webfx.platform.scheduler;
    requires webfx.platform.storage;

    // Exported packages
    exports com.orangomango.account;
    exports com.orangomango.snake;
    exports com.orangomango.snake.game;
    exports com.orangomango.snake.game.ai;
    exports com.orangomango.snake.ui;

    // Resources packages
    opens audio;
    opens fonts;
    opens images;

    // Provided services
    provides javafx.application.Application with com.orangomango.snake.MainApplication;

}