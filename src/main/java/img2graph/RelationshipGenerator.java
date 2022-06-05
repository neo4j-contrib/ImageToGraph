package img2graph;

import static java.util.Comparator.comparingInt;

import img2graph.FlowFill.Coordinate;
import img2graph.ImageReader.Image;
import img2graph.NodeGenerator.Node;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

class RelationshipGenerator {
    private final Image image;
    private final int maxDistance;
    private final int numRels;
    private TreeSet<Node> sortedX;
    private TreeSet<Node> sortedY;
    private int relId = 0;

    RelationshipGenerator(Image image, int maxDistance, int numRels) {
        this.image = image;
        this.maxDistance = maxDistance;
        this.numRels = numRels;
    }

    Collection<Relationship> generate(Collection<Node> nodes) {
        Set<Relationship> relationships = new HashSet<>();
        sortedX = new TreeSet<>(comparingInt((Node n) -> n.coordinate().x())
                .thenComparingInt(n -> n.coordinate().y()));
        sortedY = new TreeSet<>(comparingInt((Node n) -> n.coordinate().y())
                .thenComparingInt(n -> n.coordinate().x()));
        sortedX.addAll(nodes);
        sortedY.addAll(nodes);
        int biggestNode = nodes.stream().map(Node::radius).max(Integer::compare).orElse(0);
        for (Node node : nodes) {
            List<Node> neighbors = nearestNeighbors(node, 10, biggestNode);
            neighbors.sort(Comparator.comparingDouble(n -> weightedSort(n, node)));
            for (int i = 0; i < numRels && i < neighbors.size(); i++) {
                Relationship relationship = new Relationship(node, neighbors.get(i), relId++);
                if (validate(relationship)) {
                    relationships.add(relationship);
                }
            }
        }
        return relationships.stream().toList();
    }

    private boolean validate(Relationship relationship) {
        Coordinate from = relationship.from().coordinate();
        Coordinate to = relationship.to().coordinate();
        int diffX = to.x() - from.x();
        int diffY = to.y() - from.y();
        double dist = Math.sqrt(from.distSq(to))
                - relationship.from().radius()
                - relationship.to().radius();
        if (dist > maxDistance) {
            return false;
        }
        int steps = Math.max(Math.abs(diffX), Math.abs(diffY));
        double dx = diffX / (double) steps;
        double dy = diffY / (double) steps;
        for (int i = 0; i < steps; i++) {
            if (image.colors[(int) (from.x() + dx * i)][(int) (from.y() + dy * i)].equals(image.bg)) {
                return false;
            }
        }
        return true;
    }

    private List<Node> nearestNeighbors(Node node, int limit, int biggestNode) {
        int maxDist = maxDistance + biggestNode + node.radius();
        Node lowKey = new Node(
                new Coordinate(
                        node.coordinate().x() - maxDist, node.coordinate().y() - maxDist),
                0,
                0,
                null);
        Node highKey = new Node(
                new Coordinate(
                        node.coordinate().x() + maxDist, node.coordinate().y() + maxDist),
                0,
                0,
                null);
        Set<Node> closeNodes = new HashSet<>();
        closeNodes.addAll(sortedX.subSet(lowKey, highKey));
        closeNodes.retainAll(sortedY.subSet(lowKey, highKey));
        closeNodes.remove(node);
        return closeNodes.stream().limit(limit).collect(Collectors.toList());
    }

    private static double weightedSort(Node neighbour, Node node) {
        double dist = Math.sqrt(neighbour.coordinate().distSq(node.coordinate()));
        double factor =
                Math.min(1.0 / (neighbour.radius() * neighbour.radius()), 1.0 / (node.radius() * node.radius()));
        return (dist - node.radius() - neighbour.radius()) * factor;
    }

    // Undirected relationship to avoid duplicates
    record Relationship(Node from, Node to, int id) {
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
