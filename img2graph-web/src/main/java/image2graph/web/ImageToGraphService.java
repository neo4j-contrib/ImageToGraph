package image2graph.web;

import img2graph.core.ImageToGraph;
import img2graph.core.Output;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.common.annotation.Blocking;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import javax.enterprise.event.Observes;
import javax.inject.Singleton;

@Singleton
public class ImageToGraphService {

    private final ImageToGraph imageToGraph = new ImageToGraph();
    private byte[] previewData;

    void onStart(@Observes StartupEvent ev) {
        try (var inputStream =
                Thread.currentThread().getContextClassLoader().getResourceAsStream("preview.png")) {
            this.previewData = inputStream.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String preview(UploadFormData input) {
        var arguments = input.asArguments().withTargetResolution(350);
        var graph = imageToGraph.process(arguments, new ByteArrayInputStream(previewData));
        return Output.graphToSvg(graph, true, arguments.outline());
    }

    @Blocking
    public String convert(UploadFormData input) {
        var arguments = input.asArguments();
        if (input.file == null) {
            return "No File Processed";
        }

        try (var inputStream = Files.newInputStream(input.file.filePath())) {
            var graph = imageToGraph.process(arguments, inputStream);
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
