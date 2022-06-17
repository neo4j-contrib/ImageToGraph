package img2graph.core;

import img2graph.core.FlowFill.Segment;
import img2graph.core.Graph.Node;
import img2graph.core.Graph.Relationship;
import img2graph.core.ImageReader.Image;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class ImageToGraph {

    public Graph process(Arguments arguments, InputStream stream) {
        System.out.println("Reading, scaling, simplifying image");
        Image img =
                new ImageReader(arguments.targetResolution(), arguments.colorDepth())
                        .readImage(stream);

        System.out.println("Find segments");
        FlowFill flowFill = new FlowFill(img);

        List<Segment> segments = flowFill.getSegments();
        System.out.printf("Found %s segments%n", segments.size());
        if (!arguments.keepBackground()) {
            segments.removeIf(segment -> segment.color.equals(img.bg));
            System.out.printf(
                    "Found %s segments without background color (#%s)%n", segments.size(), img.bg);
        }
        segments.removeIf(segment -> segment.pixels.size() < 10);
        System.out.printf("Found %s segments after size filter%n", segments.size());

        NodeGenerator nodeGenerator =
                new NodeGenerator(
                        img,
                        arguments.nodeMinRad(),
                        arguments.nodeMaxRad(),
                        arguments.nodePadding(),
                        arguments.numSuperNodes(),
                        arguments.simplifiedColors(),
                        segments.stream().mapToLong(s1 -> s1.pixels.size()).sum());
        RelationshipGenerator relationshipGenerator =
                new RelationshipGenerator(img, arguments.relMaxDist(), arguments.relsPerNode());
        List<Node> allNodes = new ArrayList<>();
        List<Relationship> allRels = new ArrayList<>();
        segments.sort(Comparator.comparingLong((Segment s) -> s.pixels.size()).reversed());
        for (Segment segment : segments) {
            Collection<Node> nodes = nodeGenerator.generate(segment);
            Collection<Relationship> relationships = relationshipGenerator.generate(nodes);
            if (nodes.size() > 0) {
                System.out.printf(
                        "Generated graph for segment #%s. %s nodes & %s relationships%n",
                        segment.color, nodes.size(), relationships.size());
            }
            allNodes.addAll(nodes);
            allRels.addAll(relationships);
        }
        return new Graph(img.source, allNodes, allRels);
    }
}
