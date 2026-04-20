package de.jakob.game.gui.graphics.media;

import de.jakob.game.file.Directories;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.media.Media;

import java.io.File;
import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@SuppressWarnings("CallToPrintStackTrace")
final class GraphicMediaCache {

    private static final Map<String, CachedImage> TEXTURE_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Media> MEDIA_CACHE = new ConcurrentHashMap<>();

    private GraphicMediaCache() {
    }

    static CachedImage texture(String relativePath) {
        File file = Directories.texture(relativePath).asFile();
        if (!file.exists()) {
            throw new IllegalStateException("Texture nicht gefunden: " + file.getAbsolutePath());
        }

        String key = file.getAbsolutePath();
        return TEXTURE_CACHE.computeIfAbsent(key, GraphicMediaCache::loadTexture);
    }

    static CachedImage mediaImage(String relativePath) {
        File file = Directories.media(relativePath).asFile();
        if (!file.exists()) {
            throw new IllegalStateException("Media nicht gefunden: " + file.getAbsolutePath());
        }

        String key = file.getAbsolutePath();
        return TEXTURE_CACHE.computeIfAbsent(key, GraphicMediaCache::loadTexture);
    }

    static Media media(String relativePath) {
        File file = Directories.media(relativePath).asFile();
        if (!file.exists()) {
            throw new IllegalStateException("Media nicht gefunden: " + file.getAbsolutePath());
        }

        String key = file.getAbsolutePath();
        return MEDIA_CACHE.computeIfAbsent(key, k -> new Media(new File(k).toURI().toString()));
    }

    private static CachedImage loadTexture(String absolutePath) {
        try {
            Image image = new Image(new File(absolutePath).toURI().toString(), false);

            if (image.isError() || image.getWidth() <= 0 || image.getHeight() <= 0) {
                throw new IllegalStateException("Bild konnte nicht geladen werden: " + absolutePath, image.getException());
            }

            int width = (int) Math.round(image.getWidth());
            int height = (int) Math.round(image.getHeight());

            if (isGif(absolutePath)) {
                return new CachedImage(image, null, width, height);
            }

            PixelReader reader = image.getPixelReader();
            if (reader == null) {
                return new CachedImage(image, null, width, height);
            }

            BitSet bits = new BitSet(width * height);

            for (int y = 0; y < height; y++) {
                int row = y * width;
                for (int x = 0; x < width; x++) {
                    int argb = reader.getArgb(x, y);
                    int alpha = (argb >>> 24) & 0xff;
                    if (alpha > 0) {
                        bits.set(row + x);
                    }
                }
            }

            return new CachedImage(image, bits, width, height);
        } catch (Exception e) {
            e.printStackTrace();
            return new CachedImage(null, null, 0, 0);
        }
    }

    private static boolean isGif(String path) {
        return path != null && path.toLowerCase().endsWith(".gif");
    }

    protected record CachedImage(Image image, BitSet opaque, int width, int height) {
    }
}