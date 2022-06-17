package img2graph.core;

import static java.util.Comparator.comparingInt;

import img2graph.core.FlowFill.Coordinate;
import img2graph.core.Graph.Node;
import img2graph.core.ImageReader.Color;
import img2graph.core.ImageReader.Image;
import java.util.*;

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

    Collection<Graph.Relationship> generate(Collection<Node> nodes) {
        Set<Graph.Relationship> relationships = new HashSet<>();
        sortedX =
                new TreeSet<>(
                        comparingInt((Node n) -> n.coordinate().x())
                                .thenComparingInt(n -> n.coordinate().y()));
        sortedY =
                new TreeSet<>(
                        comparingInt((Node n) -> n.coordinate().y())
                                .thenComparingInt(n -> n.coordinate().x()));
        sortedX.addAll(nodes);
        sortedY.addAll(nodes);
        int biggestNode = nodes.stream().map(Graph.Node::radius).max(Integer::compare).orElse(0);
        for (Node node : nodes) {
            List<Node> neighbors = nearestNeighbors(node, biggestNode);
            neighbors.sort(Comparator.comparingDouble(n -> weightedSort(n, node)));
            int relsAdded = 0;
            for (int i = 0; relsAdded < numRels && i < neighbors.size(); i++) {
                Graph.Relationship relationship =
                        new Graph.Relationship(node, neighbors.get(i), relId++);
                if (validate(relationship)) {
                    relsAdded++;
                    relationships.add(relationship);
                }
            }
        }
        return relationships.stream().toList();
    }

    private boolean validate(Graph.Relationship relationship) {
        Coordinate from = relationship.from().coordinate();
        Coordinate to = relationship.to().coordinate();
        int diffX = to.x() - from.x();
        int diffY = to.y() - from.y();
        double dist =
                Math.sqrt(from.distSq(to))
                        - relationship.from().radius()
                        - relationship.to().radius();
        if (dist > maxDistance) {
            return false;
        }
        int steps = Math.max(Math.abs(diffX), Math.abs(diffY));
        double dx = diffX / (double) steps;
        double dy = diffY / (double) steps;
        Color simpleColor = image.colors[from.x()][from.y()];
        for (int i = 0; i < steps; i++) {
            if (!image.colors[(int) (from.x() + dx * i)][(int) (from.y() + dy * i)].equals(
                    simpleColor)) {
                return false;
            }
        }
        return true;
    }

    private List<Node> nearestNeighbors(Node node, int biggestNode) {
        int maxDist = maxDistance + biggestNode + node.radius();
        Node lowKey =
                new Node(
                        new Coordinate(
                                node.coordinate().x() - maxDist, node.coordinate().y() - maxDist),
                        0,
                        0,
                        null);
        Node highKey =
                new Node(
                        new Coordinate(
                                node.coordinate().x() + maxDist, node.coordinate().y() + maxDist),
                        0,
                        0,
                        null);
        Set<Node> closeNodes = new HashSet<>(sortedX.subSet(lowKey, highKey));
        closeNodes.retainAll(sortedY.subSet(lowKey, highKey));
        closeNodes.remove(node);
        return new ArrayList<>(closeNodes);
    }

    private static double weightedSort(Node neighbour, Node node) {
        double dist = Math.sqrt(neighbour.coordinate().distSq(node.coordinate()));
        double factor =
                Math.min(
                        1.0 / (neighbour.radius() * neighbour.radius()),
                        1.0 / (node.radius() * node.radius()));
        return (dist - node.radius() - neighbour.radius()) * factor;
    }
}
