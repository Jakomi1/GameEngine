package de.jakob.game.gui.graphics.basic;

import de.jakob.game.gui.graphics.GraphicItem;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;

import java.util.Objects;

public class GraphicScrollPane extends GraphicItem {

    private final ScrollPane scrollPane;

    private Node content;

    private boolean fitToWidth = true;
    private boolean fitToHeight = false;
    private boolean pannable = true;

    private ScrollPane.ScrollBarPolicy horizontalBarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED;
    private ScrollPane.ScrollBarPolicy verticalBarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED;

    private boolean transparentBackground = true;
    private String extraStyle = "";

    private boolean built = false;
    private String lastStyle = "";

    private GraphicScrollPane() {
        this.scrollPane = new ScrollPane();
        this.scrollPane.setPickOnBounds(true);
        setNode(scrollPane);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public GraphicScrollPane size(double width, double height) {
        if (Double.compare(this.width, width) == 0 && Double.compare(this.height, height) == 0) {
            return this;
        }

        super.size(width, height);

        if (built) {
            refresh();
        }
        return this;
    }

    public GraphicScrollPane content(Node content) {
        if (this.content == content) return this;

        this.content = content;
        if (built) refresh();
        return this;
    }

    public GraphicScrollPane content(GraphicItem item) {
        return content(item != null ? item.getNode() : null);
    }

    public GraphicScrollPane fitToWidth(boolean value) {
        if (this.fitToWidth == value) return this;

        this.fitToWidth = value;
        if (built) refresh();
        return this;
    }

    public GraphicScrollPane fitToHeight(boolean value) {
        if (this.fitToHeight == value) return this;

        this.fitToHeight = value;
        if (built) refresh();
        return this;
    }

    public GraphicScrollPane pannable(boolean value) {
        if (this.pannable == value) return this;

        this.pannable = value;
        if (built) refresh();
        return this;
    }

    public GraphicScrollPane horizontalBarPolicy(ScrollPane.ScrollBarPolicy policy) {
        ScrollPane.ScrollBarPolicy newPolicy =
                policy != null ? policy : ScrollPane.ScrollBarPolicy.AS_NEEDED;

        if (this.horizontalBarPolicy == newPolicy) return this;

        this.horizontalBarPolicy = newPolicy;
        if (built) refresh();
        return this;
    }

    public GraphicScrollPane verticalBarPolicy(ScrollPane.ScrollBarPolicy policy) {
        ScrollPane.ScrollBarPolicy newPolicy =
                policy != null ? policy : ScrollPane.ScrollBarPolicy.AS_NEEDED;

        if (this.verticalBarPolicy == newPolicy) return this;

        this.verticalBarPolicy = newPolicy;
        if (built) refresh();
        return this;
    }

    public GraphicScrollPane transparentBackground(boolean value) {
        if (this.transparentBackground == value) return this;

        this.transparentBackground = value;
        if (built) refresh();
        return this;
    }

    public GraphicScrollPane style(String style) {
        String value = style != null ? style : "";

        if (Objects.equals(this.extraStyle, value)) return this;

        this.extraStyle = value;
        if (built) refresh();
        return this;
    }

    @Override
    public void build() {
        built = true;
        refresh();
    }

    private void refresh() {
        scrollPane.setContent(content);
        scrollPane.setFitToWidth(fitToWidth);
        scrollPane.setFitToHeight(fitToHeight);
        scrollPane.setPannable(pannable);
        scrollPane.setHbarPolicy(horizontalBarPolicy);
        scrollPane.setVbarPolicy(verticalBarPolicy);

        double w = getWidth();
        double h = getHeight();

        if (w > 0 && h > 0) {
            scrollPane.setPrefSize(w, h);
            scrollPane.setMinSize(w, h);
            scrollPane.setMaxSize(w, h);
        }

        StringBuilder style = new StringBuilder();

        if (transparentBackground) {
            style.append("-fx-background-color: transparent;");
            style.append("-fx-background: transparent;");
        }

        if (!extraStyle.isBlank()) {
            style.append(extraStyle);
            if (!extraStyle.endsWith(";")) {
                style.append(";");
            }
        }

        String newStyle = style.toString();

        if (!newStyle.equals(lastStyle)) {
            scrollPane.setStyle(newStyle);
            lastStyle = newStyle;
        }

        applyPosition();
    }

    // =========================
    // BUILDER
    // =========================

    public static class Builder extends GraphicItemBuilder<GraphicScrollPane, Builder> {

        private Node content;

        private boolean fitToWidth = true;
        private boolean fitToHeight = false;
        private boolean pannable = true;

        private ScrollPane.ScrollBarPolicy horizontalBarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED;
        private ScrollPane.ScrollBarPolicy verticalBarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED;

        private boolean transparentBackground = true;
        private String style = "";

        public Builder content(Node content) {
            this.content = content;
            return this;
        }

        public Builder content(GraphicItem item) {
            this.content = item != null ? item.getNode() : null;
            return this;
        }

        public Builder fitToWidth(boolean value) {
            this.fitToWidth = value;
            return this;
        }

        public Builder fitToHeight(boolean value) {
            this.fitToHeight = value;
            return this;
        }

        public Builder pannable(boolean value) {
            this.pannable = value;
            return this;
        }

        public Builder horizontalBarPolicy(ScrollPane.ScrollBarPolicy policy) {
            this.horizontalBarPolicy =
                    policy != null ? policy : ScrollPane.ScrollBarPolicy.AS_NEEDED;
            return this;
        }

        public Builder verticalBarPolicy(ScrollPane.ScrollBarPolicy policy) {
            this.verticalBarPolicy =
                    policy != null ? policy : ScrollPane.ScrollBarPolicy.AS_NEEDED;
            return this;
        }

        public Builder transparentBackground(boolean value) {
            this.transparentBackground = value;
            return this;
        }

        public Builder style(String style) {
            this.style = style != null ? style : "";
            return this;
        }

        @Override
        protected GraphicScrollPane create() {
            GraphicScrollPane pane = new GraphicScrollPane();

            pane.content = content;
            pane.fitToWidth = fitToWidth;
            pane.fitToHeight = fitToHeight;
            pane.pannable = pannable;
            pane.horizontalBarPolicy = horizontalBarPolicy;
            pane.verticalBarPolicy = verticalBarPolicy;
            pane.transparentBackground = transparentBackground;
            pane.extraStyle = style;

            return pane;
        }
    }
}