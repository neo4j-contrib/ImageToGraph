package img2graph.server;

import img2graph.ImageToGraph;
import img2graph.Output;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import javax.inject.Singleton;
import javax.ws.rs.core.MultivaluedMap;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Singleton
public class FileUploadService {

    String UPLOAD_DIR = "/tmp/upload";

    public String uploadFile(MultipartFormDataInput input) {
        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<InputPart> inputParts = uploadForm.get("file");
        return inputParts.stream().flatMap(inputPart ->
                getFileName(inputPart.getHeaders()).map(fileName -> {
                    System.out.println("fileName = " + fileName);
                    try {
                        InputStream inputStream = inputPart.getBody(InputStream.class, null);
                        var result = new ImageToGraph().process(inputStream);
                        return Output.graphToSvg(result.img(), true, result.allNodes(), result.allRels());
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                })).findFirst().orElse("No File Processed");
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