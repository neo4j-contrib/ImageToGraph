package img2graph;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

class ImageReader {
    private static Color WHITE = new Color(0xFFFFFFFF);
    private static Color BLACK = new Color(0xFF000000);

    private final int targetRes;
    private final int colorDepth;

    ImageReader(int targetRes, int colorDepth) {
        this.targetRes = targetRes;
        this.colorDepth = colorDepth;
    }

    Image readImage(InputStream stream) {
        try {
            BufferedImage image = alphaToWhite(resizeImage(ImageIO.read(stream), targetRes));
            return simplifyColor(image);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Image simplifyColor(BufferedImage image) {
        List<Color> colors = new ArrayList<>(image.getWidth() * image.getHeight());
        Map<Integer, ColorBucket> colorCount = new HashMap<>();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int rawColor = image.getRGB(x, y);
                Color color = new Color(rawColor);
                colorCount.computeIfAbsent(rawColor, ColorBucket::new).add(rawColor);
                if (!BLACK.equals(color) && !WHITE.equals(color)) {
                    colors.add(color);
                }
            }
        }

        List<ColorBucket> trueColors = colorCount.values().stream()
                .sorted((o1, o2) -> Integer.compare(o2.size, o1.size))
                .limit(10)
                .collect(Collectors.toList());

        List<Color> palette;
        if (trueColors.size() < 10 || trueColors.get(trueColors.size() - 1).size < 200) {
            palette = trueColors.stream().map(cb -> new Color(cb.rgb)).collect(Collectors.toList());
        } else {
            palette = medianCut(image);
            palette.add(BLACK);
            palette.add(WHITE);
        }
        Image img = new Image(image, resizeImage(image, targetRes), new Color(trueColors.get(0).rgb));
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color rawColor = new Color(image.getRGB(x, y));
                Color simplified = getClosest(palette, new Color(rawColor.raw));
                img.colors[x][y] = simplified;
                image.setRGB(x, y, simplified.raw);
            }
        }
        addDebugPalette(image, palette);
        return img;
    }

    private List<Color> medianCut(BufferedImage image) {
        List<Color> colors = new ArrayList<>(image.getWidth() * image.getHeight());
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color color = new Color(image.getRGB(x, y));
                if (!BLACK.equals(color) && !WHITE.equals(color)) {
                    colors.add(color);
                }
            }
        }
        return medianCut(colors, colorDepth);
    }

    private static List<Color> medianCut(List<Color> colors, int depth) {
        if (colors.isEmpty()) {
            return List.of();
        }
        if (depth == 0) {
            ColorBucket bucket = new ColorBucket(0);
            for (Color color : colors) {
                bucket.add(color.raw);
            }
            return List.of(new Color(bucket.getAverage()));
        }
        int[][] ranges = {
            {Integer.MAX_VALUE, Integer.MIN_VALUE},
            {Integer.MAX_VALUE, Integer.MIN_VALUE},
            {Integer.MAX_VALUE, Integer.MIN_VALUE}
        };

        for (Color color : colors) {
            range(ranges[0], color.r);
            range(ranges[1], color.g);
            range(ranges[2], color.b);
        }

        int dr = ranges[0][1] - ranges[0][0];
        int dg = ranges[1][1] - ranges[1][0];
        int db = ranges[2][1] - ranges[2][0];
        if (dr > dg && dr > db) {
            colors.sort(Comparator.comparingInt(Color::r));
        } else if (dg > db) {
            colors.sort(Comparator.comparingInt(Color::g));
        } else {
            colors.sort(Comparator.comparingInt(Color::b));
        }
        List<Color> result = new ArrayList<>();
        result.addAll(medianCut(colors.subList(0, colors.size() / 2), depth - 1));
        result.addAll(medianCut(colors.subList(colors.size() / 2, colors.size()), depth - 1));
        return result;
    }

    private static BufferedImage alphaToWhite(BufferedImage image) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int argb = image.getRGB(x, y);
                boolean visible = (argb & 0xFF000000) >>> 24 > 230;
                image.setRGB(x, y, visible ? argb | 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return image;
    }

    private static void range(int[] range, int color) {
        range[0] = Math.min(color, range[0]);
        range[1] = Math.max(color, range[1]);
    }

    private static void addDebugPalette(BufferedImage image, List<Color> palette) {
        for (int i = 0; i < palette.size(); i++) {
            Color c = palette.get(i);
            int w = (i * 20) % 500;
            int h = ((i * 20) / 500) * 20;
            for (int x = 0; x < 20; x++) {
                for (int y = 0; y < 20; y++) {
                    image.setRGB(w + x, h + y, c.raw);
                }
            }
        }
    }

    record Color(int r, int g, int b, int raw) {
        Color(int argb) {
            this((argb & 0xFF0000) >> 16, (argb & 0x00FF00) >> 8, argb & 0x0000FF, argb);
        }

        private int distSq(Color other) {
            int dR = r - other.r;
            int dG = g - other.g;
            int dB = b - other.b;
            return dR * dR + dG * dG + dB * dB;
        }

        @Override
        public String toString() {
            return String.format("%02x%02x%02x", r, g, b);
        }
    }

    private static Color getClosest(List<Color> palette, Color rawColor) {
        Color closest = rawColor;
        int dist = Integer.MAX_VALUE;
        for (Color color : palette) {
            if (color.raw == rawColor.raw) {
                return rawColor;
            }
            int d = color.distSq(rawColor);
            if (d < dist) {
                closest = color;
                dist = d;
            }
        }
        return closest;
    }

    static BufferedImage resizeImage(BufferedImage image, int targetMax) {
        boolean portrait = image.getHeight() > image.getWidth();
        double size = portrait ? image.getHeight() : image.getWidth();
        double sf = (double) targetMax / size;
        int targetHeight = (int) (sf * image.getHeight());
        int targetWidth = (int) (sf * image.getWidth());
        java.awt.Image resultingImage =
                image.getScaledInstance(targetWidth, targetHeight, java.awt.Image.SCALE_DEFAULT);
        BufferedImage scaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = scaledImage.getGraphics();
        graphics.drawImage(resultingImage, 0, 0, null);
        graphics.dispose();
        return scaledImage;
    }

    static class ColorBucket {
        int rgb;
        long r = 0, g = 0, b = 0;
        int size;

        public ColorBucket(int argb) {
            this.rgb = argb;
        }

        void add(int argb) {
            r += (argb & 0xFF0000) >> 16;
            g += (argb & 0x00FF00) >> 8;
            b += argb & 0x0000FF;
            size++;
        }

        int getAverage() {
            int avgR = (int) r / size;
            int avgG = (int) g / size;
            int avgB = (int) b / size;
            return 0xFF000000 | avgR << 16 | avgG << 8 | avgB;
        }
    }

    static class Image {
        final BufferedImage original;
        final BufferedImage source;
        final int width;
        final int height;
        final Color[][] colors;
        final Color bg;

        Image(BufferedImage source, BufferedImage original, Color bg) {
            this.width = source.getWidth();
            this.height = source.getHeight();
            this.source = source;
            this.original = original;
            this.bg = bg;
            this.colors = new Color[width][height];
        }
    }
}
