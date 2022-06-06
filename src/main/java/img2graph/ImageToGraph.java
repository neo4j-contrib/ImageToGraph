package img2graph;

import img2graph.FlowFill.Segment;
import img2graph.ImageReader.Image;
import img2graph.NodeGenerator.Node;
import img2graph.RelationshipGenerator.Relationship;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import javax.imageio.ImageIO;
import picocli.CommandLine;

@CommandLine.Command(name = "convert", description = "Convert an image to a graph")
public class ImageToGraph implements Callable<Object> {

    @CommandLine.Parameters(index = "0", description = "Path to image to operate on")
    private Path input;

    @CommandLine.Option(
            names = "--output",
            description = "Path to output directory. Default to current " + "working directory")
    private Path output = Path.of("").toAbsolutePath();

    @CommandLine.Option(names = "--keep-bg", description = "Keep the background")
    private boolean keepBackground;

    @CommandLine.Option(names = "--node-min-radius", description = "Node minimum radius. (default: ${DEFAULT-VALUE})")
    public int nodeMinRad = 3;

    @CommandLine.Option(names = "--node-max-radius", description = "Node maximum radius. (default: ${DEFAULT-VALUE})")
    public int nodeMaxRad = 10;

    @CommandLine.Option(names = "--node-padding", description = "Node padding. (default: ${DEFAULT-VALUE})")
    public int nodePadding = 2;

    @CommandLine.Option(
            names = "--rel-max-distance",
            description = "Relationship maximum distance. (default: ${DEFAULT-VALUE})")
    public int relMaxDist = 20;

    @CommandLine.Option(
            names = "--rels-per-node",
            description = "Avg relationships per node. (default: ${DEFAULT-VALUE})")
    public int relsPerNode = 2;

    @CommandLine.Option(
            names = "--target-res",
            description = "Target resolution. Changes size of graph. (default: ${DEFAULT-VALUE})")
    public int targetResolution = 1024;

    @CommandLine.Option(
            names = "--color-depth",
            description = "Color depth for simplified image. (default: ${DEFAULT-VALUE})")
    public int colorDepth = 4;

    @CommandLine.Option(
            names = "--simplified-colors",
            description = "Use simplified colors. (default: " + "${DEFAULT-VALUE})")
    public boolean simplifiedColors;

    @CommandLine.Option(
            names = "--transparent-bg",
            description = "Transparent background for SVG output. (default: " + "${DEFAULT-VALUE})")
    private boolean transparentBg;

    @CommandLine.Option(names = "--outline", description = "Outline on nodes (default: " + "${DEFAULT-VALUE})")
    public boolean outline;

    public record Result(Image img, List<Node> allNodes, List<Relationship> allRels) {}

    @Override
    public Object call() throws Exception {
        if (!Files.exists(input)) {
            System.err.println(input.toString() + " does not exist.");
            System.exit(1);
        }
        if (!Files.exists(output)) {
            Files.createDirectories(output);
        }
        output = output.toAbsolutePath();

        Result result = process(new FileInputStream(input.toFile()));

        String fileName = input.getFileName().toString();
        if (fileName.lastIndexOf('.') != -1) {
            fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        }
        ImageIO.write(
                result.img().source,
                "png",
                output.resolve(fileName + "_simplified.png").toFile());

        System.out.printf(
                "Graph complete. Total %d nodes & %d relationships%n",
                result.allNodes().size(), result.allRels().size());
        System.out.println("Files saved at: " + output);
        System.out.println("Copy the json content and paste/import at https://arrows.app/ or use the url");
        System.out.println("Or use the generated svg directly");
        Path nodeOutput = output.resolve(fileName + "-nodes.csv").toAbsolutePath();
        Path relOutput = output.resolve(fileName + "-rels.csv").toAbsolutePath();
        Path urlOutput = output.resolve(fileName + "-url.txt").toAbsolutePath();
        Path jsonOutput = output.resolve(fileName + "-graph.json").toAbsolutePath();
        Path svgOutput = output.resolve(fileName + "-graph.svg").toAbsolutePath();
        Files.writeString(nodeOutput, Output.nodesToCsv(result.allNodes()));
        Files.writeString(relOutput, Output.relationshipsToCsv(result.allRels()));
        String json = Output.graphToJson(result.allNodes(), result.allRels());
        Files.writeString(jsonOutput, json);
        Files.writeString(urlOutput, Output.arrowsUrl(json));
        Files.writeString(
                svgOutput,
                Output.graphToSvg(result.img(), transparentBg, outline, result.allNodes(), result.allRels()));
        return null;
    }

    public Result process(InputStream stream) {
        System.out.println("Reading, scaling, simplifying image");
        Image img = new ImageReader(targetResolution, colorDepth).readImage(stream);

        System.out.println("Find segments");
        FlowFill flowFill = new FlowFill(img);

        List<Segment> segments = flowFill.getSegments();
        System.out.printf("Found %s segments%n", segments.size());
        if (!keepBackground) {
            segments.removeIf(segment -> segment.color.equals(img.bg));
            System.out.printf("Found %s segments without background color (#%s)%n", segments.size(), img.bg);
        }
        segments.removeIf(segment -> segment.pixels.size() < 10);
        System.out.printf("Found %s segments after size filter%n", segments.size());

        NodeGenerator nodeGenerator = new NodeGenerator(img, nodeMinRad, nodeMaxRad, nodePadding, simplifiedColors);
        RelationshipGenerator relationshipGenerator = new RelationshipGenerator(img, relMaxDist, relsPerNode);
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
        return new Result(img, allNodes, allRels);
    }
}
