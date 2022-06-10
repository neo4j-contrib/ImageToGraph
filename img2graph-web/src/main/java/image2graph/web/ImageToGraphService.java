package image2graph.web;

import img2graph.core.Arguments;
import img2graph.core.Graph;
import img2graph.core.ImageToGraph;
import img2graph.core.Output;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import javax.inject.Singleton;

@Singleton
public class ImageToGraphService {
    private final ImageToGraph imageToGraph = new ImageToGraph();

    public String preview(UploadFormData input) {
        Arguments arguments = input.asArguments().withTargetResolution(350);
        try (InputStream inputStream =
                Thread.currentThread().getContextClassLoader().getResourceAsStream("preview.png")) {
            Graph graph = imageToGraph.process(arguments, inputStream);
            return Output.graphToSvg(graph, true, arguments.outline());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String convert(UploadFormData input) {
        Arguments arguments = input.asArguments();
        if (input.file == null) {
            return "No File Processed";
        }

        try (InputStream inputStream = Files.newInputStream(input.file.filePath())) {
            Graph graph = imageToGraph.process(arguments, inputStream);
            if (input.shouldRedirectToArrows()) {
                String json = Output.graphToJson(graph);
                return Output.arrowsUrl(json);
            } else {
                return Output.graphToSvg(graph, true, arguments.outline());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
