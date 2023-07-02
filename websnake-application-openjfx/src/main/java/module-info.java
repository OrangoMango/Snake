// File managed by WebFX (DO NOT EDIT MANUALLY)

module websnake.application.openjfx {

    // Direct dependencies modules
    requires webfx.kit.openjfx;
    requires webfx.kit.platform.audio.openjfx.gwt;
    requires webfx.platform.boot.java;
    requires webfx.platform.console.java;
    requires webfx.platform.resource.java;
    requires webfx.platform.scheduler.java;
    requires webfx.platform.shutdown.java;
    requires websnake.application;

    // Meta Resource package
    opens dev.webfx.platform.meta.exe;

}