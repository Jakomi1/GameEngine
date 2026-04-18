package de.jakob.game.color;

public interface Color {

    javafx.scene.paint.Color toFX();

    String toCSS();

    Color brighter(double factor);

    Color darker(double factor);

    double luminance();

    default boolean isLight() {
        return luminance() > 0.5;
    }

    default Color idealTextColor() {
        return isLight()
                ? NamedColor.BLACK
                : NamedColor.WHITE;
    }

    static Color fromRGB(int r, int g, int b) {
        return new HexColor(r, g, b);
    }

    static Color fromHex(String hex) {
        return HexColor.fromHex(hex);
    }

    record HexColor(int r, int g, int b) implements Color {

            public HexColor(int r, int g, int b) {
                this.r = clamp(r);
                this.g = clamp(g);
                this.b = clamp(b);
            }

            private static HexColor fromHex(String hex) {
                String value = hex == null ? "" : hex.replace("#", "").trim();
                if (value.length() != 6) {
                    throw new IllegalArgumentException("Hex-Farbe muss 6 Stellen haben");
                }

                int r = Integer.parseInt(value.substring(0, 2), 16);
                int g = Integer.parseInt(value.substring(2, 4), 16);
                int b = Integer.parseInt(value.substring(4, 6), 16);

                return new HexColor(r, g, b);
            }

            private int clamp(int v) {
                return Math.max(0, Math.min(255, v));
            }

            @Override
            public javafx.scene.paint.Color toFX() {
                return javafx.scene.paint.Color.rgb(r, g, b);
            }

            @Override
            public String toCSS() {
                return String.format("#%02x%02x%02x", r, g, b);
            }

            @Override
            public Color brighter(double factor) {
                int nr = (int) (r + (255 - r) * factor);
                int ng = (int) (g + (255 - g) * factor);
                int nb = (int) (b + (255 - b) * factor);
                return new HexColor(nr, ng, nb);
            }

            @Override
            public Color darker(double factor) {
                int nr = (int) (r * (1 - factor));
                int ng = (int) (g * (1 - factor));
                int nb = (int) (b * (1 - factor));
                return new HexColor(nr, ng, nb);
            }

            @Override
            public double luminance() {
                // Standard-Formel (perceived brightness)
                return (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
            }
        }
}