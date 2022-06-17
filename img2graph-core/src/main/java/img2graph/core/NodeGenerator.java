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
    private final long segmentedSize;
    private final int maxSuperNodes;
    private int numberOfSuperNodes;
    private int nodeId = 0;

    NodeGenerator(
            Image image,
            int minRadius,
            int maxRadius,
            int padding,
            int maxSuperNodes,
            boolean useSimplifiedColor,
            long segmentedSize) {
        this.image = image;
        this.minRadius = minRadius;
        this.maxRadius = maxRadius;
        this.padding = padding;
        this.maxSuperNodes = maxSuperNodes;
        this.useSimplifiedColor = useSimplifiedColor;
        this.segmentedSize = segmentedSize;
    }

    Collection<Graph.Node> generate(Segment segment) {
        double segmentPercent = (double) segment.pixels.size() / segmentedSize;
        int superNodesLimit =
                maxSuperNodes > numberOfSuperNodes
                        ? Math.max(1, (int) Math.round(segmentPercent * maxSuperNodes))
                        : 0;

        List<Graph.Node> nodes = new ArrayList<>();
        Coordinate coordinate;
        while ((coordinate = segment.randomPixel()) != null) {
            if (tryPutNodeAt(coordinate, segment, nodes, superNodesLimit > 0)) {
                superNodesLimit--;
            }
        }
        return nodes;
    }

    private boolean tryPutNodeAt(
            Coordinate coordinate,
            Segment segment,
            List<Graph.Node> into,
            boolean canPlaceSuperNode) {
        int radius = -1;
        int extra =
                canPlaceSuperNode
                        ? (int)
                                ((3.0 * maxRadius)
                                        * (maxSuperNodes - numberOfSuperNodes * 0.8)
                                        / maxSuperNodes)
                        : 0;
        for (int i = minRadius; i <= maxRadius + extra; i++) {
            if (isFree(segment, coordinate, i)) {
                radius = i;
            } else {
                break;
            }
        }
        if (radius > 0) {
            if (radius > maxRadius) {
                numberOfSuperNodes++;
            }

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
        return radius > maxRadius;
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
