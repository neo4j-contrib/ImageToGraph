package img2graph.core;

import img2graph.core.FlowFill.Coordinate;
import img2graph.core.FlowFill.Segment;
import img2graph.core.ImageReader.Color;
import img2graph.core.ImageReader.Image;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class NodeGenerator {
    private final Image image;
    private final int minRadius;
    private final int maxRadius;
    private final int padding;
    private final boolean useSimplifiedColor;
    private int nodeId = 0;

    NodeGenerator(
            Image image, int minRadius, int maxRadius, int padding, boolean useSimplifiedColor) {
        this.image = image;
        this.minRadius = minRadius;
        this.maxRadius = maxRadius;
        this.padding = padding;
        this.useSimplifiedColor = useSimplifiedColor;
    }

    Collection<Graph.Node> generate(Segment segment) {
        List<Graph.Node> nodes = new ArrayList<>();
        Coordinate coordinate;
        while ((coordinate = segment.randomPixel()) != null) {
            tryPutNodeAt(coordinate, segment, nodes);
        }
        return nodes;
    }

    private void tryPutNodeAt(Coordinate coordinate, Segment segment, List<Graph.Node> into) {
        int radius = -1;
        for (int i = minRadius; i <= maxRadius; i++) {
            if (isFree(segment, coordinate, i)) {
                radius = i;
            } else {
                break;
            }
        }
        if (radius > 0) {
            Color color =
                    useSimplifiedColor
                            ? segment.color
                            : new Color(image.original.getRGB(coordinate.x(), coordinate.y()));
            into.add(new Graph.Node(coordinate, radius, nodeId++, color));

            int padding = radius + this.padding;
            int paddingSq = padding * padding;
            for (int x = -padding; x <= padding; x++) {
                for (int y = -padding; y < padding; y++) {
                    if (x * x + y * y <= paddingSq) {
                        Coordinate removableCoordinate =
                                new Coordinate(coordinate.x() + x, coordinate.y() + y);
                        segment.pixels.remove(removableCoordinate);
                    }
                }
            }
        }
    }

    private boolean isFree(Segment segment, Coordinate coordinate, int radius) {
        int radSq = radius * radius;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y < radius; y++) {
                if (x * x + y * y <= radSq
                        && !segment.pixels.contains(
                                new Coordinate(coordinate.x() + x, coordinate.y() + y))) {
                    return false;
                }
            }
        }
        return true;
    }
}
