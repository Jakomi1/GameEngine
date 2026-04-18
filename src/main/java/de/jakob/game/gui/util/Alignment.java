package de.jakob.game.gui.util;

import javafx.geometry.Point2D;

public record Alignment(double percentX, double percentY) {
    public static final Alignment TOP_LEFT     = of(0.0, 0.0);
    public static final Alignment TOP_CENTER   = of(0.5, 0.0);
    public static final Alignment TOP_RIGHT    = of(1.0, 0.0);

    public static final Alignment CENTER_LEFT  = of(0.0, 0.5);
    public static final Alignment CENTER       = of(0.5, 0.5);
    public static final Alignment CENTER_RIGHT = of(1.0, 0.5);

    public static final Alignment BOTTOM_LEFT  = of(0.0, 1.0);
    public static final Alignment BOTTOM_CENTER= of(0.5, 1.0);
    public static final Alignment BOTTOM_RIGHT = of(1.0, 1.0);

    public Alignment(double percentX, double percentY) {
        this.percentX = clamp(percentX);
        this.percentY = clamp(percentY);
    }

    public static Alignment of(double percentX, double percentY) {
        return new Alignment(percentX, percentY);
    }

    public double resolveX(double areaWidth, double elementWidth) {
        return (areaWidth * percentX) - (elementWidth / 2.0);
    }

    public double resolveY(double areaHeight, double elementHeight) {
        return (areaHeight * percentY) - (elementHeight / 2.0);
    }

    public Point2D resolve(double areaWidth, double areaHeight, double elementWidth, double elementHeight) {
        return new Point2D(
                resolveX(areaWidth, elementWidth),
                resolveY(areaHeight, elementHeight)
        );
    }

    public Alignment withX(double percentX) {
        return of(percentX, this.percentY);
    }

    public Alignment withY(double percentY) {
        return of(this.percentX, percentY);
    }

    public Alignment left() {
        return withX(0.25);
    }

    public Alignment middleX() {
        return withX(0.50);
    }

    public Alignment right() {
        return withX(0.75);
    }

    public Alignment leftCorner() {
        return withX(0.01);
    }

    public Alignment rightCorner() {
        return withX(0.99);
    }

    public Alignment top() {
        return withY(0.01);
    }

    public Alignment centerY() {
        return withY(0.50);
    }

    public Alignment bottom() {
        return withY(0.99);
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    @Override
    public String toString() {
        return "Alignment{" +
                "percentX=" + percentX +
                ", percentY=" + percentY +
                '}';
    }
}