package de.jakob.game.gui.graphics.basic;

import de.jakob.game.color.Color;
import de.jakob.game.color.NamedColor;
import de.jakob.game.font.FontUsable;
import de.jakob.game.gui.graphics.GraphicItem;
import de.jakob.game.gui.util.Alignment;
import de.jakob.game.scheduler.GameScheduler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.text.Font;

import java.util.Objects;

public class GraphicButton extends GraphicItem implements FontUsable<GraphicButton> {

    private final Button button;

    private String text = "";
    private GraphicText graphicText;

    private GameScheduler pressedRepeatScheduler;
    private Runnable pressedRepeatAction;
    private long pressedRepeatIntervalTicks = -1;
    private GameScheduler.ScheduledTask pressedRepeatTask;

    private GameScheduler notPressedRepeatScheduler;
    private Runnable notPressedRepeatAction;
    private long notPressedRepeatIntervalTicks = -1;
    private GameScheduler.ScheduledTask notPressedRepeatTask;

    private Color backgroundColor;
    private Color hoverColor;
    private Color clickColor;
    private Color textColor;

    private Font font;

    private boolean hovered = false;
    private boolean pressed = false;

    private double roundingPercent = 0.1;

    private static final double DEFAULT_WIDTH = 120;
    private static final double DEFAULT_HEIGHT = 40;

    private boolean built = false;
    private boolean handlersInstalled = false;

    private boolean dirtyContent = true;
    private boolean dirtySize = true;
    private boolean dirtyFont = true;
    private boolean dirtyStyle = true;

    private String lastStyle = "";

    private Alignment alignment;
    private boolean wrapText = false;
    private Double minHeightValue = null;
    private Double maxWidthValue = null;

