package img2graph.server;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@Path("/preview")
public class Preview {

    @Inject
    FileUploadService fileUploadService;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("image/svg+xml")
    public Response fileUpload(@MultipartForm MultipartFormDataInput input) {
        return Response.ok()
                .header("Content-Disposition", "inline")
                .entity(fileUploadService.preview(input))
                .build();
    }
}
