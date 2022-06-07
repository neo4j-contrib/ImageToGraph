package img2graph.cli;

import img2graph.core.Arguments;
import img2graph.core.Graph;
import img2graph.core.ImageToGraph;
import img2graph.core.Output;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import javax.imageio.ImageIO;
import picocli.CommandLine;

@CommandLine.Command(name = "img2graph", description = "Convert an image to a graph")
public final class Application implements Callable<Object> {

    @CommandLine.Parameters(index = "0", description = "Path to image to operate on")
    private Path input;

    @CommandLine.Option(
            names = "--output",
            description = "Path to output directory. Default to current " + "working directory")
    private Path output = Path.of("").toAbsolutePath();

    @CommandLine.Option(names = "--keep-bg", description = "Keep the background")
    public boolean keepBackground = Arguments.DEFAULT_ARGUMENTS.keepBackground();

    @CommandLine.Option(
            names = "--node-min-radius",
            description = "Node minimum radius. (default: ${DEFAULT-VALUE})")
    public int nodeMinRad = Arguments.DEFAULT_ARGUMENTS.nodeMinRad();

    @CommandLine.Option(
            names = "--node-max-radius",
            description = "Node maximum radius. (default: ${DEFAULT-VALUE})")
    public int nodeMaxRad = Arguments.DEFAULT_ARGUMENTS.nodeMaxRad();

    @CommandLine.Option(
            names = "--node-padding",
            description = "Node padding. (default: ${DEFAULT-VALUE})")
    public int nodePadding = Arguments.DEFAULT_ARGUMENTS.nodePadding();

    @CommandLine.Option(
            names = "--rel-max-distance",
            description = "Relationship maximum distance. (default: ${DEFAULT-VALUE})")
    public int relMaxDist = Arguments.DEFAULT_ARGUMENTS.relMaxDist();

    @CommandLine.Option(
            names = "--rels-per-node",
            description = "Avg relationships per node. (default: ${DEFAULT-VALUE})")
    public int relsPerNode = Arguments.DEFAULT_ARGUMENTS.relsPerNode();

    @CommandLine.Option(
            names = "--target-res",
            description = "Target resolution. Changes size of graph. (default: ${DEFAULT-VALUE})")
    public int targetResolution = Arguments.DEFAULT_ARGUMENTS.targetResolution();

    @CommandLine.Option(
            names = "--color-depth",
            description = "Color depth for simplified image. (default: ${DEFAULT-VALUE})")
    public int colorDepth = Arguments.DEFAULT_ARGUMENTS.colorDepth();

    @CommandLine.Option(
            names = "--simplified-colors",
            description = "Use simplified colors. (default: " + "${DEFAULT-VALUE})")
    public boolean simplifiedColors = Arguments.DEFAULT_ARGUMENTS.simplifiedColors();

    @CommandLine.Option(
            names = "--transparent-bg",
            description = "Transparent background for SVG output. (default: " + "${DEFAULT-VALUE})")
    private boolean transparentBg = Arguments.DEFAULT_ARGUMENTS.transparentBg();

    @CommandLine.Option(
            names = "--outline",
            description = "Outline on nodes (default: " + "${DEFAULT-VALUE})")
    public boolean outline = Arguments.DEFAULT_ARGUMENTS.outline();

    public static void main(String[] args) {
        System.exit(new CommandLine(new Application()).execute(args));
    }

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

        var args =
                new Arguments(
                        keepBackground,
                        nodeMinRad,
                        nodeMaxRad,
                        nodePadding,
                        relMaxDist,
                        relsPerNode,
                        targetResolution,
                        colorDepth,
                        simplifiedColors,
                        transparentBg,
                        outline);
        System.out.println(args);
        Graph graph = new ImageToGraph().process(args, new FileInputStream(input.toFile()));

        String fileName = input.getFileName().toString();
        if (fileName.lastIndexOf('.') != -1) {
            fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        }
        ImageIO.write(graph.image(), "png", output.resolve(fileName + "_simplified.png").toFile());

        System.out.printf(
                "Graph complete. Total %d nodes & %d relationships%n",
                graph.nodes().size(), graph.relationships().size());
        System.out.println("Files saved at: " + output);
        System.out.println(
                "Copy the json content and paste/import at https://arrows.app/ or use the url");
        System.out.println("Or use the generated svg directly");
        Path nodeOutput = output.resolve(fileName + "-nodes.csv").toAbsolutePath();
        Path relOutput = output.resolve(fileName + "-rels.csv").toAbsolutePath();
        Path urlOutput = output.resolve(fileName + "-url.txt").toAbsolutePath();
        Path jsonOutput = output.resolve(fileName + "-graph.json").toAbsolutePath();
        Path svgOutput = output.resolve(fileName + "-graph.svg").toAbsolutePath();
        Files.writeString(nodeOutput, Output.nodesToCsv(graph));
        Files.writeString(relOutput, Output.relationshipsToCsv(graph));
        String json = Output.graphToJson(graph.nodes(), graph.relationships());
        Files.writeString(jsonOutput, json);
        Files.writeString(urlOutput, Output.arrowsUrl(json));
        Files.writeString(svgOutput, Output.graphToSvg(graph, transparentBg, outline));
        return null;
    }
}