    private GraphicButton() {
        this.button = new Button();
        setNode(button);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected boolean usesExternalClickHandling() {
        return true;
    }

    @Override
    public GraphicButton size(double width, double height) {
        if (Double.compare(this.width, width) == 0 && Double.compare(this.height, height) == 0) {
            return this;
        }

        super.size(width, height);
        dirtySize = true;
        dirtyStyle = true;

        if (built) {
            refresh();
        }

        return this;
    }

    private void markContentDirty() {
        dirtyContent = true;
        dirtySize = true;
        dirtyFont = true;
        dirtyStyle = true;

        if (built) {
            refresh();
        }
    }

    private void markStyleDirty() {
        dirtyStyle = true;
        if (built) {
            refresh();
        }
    }

    private void refresh() {
        if (button == null) {
            return;
        }

        if (dirtyFont) {
            refreshFont();
            dirtyFont = false;
        }

        if (dirtyContent) {
            refreshContent();
            dirtyContent = false;
        }

        if (dirtySize) {
            refreshSize();
            dirtySize = false;
        }

        if (dirtyStyle) {
            refreshStyle();
            dirtyStyle = false;
        }

        applyPosition();
    }

    private void refreshContent() {
        if (graphicText != null) {
            graphicText.init(gui);
            graphicText.build();

            graphicText.getNode().setMouseTransparent(true);

            button.setText("");
            button.setGraphic(graphicText.getNode());
        } else {
            button.setGraphic(null);
            button.setText(text);
        }
    }

    private void refreshSize() {
        double w = width > 0 ? width : DEFAULT_WIDTH;
        double h = height > 0 ? height : DEFAULT_HEIGHT;

        if (Double.compare(this.width, w) != 0 || Double.compare(this.height, h) != 0) {
            this.width = w;
            this.height = h;
            recalcPosition();
        }

        button.setPrefSize(w, h);
        button.setMinSize(w, h);
        button.setMaxSize(w, h);

        if (minHeightValue != null) button.setMinHeight(minHeightValue);
        if (maxWidthValue != null) button.setMaxWidth(maxWidthValue);
    }

    public GraphicButton repeatingWhilePressed(GameScheduler scheduler, Runnable action, long intervalTicks) {
        if (scheduler == null) {
            throw new IllegalArgumentException("Scheduler darf nicht null sein!");
        }
        if (action == null) {
            throw new IllegalArgumentException("Action darf nicht null sein!");
        }
        if (intervalTicks <= 0) {
            throw new IllegalArgumentException("intervalTicks muss > 0 sein!");
        }

        this.pressedRepeatScheduler = scheduler;
        this.pressedRepeatAction = action;
        this.pressedRepeatIntervalTicks = intervalTicks;

        if (built) {
            updateRepeatTasks();
        }

        return this;
    }

    public GraphicButton repeatingWhileNotPressed(GameScheduler scheduler, Runnable action, long intervalTicks) {
        if (scheduler == null) {
            throw new IllegalArgumentException("Scheduler darf nicht null sein!");
        }
        if (action == null) {
            throw new IllegalArgumentException("Action darf nicht null sein!");
        }
        if (intervalTicks <= 0) {
            throw new IllegalArgumentException("intervalTicks muss > 0 sein!");
        }

        this.notPressedRepeatScheduler = scheduler;
        this.notPressedRepeatAction = action;
        this.notPressedRepeatIntervalTicks = intervalTicks;

        if (built) {
            updateRepeatTasks();
        }

        return this;
    }

    public GraphicButton roundedEdges(double percent) {
        double clamped = Math.max(0.0, Math.min(1.0, percent));
        if (Double.compare(this.roundingPercent, clamped) == 0) {
            return this;
        }

        this.roundingPercent = clamped;
        markStyleDirty();
        return this;
    }

    public GraphicButton text(String text) {
        String value = text != null ? text : "";
        if (Objects.equals(this.text, value) && graphicText == null) {
            return this;
        }

        this.text = value;
        this.graphicText = null;
        markContentDirty();
        return this;
    }

    public GraphicButton graphicText(GraphicText graphicText) {
        if (this.graphicText == graphicText) {
            return this;
        }

        this.graphicText = graphicText;
        this.text = "";
        markContentDirty();
        return this;
    }

    public GraphicButton backgroundColor(Color color) {
        if (Objects.equals(this.backgroundColor, color)) {
            return this;
        }

        this.backgroundColor = color;
        markStyleDirty();
        return this;
    }

    public GraphicButton hover(Color color) {
        if (Objects.equals(this.hoverColor, color)) {
            return this;
        }

        this.hoverColor = color;
        markStyleDirty();
        return this;
    }

    public GraphicButton clickColor(Color color) {
        if (Objects.equals(this.clickColor, color)) {
            return this;
        }

        this.clickColor = color;
        markStyleDirty();
        return this;
    }

    public GraphicButton textColor(Color color) {
        if (Objects.equals(this.textColor, color)) {
            return this;
        }

        this.textColor = color;
        markStyleDirty();
        return this;
    }

    @Override
    public GraphicButton font(Font font) {
        if (Objects.equals(this.font, font)) {
            return this;
        }

        this.font = font;
        dirtyFont = true;
        dirtyContent = true;
        dirtySize = true;

        if (built) {
            refresh();
        }

        return this;
    }

    @Override
    public Font getFont() {
        return font;
    }

    @Override
    public void applyFont() {
        Font usedFont = effectiveFont();

        if (graphicText != null) {
            graphicText.font(usedFont);
        } else {
            button.setFont(usedFont);
        }
    }

    @Override
    public void build() {
        if (!handlersInstalled) {
            button.setAlignment(Pos.CENTER);
            button.setFocusTraversable(false);

            button.setOnMouseEntered(e -> {
                hovered = true;
                refreshStyleOnly();
            });

            button.setOnMouseExited(e -> {
                hovered = false;
                pressed = false;
                refreshStyleOnly();
                updateRepeatTasks();
            });

            button.setOnMousePressed(e -> {
                if (e.isPrimaryButtonDown()) {
                    pressed = true;
                    refreshStyleOnly();
                    updateRepeatTasks();
                }
            });

            button.setOnMouseReleased(e -> {
                pressed = false;
                refreshStyleOnly();
                updateRepeatTasks();
            });

            button.setOnAction(e -> {
                fireOnClick();
                if (button.getParent() != null) {
                    button.getParent().requestFocus();
                }
            });

            handlersInstalled = true;
        }

        button.setWrapText(wrapText);
        applyAlignment();

        built = true;
        refresh();
        updateRepeatTasks();
    }

    private void refreshStyleOnly() {
        dirtyStyle = true;
        if (built) {
            refreshStyle();
            dirtyStyle = false;
        }
    }

    private void refreshFont() {
        Font usedFont = effectiveFont();

        if (graphicText != null) {
            graphicText.font(usedFont);
        } else {
            button.setFont(usedFont);
        }
    }

    private Color effectiveBackgroundColor() {
        return backgroundColor != null ? backgroundColor : Color.fromHex("#cccccc");
    }

    private Color effectiveHoverColor(Color base) {
        return hoverColor != null ? hoverColor : base.brighter(0.15);
    }

    private Color effectiveClickColor(Color hover) {
        return clickColor != null ? clickColor : hover.brighter(0.15);
    }

    private void refreshStyle() {
        Color base = effectiveBackgroundColor();
        Color hover = effectiveHoverColor(base);
        Color click = effectiveClickColor(hover);

        Color bg = pressed ? click : (hovered ? hover : base);

        StringBuilder style = new StringBuilder(128);

        double w = getWidth() > 0 ? getWidth() : (width > 0 ? width : DEFAULT_WIDTH);
        double h = getHeight() > 0 ? getHeight() : (height > 0 ? height : DEFAULT_HEIGHT);
        double radius = Math.min(w, h) * roundingPercent;

        style.append("-fx-background-color: ").append(bg.toCSS()).append(';');
        style.append("-fx-background-radius: ").append(radius).append(';');

        if (textColor != null && graphicText == null) {
            style.append("-fx-text-fill: ").append(textColor.toCSS()).append(';');
        }

        style.append("-fx-focus-color: transparent;");
        style.append("-fx-faint-focus-color: transparent;");

        String newStyle = style.toString();
        if (!newStyle.equals(lastStyle)) {
            button.setStyle(newStyle);
            lastStyle = newStyle;
        }
    }

    public Font effectiveFont() {
        return font != null ? font : FontUsable.DEFAULT_FONT;
    }

    public GraphicButton alignment(Alignment alignment) {
        this.alignment = alignment;
        if (built) applyAlignment();
        return this;
    }

    private void applyAlignment() {
        if (alignment == null) return;
        Pos fxPos = Pos.CENTER;
        if (alignment.percentX() <= 0.1) {
            if (alignment.percentY() <= 0.1) fxPos = Pos.TOP_LEFT;
            else if (alignment.percentY() >= 0.9) fxPos = Pos.BOTTOM_LEFT;
            else fxPos = Pos.CENTER_LEFT;
        } else if (alignment.percentX() >= 0.9) {
            if (alignment.percentY() <= 0.1) fxPos = Pos.TOP_RIGHT;
            else if (alignment.percentY() >= 0.9) fxPos = Pos.BOTTOM_RIGHT;
            else fxPos = Pos.CENTER_RIGHT;
        } else {
            if (alignment.percentY() <= 0.1) fxPos = Pos.TOP_CENTER;
            else if (alignment.percentY() >= 0.9) fxPos = Pos.BOTTOM_CENTER;
            else fxPos = Pos.CENTER;
        }
        this.button.setAlignment(fxPos);
    }

    public GraphicButton wrapText(boolean wrap) {
        this.wrapText = wrap;
        if (button != null) this.button.setWrapText(wrap);
        return this;
    }

    public GraphicButton minHeight(double height) {
        this.minHeightValue = height;
        if (button != null) this.button.setMinHeight(height);
        return this;
    }

    public GraphicButton maxWidth(double width) {
        this.maxWidthValue = width;
        if (button != null) this.button.setMaxWidth(width);
        return this;
    }

    public static class Builder extends GraphicItemBuilder<GraphicButton, Builder> {

        private String text = "";
        private GraphicText graphicText;
        private Color backgroundColor = NamedColor.LIGHT;
        private Color hoverColor;
        private Color clickColor;
        private Color textColor;
        private Font font;
        private GameScheduler pressedRepeatScheduler;
        private Runnable pressedRepeatAction;
        private long pressedRepeatIntervalTicks = -1;
        private GameScheduler notPressedRepeatScheduler;
        private Runnable notPressedRepeatAction;
        private long notPressedRepeatIntervalTicks = -1;
        private double roundingPercent = 0.05;
        private Alignment alignment;
        private boolean wrapText = false;
        private Double minHeight;
        private Double maxWidth;

        public Builder text(String text) {
            this.text = text != null ? text : "";
            this.graphicText = null;
            return this;
        }

        public Builder graphicText(GraphicText graphicText) {
            this.graphicText = graphicText;
            this.text = "";
            return this;
        }

        public Builder backgroundColor(Color color) {
            this.backgroundColor = color;
            return this;
        }

        public Builder hover(Color color) {
            this.hoverColor = color;
            return this;
        }

        public Builder clickColor(Color color) {
            this.clickColor = color;
            return this;
        }

        public Builder textColor(Color color) {
            this.textColor = color;
            return this;
        }

        public Builder font(Font font) {
            this.font = font;
            return this;
        }

        public Builder repeatingWhilePressed(GameScheduler scheduler, Runnable action, long intervalTicks) {
            this.pressedRepeatScheduler = scheduler;
            this.pressedRepeatAction = action;
            this.pressedRepeatIntervalTicks = intervalTicks;
            return this;
        }

        public Builder repeatingWhileNotPressed(GameScheduler scheduler, Runnable action, long intervalTicks) {
            this.notPressedRepeatScheduler = scheduler;
            this.notPressedRepeatAction = action;
            this.notPressedRepeatIntervalTicks = intervalTicks;
            return this;
        }

        public Builder roundedEdges(double percent) {
            this.roundingPercent = Math.max(0.0, Math.min(1.0, percent));
            return this;
        }

        public Builder alignment(Alignment alignment) {
            this.alignment = alignment;
            return this;
        }

        public Builder wrapText(boolean wrapText) {
            this.wrapText = wrapText;
            return this;
        }

        public Builder minHeight(double minHeight) {
            this.minHeight = minHeight;
            return this;
        }

        public Builder maxWidth(double maxWidth) {
            this.maxWidth = maxWidth;
            return this;
        }

        @Override
        protected GraphicButton create() {
            GraphicButton button = new GraphicButton();
            button.text = text;
            button.graphicText = graphicText;
            button.backgroundColor = backgroundColor;
            button.hoverColor = hoverColor;
            button.clickColor = clickColor;
            button.textColor = textColor;
            button.font = font;
            button.roundingPercent = roundingPercent;
            button.pressedRepeatScheduler = pressedRepeatScheduler;
            button.pressedRepeatAction = pressedRepeatAction;
            button.pressedRepeatIntervalTicks = pressedRepeatIntervalTicks;
            button.notPressedRepeatScheduler = notPressedRepeatScheduler;
            button.notPressedRepeatAction = notPressedRepeatAction;
            button.notPressedRepeatIntervalTicks = notPressedRepeatIntervalTicks;
            button.alignment = alignment;
            button.wrapText = wrapText;
            button.minHeightValue = minHeight;
            button.maxWidthValue = maxWidth;
            return button;
        }
    }

    private void updateRepeatTasks() {
        if (pressed) {
            stopNotPressedRepeatTask();
            startPressedRepeatTask();
        } else {
            stopPressedRepeatTask();
            startNotPressedRepeatTask();
        }
    }

    private void startPressedRepeatTask() {
        stopPressedRepeatTask();
        if (pressedRepeatScheduler == null || pressedRepeatAction == null || pressedRepeatIntervalTicks <= 0) {
            return;
        }
        pressedRepeatTask = pressedRepeatScheduler.runRepeating(() -> {
            if (!pressed) return;
            pressedRepeatAction.run();
        }, 0, pressedRepeatIntervalTicks);
    }

    private void startNotPressedRepeatTask() {
        stopNotPressedRepeatTask();
        if (notPressedRepeatScheduler == null || notPressedRepeatAction == null || notPressedRepeatIntervalTicks <= 0) {
            return;
        }
        notPressedRepeatTask = notPressedRepeatScheduler.runRepeating(() -> {
            if (pressed) return;
            notPressedRepeatAction.run();
        }, 0, notPressedRepeatIntervalTicks);
    }

    private void stopPressedRepeatTask() {
        if (pressedRepeatTask != null) {
            pressedRepeatTask.cancel();
            pressedRepeatTask = null;
        }
    }

    private void stopNotPressedRepeatTask() {
        if (notPressedRepeatTask != null) {
            notPressedRepeatTask.cancel();
            notPressedRepeatTask = null;
        }
    }
}