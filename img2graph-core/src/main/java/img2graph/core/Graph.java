package img2graph.core;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public record Graph(BufferedImage image, List<Node> nodes, List<Relationship> relationships) {

    public Graph {
        nodes = Collections.unmodifiableList(nodes);
        relationships = Collections.unmodifiableList(relationships);
    }

    public record Node(
            FlowFill.Coordinate coordinate, int radius, int id, ImageReader.Color color) {
        @Override
        public String toString() {
            return coordinate + "," + radius;
        }
    }

    // Undirected relationship to avoid duplicates
    public record Relationship(Node from, Node to, int id) {
        @Override
        public boolean equals(Object o) {
            Relationship that = (Relationship) o;
            return Objects.equals(from, that.from) && Objects.equals(to, that.to)
                    || Objects.equals(from, that.to) && Objects.equals(to, that.from);
        }

        @Override
        public int hashCode() {
            int fH = Objects.hash(from);
            int tH = Objects.hash(to);
            return Objects.hash(Math.min(fH, tH), Math.max(fH, tH));
        }
    }
}
