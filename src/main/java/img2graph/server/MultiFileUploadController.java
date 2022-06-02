package img2graph.server;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class MultiFileUploadController {

    @Inject
    FileUploadService fileUploadService;

    @GET()
    public String index() {
        return """
                <form method='post' enctype='multipart/form-data'>
                <input type='file' name='file' accept='image/*'>
                <button>Upload Image</button>
                </form>
               """;
    }
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("image/svg+xml")
    public Response fileUpload(@MultipartForm MultipartFormDataInput input) {
        return Response.ok().header("Content-Disposition","inline").entity(fileUploadService.uploadFile(input)).build();
    }
}