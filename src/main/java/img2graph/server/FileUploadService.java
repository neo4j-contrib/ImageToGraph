package img2graph.server;

import img2graph.ImageToGraph;
import img2graph.Output;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.inject.Singleton;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@Singleton
public class FileUploadService {

    String UPLOAD_DIR = "/tmp/upload";

    public String preview(MultipartFormDataInput input) {
        ImageToGraph imageToGraph = fromForm(input);
        imageToGraph.targetResolution = 200;
        ImageToGraph.Result result;
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("preview.png")) {
            result = imageToGraph.process(inputStream);
            return Output.graphToSvg(result.img(), true, result.allNodes(), result.allRels());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Preview not available";
    }

    public String uploadFile(MultipartFormDataInput input) {
        ImageToGraph imageToGraph = fromForm(input);
        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<InputPart> inputParts = uploadForm.get("file");
        return inputParts.stream()
                .flatMap(inputPart -> getFileName(inputPart.getHeaders()).map(fileName -> {
                    System.out.println("fileName = " + fileName);
                    try {
                        ImageToGraph.Result result;
                        try (InputStream inputStream = inputPart.getBody(InputStream.class, null)) {
                            result = imageToGraph.process(inputStream);
                        }
                        return Output.graphToSvg(result.img(), true, result.allNodes(), result.allRels());
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }))
                .findFirst()
                .orElse("No File Processed");
    }

    private ImageToGraph fromForm(MultipartFormDataInput input) {
        ImageToGraph imageToGraph = new ImageToGraph();
        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        try {
            boolean useSimpleColors = uploadForm.containsKey("simple-color");
            int nodeMax = uploadForm.get("node-max").get(0).getBody(Integer.class, null);
            int nodeMin = uploadForm.get("node-min").get(0).getBody(Integer.class, null);
            int nodePadding = uploadForm.get("node-padding").get(0).getBody(Integer.class, null);
            int colorDepth = uploadForm.get("color-depth").get(0).getBody(Integer.class, null);
            int relAvg = uploadForm.get("rel-avg").get(0).getBody(Integer.class, null);
            int relLength = uploadForm.get("rel-max-length").get(0).getBody(Integer.class, null);

            imageToGraph.simplifiedColors = useSimpleColors;
            imageToGraph.nodeMaxRad = Math.max(nodeMax, nodeMin);
            imageToGraph.nodeMinRad = Math.min(nodeMax, nodeMin);
            imageToGraph.nodePadding = nodePadding;
            imageToGraph.colorDepth = colorDepth;
            imageToGraph.relsPerNode = relAvg;
            imageToGraph.relMaxDist = relLength;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageToGraph;
    }

    private void writeFile(InputStream inputStream,String fileName) throws IOException {
        byte[] bytes = IOUtils.toByteArray(inputStream);
        File customDir = new File(UPLOAD_DIR);
        fileName = customDir.getAbsolutePath() + File.separator + fileName;
        Files.write(Paths.get(fileName), bytes, StandardOpenOption.CREATE_NEW);
    }

    private Stream<String> getFileName(MultivaluedMap<String, String> header) {
        String[] contentDisposition = header.getFirst("Content-Disposition").split(";");
        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {
                String name = filename.split("=")[1];
                String cleanedName = name.trim().replaceAll("\"", "");
                return Stream.of(cleanedName);
            }
        }
        return Stream.empty();
    }
}