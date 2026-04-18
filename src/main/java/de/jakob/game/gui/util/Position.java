package de.jakob.game.gui.util;

import de.jakob.game.gui.generic.MainGraphicUserInterface;
import de.jakob.game.gui.GraphicUserInterface;
import de.jakob.game.gui.GraphicWindow;
import de.jakob.game.gui.graphics.GraphicItem;
import javafx.geometry.Point2D;

public class Position {

    public static Builder of(double x, double y) {
        return new Builder().position(x, y);
    }

    public static Builder of(Alignment alignment) {
        return new Builder().align(alignment);
    }

    public static Builder margin(double all) {
        return new Builder().margin(all, all, all, all);
    }

    public static Builder margin(double top, double right, double bottom, double left) {
        return new Builder().margin(top, right, bottom, left);
    }

    public static Builder marginTop(double value) {
        return new Builder().marginTop(value);
    }

    public static Builder marginRight(double value) {
        return new Builder().marginRight(value);
    }

    public static Builder marginBottom(double value) {
        return new Builder().marginBottom(value);
    }

    public static Builder marginLeft(double value) {
        return new Builder().marginLeft(value);
    }

    public static class Builder {

        private Double x = null;
        private Double y = null;

        private Double minX, minY;
        private Double maxX, maxY;

        private double marginTop = 0;
        private double marginRight = 0;
        private double marginBottom = 0;
        private double marginLeft = 0;

        private Alignment alignment;
        private boolean clampToGui = true;

        public Builder position(double x, double y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder align(Alignment alignment) {
            this.alignment = alignment;
            return this;
        }

        public Builder min(double x, double y) {
            this.minX = x;
            this.minY = y;
            return this;
        }

        public Builder max(double x, double y) {
            this.maxX = x;
            this.maxY = y;
            return this;
        }

        public Builder margin(double top, double right, double bottom, double left) {
            this.marginTop = top;
            this.marginRight = right;
            this.marginBottom = bottom;
            this.marginLeft = left;
            return this;
        }

        public Builder margin(double all) {
            return margin(all, all, all, all);
        }

        public Builder marginTop(double value) {
            this.marginTop = value;
            return this;
        }

        public Builder marginRight(double value) {
            this.marginRight = value;
            return this;
        }

        public Builder marginBottom(double value) {
            this.marginBottom = value;
            return this;
        }

        public Builder marginLeft(double value) {
            this.marginLeft = value;
            return this;
        }

        public Builder noGuiClamp() {
            this.clampToGui = false;
            return this;
        }

        public Point2D get(GraphicWindow window, GraphicUserInterface gui) {
            final double areaWidth = window != null ? window.getViewportWidth() : 0.0;
            final double areaHeight = window != null ? window.getViewportHeight() : 0.0;

            final double elementWidth = gui != null ? gui.getWindowWidth() : 0.0;
            final double elementHeight = gui != null ? gui.getWindowHeight() : 0.0;

            return resolve(areaWidth, areaHeight, elementWidth, elementHeight);
        }

        public Point2D get(GraphicUserInterface gui, GraphicItem item) {
            final double areaWidth = gui != null ? gui.getWindowWidth() : 0.0;

            final double areaHeight;
            if (gui instanceof MainGraphicUserInterface) {
                areaHeight = gui.getWindowHeight();
            } else {
                areaHeight = Math.max(0.0, gui != null ? gui.getWindowHeight() - GraphicUserInterface.TOP_BAR_HEIGHT : 0.0);
            }

            double elementWidth = 0.0;
            double elementHeight = 0.0;

            if (item != null) {
                elementWidth = item.getWidth();
                elementHeight = item.getHeight();

                if (elementWidth <= 0.0 || elementHeight <= 0.0) {
                    final var node = item.getNode();
                    if (node != null) {
                        if (elementWidth <= 0.0) {
                            elementWidth = node.getBoundsInParent().getWidth();
                        }
                        if (elementHeight <= 0.0) {
                            elementHeight = node.getBoundsInParent().getHeight();
                        }
                    }
                }
            }

            return resolve(areaWidth, areaHeight, elementWidth, elementHeight);
        }

        private Point2D resolve(double areaWidth, double areaHeight, double elementWidth, double elementHeight) {
            double resultX;
            double resultY;

            final boolean hasHorizontalMargin = marginLeft != 0.0 || marginRight != 0.0;
            final boolean hasVerticalMargin = marginTop != 0.0 || marginBottom != 0.0;

            if (hasHorizontalMargin) {
                if (marginLeft != 0.0) {
                    resultX = resolveHorizontalMargin(marginLeft, areaWidth);
                } else if (marginRight != 0.0) {
                    resultX = areaWidth - resolveHorizontalMargin(marginRight, areaWidth) - elementWidth;
                } else if (alignment != null) {
                    resultX = alignment.percentX() * (areaWidth - elementWidth);
                } else {
                    resultX = x != null ? x : 0.0;
                }
            } else if (alignment != null) {
                resultX = alignment.percentX() * (areaWidth - elementWidth);
            } else {
                resultX = x != null ? x : 0.0;
            }

            if (hasVerticalMargin) {
                if (marginTop != 0.0) {
                    resultY = resolveVerticalMargin(marginTop, areaHeight);
                } else if (marginBottom != 0.0) {
                    resultY = areaHeight - resolveVerticalMargin(marginBottom, areaHeight) - elementHeight;
                } else if (alignment != null) {
                    resultY = alignment.percentY() * (areaHeight - elementHeight);
                } else {
                    resultY = y != null ? y : 0.0;
                }
            } else if (alignment != null) {
                resultY = alignment.percentY() * (areaHeight - elementHeight);
            } else {
                resultY = y != null ? y : 0.0;
            }

            if (clampToGui) {
                final double minX = resolveHorizontalMargin(marginLeft, areaWidth);
                final double minY = resolveVerticalMargin(marginTop, areaHeight);
                final double maxX = Math.max(0.0, areaWidth - resolveHorizontalMargin(marginRight, areaWidth) - elementWidth);
                final double maxY = Math.max(0.0, areaHeight - resolveVerticalMargin(marginBottom, areaHeight) - elementHeight);

                resultX = resultX < minX ? minX : (resultX > maxX ? maxX : resultX);
                resultY = resultY < minY ? minY : (resultY > maxY ? maxY : resultY);
            }

            if (this.minX != null && resultX < this.minX) resultX = this.minX;
            if (this.minY != null && resultY < this.minY) resultY = this.minY;
            if (this.maxX != null && resultX > this.maxX) resultX = this.maxX;
            if (this.maxY != null && resultY > this.maxY) resultY = this.maxY;

            return new Point2D(resultX, resultY);
        }

        private double resolveHorizontalMargin(double value, double areaWidth) {
            return Math.abs(value) < 1.0 ? areaWidth * value : value;
        }

        private double resolveVerticalMargin(double value, double areaHeight) {
            return Math.abs(value) < 1.0 ? areaHeight * value : value;
        }

    }
}