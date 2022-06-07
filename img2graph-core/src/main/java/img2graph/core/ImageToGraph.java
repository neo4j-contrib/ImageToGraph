package img2graph.core;

import img2graph.core.FlowFill.Segment;
import img2graph.core.Graph.Node;
import img2graph.core.Graph.Relationship;
import img2graph.core.ImageReader.Image;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
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
                        arguments.simplifiedColors());
        RelationshipGenerator relationshipGenerator =
                new RelationshipGenerator(img, arguments.relMaxDist(), arguments.relsPerNode());
        List<Node> allNodes = new ArrayList<>();
        List<Relationship> allRels = new ArrayList<>();

        for (Segment segment : segments) {
            System.out.print("Generating graph for segment #" + segment.color + ". ");
            Collection<Node> nodes = nodeGenerator.generate(segment);
            System.out.print(nodes.size() + " nodes");
            Collection<Relationship> relationships = relationshipGenerator.generate(nodes);
            System.out.println(" & " + relationships.size() + " relationships");
            allNodes.addAll(nodes);
            allRels.addAll(relationships);
        }
        return new Graph(img.source, allNodes, allRels);
    }
}
