package img2graph;

import img2graph.FlowFill.Coordinate;
import img2graph.ImageReader.Color;
import img2graph.ImageReader.Image;
import img2graph.NodeGenerator.Node;
import img2graph.RelationshipGenerator.Relationship;
import java.awt.BasicStroke;
import java.util.Base64;
import java.util.Collection;
import org.jfree.svg.SVGGraphics2D;

public class Output {

    public static String graphToSvg(
            Image img,
            boolean transparentBg,
            boolean outline,
            Collection<Node> nodes,
            Collection<Relationship> relationships) {
        SVGGraphics2D svg = new SVGGraphics2D(img.width, img.height);
        if (!transparentBg) {
            svg.setPaint(java.awt.Color.WHITE);
            svg.fillRect(0, 0, img.width, img.height);
        }

        for (Relationship relationship : relationships) {
            svg.setPaint(new java.awt.Color(relationship.from().color().raw()));
            Coordinate from = relationship.from().coordinate();
            Coordinate to = relationship.to().coordinate();
            svg.drawLine(from.x(), from.y(), to.x(), to.y());
        }
        svg.setStroke(new BasicStroke(.50f));
        for (Node node : nodes) {
            int radius = node.radius();
            int side = radius * 2;
            Color color = node.color();
            svg.setPaint(new java.awt.Color(color.raw()));
            svg.fillOval(node.coordinate().x() - radius, node.coordinate().y() - radius, side, side);
            if (outline) {
                float factor = 0.5f / 255.f;
                svg.setPaint(new java.awt.Color(color.r() * factor, color.g() * factor, color.b() * factor));
                svg.drawOval(node.coordinate().x() - radius, node.coordinate().y() - radius, side, side);
            }
        }

        return svg.getSVGElement();
    }

    static String nodesToCsv(Collection<Node> nodes) {
        StringBuilder nodesCsv = new StringBuilder();
        for (Node node : nodes) {
            nodesCsv.append(node).append(",").append(node.color()).append('\n');
        }
        return nodesCsv.toString();
    }

    static String relationshipsToCsv(Collection<Relationship> relationships) {
        StringBuilder relCsv = new StringBuilder();
        for (Relationship rel : relationships) {
            relCsv.append(rel.from().id())
                    .append(",")
                    .append(rel.to().id())
                    .append(",")
                    .append(rel.from().color())
                    .append('\n');
        }
        return relCsv.toString();
    }

    static String graphToJson(Collection<Node> nodes, Collection<Relationship> relationships) {
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

    static String arrowsUrl(String json) {
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
