package img2graph.core;

import img2graph.core.FlowFill.Coordinate;
import img2graph.core.Graph.Node;
import img2graph.core.Graph.Relationship;
import img2graph.core.ImageReader.Color;
import java.awt.*;
import java.util.Base64;
import java.util.Collection;
import org.jfree.svg.SVGGraphics2D;

public final class Output {

    public static String graphToSvg(Graph graph, boolean transparentBg, boolean outline) {
        var width = graph.image().getWidth();
        var height = graph.image().getHeight();
        SVGGraphics2D svg = new SVGGraphics2D(width, height);
        if (!transparentBg) {
            svg.setPaint(java.awt.Color.WHITE);
            svg.fillRect(0, 0, width, height);
        }

        for (Relationship relationship : graph.relationships()) {
            svg.setPaint(new java.awt.Color(relationship.from().color().raw()));
            Coordinate from = relationship.from().coordinate();
            Coordinate to = relationship.to().coordinate();
            svg.drawLine(from.x(), from.y(), to.x(), to.y());
        }
        svg.setStroke(new BasicStroke(.50f));
        for (Node node : graph.nodes()) {
            int radius = node.radius();
            int side = radius * 2;
            Color color = node.color();
            svg.setPaint(new java.awt.Color(color.raw()));
            svg.fillOval(
                    node.coordinate().x() - radius, node.coordinate().y() - radius, side, side);
            if (outline) {
                float factor = 0.5f / 255.f;
                svg.setPaint(
                        new java.awt.Color(
                                color.r() * factor, color.g() * factor, color.b() * factor));
                svg.drawOval(
                        node.coordinate().x() - radius, node.coordinate().y() - radius, side, side);
            }
        }

        return svg.getSVGElement();
    }

    public static String nodesToCsv(Graph graph) {
        StringBuilder nodesCsv = new StringBuilder();
        for (Node node : graph.nodes()) {
            nodesCsv.append(node).append(",").append(node.color()).append('\n');
        }
        return nodesCsv.toString();
    }

    public static String relationshipsToCsv(Graph graph) {
        StringBuilder relCsv = new StringBuilder();
        for (Relationship rel : graph.relationships()) {
            relCsv.append(rel.from().id())
                    .append(",")
                    .append(rel.to().id())
                    .append(",")
                    .append(rel.from().color())
                    .append('\n');
        }
        return relCsv.toString();
    }

    public static String graphToJson(
            Collection<Node> nodes, Collection<Relationship> relationships) {
        StringBuilder nodesJson = new StringBuilder();
        for (Node node : nodes) {
            nodesJson.append(nodeAsJson(node, node.color())).append(",");
        }

        StringBuilder relJson = new StringBuilder();
        for (Relationship rel : relationships) {
            relJson.append(relAsJson(rel, rel.from().color())).append(",");
        }

        // Delete last commas
        if (!nodesJson.isEmpty()) {
            nodesJson.deleteCharAt(nodesJson.length() - 1);
        }
        if (!relJson.isEmpty()) {
            relJson.deleteCharAt(relJson.length() - 1);
        }
        return String.format(jsonTemplate, nodesJson, relJson);
    }

    public static String arrowsUrl(String json) {
        return "https://arrows.app/#/import/json=" + base64Encode(json);
    }

    private static String base64Encode(String string) {
        return Base64.getEncoder().encodeToString(string.replaceAll("\\s", "").getBytes());
    }

    private static String nodeAsJson(Node node, Color color) {
        return String.format(
                nodeTemplate,
                node.id(),
                node.coordinate().x(),
                node.coordinate().y(),
                node.radius(),
                color);
    }

    private static String relAsJson(Relationship rel, Color color) {
        return String.format(relTemplate, rel.id(), rel.from().id(), rel.to().id(), color);
    }

    private static final String jsonTemplate =
            """
            {
                "graph": {
                    "nodes": [
                    %s
                    ],
                    "relationships": [
                    %s
                    ],
                    "style": {
                        "arrow-width": 1,
                        "border-width": 0,
                        "directionality": "undirected",
                        "margin-start": 0,
                        "margin-end": 0
                    }
                },
                "diagramName": "img2graph"
            }
            """;

    private static final String nodeTemplate =
            """
                {
                  "id": "n%d",
                  "position": {
                    "x": %d,
                    "y": %d
                  },
                  "style": {
                    "radius": %d,
                    "node-color": "#%s"
                  }
                }
            """;

    private static final String relTemplate =
            """
                {
                  "id": "r%d",
                  "fromId": "n%d",
                  "toId": "n%d",
                  "style": {
                    "arrow-color": "#%s"
                  }
                }
            """;
}
