package img2graph;

import img2graph.FlowFill.Coordinate;
import img2graph.FlowFill.Segment;
import img2graph.ImageReader.Color;
import img2graph.ImageReader.Image;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

class NodeGenerator {
    private static final Random RANDOM = new Random(45);
    private static final int MAX_ATTEMPTS = 1000;
    private final Image image;
    private final int minRadius;
    private final int maxRadius;
    private final int padding;
    private final boolean useSimplifiedColor;
    private int nodeId = 0;

    NodeGenerator(Image image, int minRadius, int maxRadius, int padding, boolean useSimplifiedColor) {
        this.image = image;
        this.minRadius = minRadius;
        this.maxRadius = maxRadius;
        this.padding = padding;
        this.useSimplifiedColor = useSimplifiedColor;
    }

    Collection<Node> generate(Segment segment) {
        List<Node> nodes = new ArrayList<>();
        int attempts = 0;
        while (attempts < MAX_ATTEMPTS && !segment.pixels.isEmpty()) {
            if (generateNodeFor(segment, nodes)) {
                attempts = 0;
            } else {
                attempts++;
            }
        }
        return nodes;
    }

    private boolean generateNodeFor(Segment segment, List<Node> into) {
        Coordinate coordinate = segment.pixels.stream()
                .skip(RANDOM.nextInt(segment.pixels.size()))
                .findFirst()
                .get();

        int radius = -1;
        for (int i = minRadius; i <= maxRadius; i++) {
            if (isFree(segment, coordinate, i)) {
                radius = i;
            } else {
                break;
            }
        }
        if (radius > 0) {
            Color color = useSimplifiedColor
                    ? segment.color
                    : new Color(image.original.getRGB(coordinate.x(), coordinate.y()));
            into.add(new Node(coordinate, radius, nodeId++, color));

            int padding = radius + this.padding;
            int paddingSq = padding * padding;
            for (int x = -padding; x <= padding; x++) {
                for (int y = -padding; y < padding; y++) {
                    if (x * x + y * y <= paddingSq) {
                        Coordinate removableCoordinate = new Coordinate(coordinate.x() + x, coordinate.y() + y);
                        segment.pixels.remove(removableCoordinate);
                    }
                }
            }
            return true;
        }
        return false;
    }

    private boolean isFree(Segment segment, Coordinate coordinate, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y < radius; y++) {
                if (!segment.pixels.contains(new Coordinate(coordinate.x() + x, coordinate.y() + y))) {
                    return false;
                }
            }
        }
        return true;
    }

    record Node(Coordinate coordinate, int radius, int id, Color color) {
        @Override
        public String toString() {
            return coordinate + "," + radius;
        }
    }
}
