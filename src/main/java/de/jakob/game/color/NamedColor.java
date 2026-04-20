package de.jakob.game.color;

public enum NamedColor implements Color {
    WHITE(255, 255, 255),
    BLACK(0, 0, 0),

    LIGHTEST_GRAY(245, 245, 245),
    LIGHT_GRAY(200, 200, 200),
    GRAY(128, 128, 128),
    DARK_GRAY(64, 64, 64),
    DARKEST_GRAY(25, 25, 25),

    RED(255, 0, 0),
    SOFT_RED(255, 120, 120),
    DARK_RED(150, 0, 0),

    GREEN(0, 200, 0),
    SOFT_GREEN(120, 220, 140),
    DARK_GREEN(0, 120, 0),

    BLUE(0, 120, 255),
    SOFT_BLUE(120, 170, 255),
    DARK_BLUE(0, 70, 160),

    YELLOW(255, 255, 0),
    GOLD(255, 215, 0),
    ORANGE(255, 165, 0),
    DARK_ORANGE(200, 120, 0),

    PURPLE(140, 0, 180),
    SOFT_PURPLE(190, 120, 255),
    PINK(255, 105, 180),

    CYAN(0, 255, 255),
    TEAL(0, 140, 140),

    BACKGROUND(36, 36, 36),
    LIGHT(120, 120, 120),
    ULTRA_LIGHT(250, 250, 250),

    SURFACE(60, 60, 60),
    LIGHT_SURFACE(240, 240, 240),

    PRIMARY(100, 150, 255),
    PRIMARY_DARK(70, 110, 200),

    SECONDARY(180, 180, 255),

    SUCCESS(120, 200, 140),
    SUCCESS_DARK(80, 160, 100),

    WARNING(255, 200, 100),
    WARNING_DARK(200, 150, 60),

    ERROR(255, 100, 100),
    ERROR_DARK(200, 60, 60);

    private final Color color;

    NamedColor(int r, int g, int b) {
        this.color = Color.fromRGB(r, g, b);
    }

    @Override
    public javafx.scene.paint.Color toFX() {
        return color.toFX();
    }

    @Override
    public String toCSS() {
        return color.toCSS();
    }

    @Override
    public Color brighter(double factor) {
        return color.brighter(factor);
    }

    @Override
    public Color darker(double factor) {
        return color.darker(factor);
    }

    @Override
    public double luminance() {
        return color.luminance();
    }
}