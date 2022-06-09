package img2graph.cli;

import img2graph.core.Arguments;
import img2graph.core.ImageToGraph;
import img2graph.core.Output;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import javax.imageio.ImageIO;
import picocli.CommandLine;

@CommandLine.Command(name = "img2graph", description = "Convert an image to a graph")
public final class Application implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "Path to image to operate on")
    private Path input;

    @CommandLine.Option(
            names = "--output",
            description = "Path to output directory. Default to current working directory")
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
            description = "Use simplified colors. (default: ${DEFAULT-VALUE})")
    public boolean simplifiedColors = Arguments.DEFAULT_ARGUMENTS.simplifiedColors();

    @CommandLine.Option(
            names = "--transparent-bg",
            description = "Transparent background for SVG output. (default: ${DEFAULT-VALUE})")
    public boolean transparentBg = Arguments.DEFAULT_ARGUMENTS.transparentBg();

    @CommandLine.Option(
            names = "--outline",
            description = "Outline on nodes. (default: ${DEFAULT-VALUE})")
    public boolean outline = Arguments.DEFAULT_ARGUMENTS.outline();

    @CommandLine.Option(
            names = "--open",
            description = "Opens the generated Graph in Arrows.app. (default: ${DEFAULT-VALUE})")
    public boolean open = false;

    public static void main(String[] args) {
        System.exit(new CommandLine(new Application()).execute(args));
    }

    @Override
    public Integer call() throws Exception {

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

        var graph = new ImageToGraph().process(args, new FileInputStream(input.toFile()));

        var fileName = input.getFileName().toString();
        if (fileName.lastIndexOf('.') != -1) {
            fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        }
        ImageIO.write(graph.image(), "png", output.resolve(fileName + "_simplified.png").toFile());

        System.out.printf(
                "Graph complete. Total %d nodes & %d relationships%n",
                graph.nodes().size(), graph.relationships().size());
        System.out.println("Files saved at: " + output);
        System.out.println(
                "Copy the json content and paste/import at https://arrows.app or use the generated"
                        + " svg directly.");

        var nodeOutput = output.resolve(fileName + "-nodes.csv").toAbsolutePath();
        Files.writeString(nodeOutput, Output.nodesToCsv(graph));
        var relOutput = output.resolve(fileName + "-rels.csv").toAbsolutePath();
        Files.writeString(relOutput, Output.relationshipsToCsv(graph));

        var jsonOutput = output.resolve(fileName + "-graph.json").toAbsolutePath();
        var json = Output.graphToJson(graph.nodes(), graph.relationships());
        Files.writeString(jsonOutput, json);

        var svgOutput = output.resolve(fileName + "-graph.svg").toAbsolutePath();
        Files.writeString(svgOutput, Output.graphToSvg(graph, transparentBg, outline));

        if (open) {
            openOrCopyToClipboard(fileName, Output.arrowsUrl(json));
        }

        return 0;
    }

    void openOrCopyToClipboard(String fileName, String arrowsUrl) throws IOException {

        if (GraphicsEnvironment.isHeadless() || !Desktop.isDesktopSupported()) {
            var urlOutput = output.resolve(fileName + "-url.txt").toAbsolutePath();
            Files.writeString(urlOutput, arrowsUrl);
            System.out.println(
                    "Running in a headless environment, URL has been written to " + urlOutput);
        } else if (!Desktop.isDesktopSupported()) {
            var defaultToolkit = Toolkit.getDefaultToolkit();
            var clipboard = defaultToolkit.getSystemClipboard();
            clipboard.setContents(new StringSelection(arrowsUrl), null);
            System.out.println(
                    "URL has been copied to your clipboard. Just use your preferred browser and"
                            + " paste there.");
        } else {
            var desktop = Desktop.getDesktop();
            desktop.browse(URI.create(arrowsUrl));
        }
    }
}
