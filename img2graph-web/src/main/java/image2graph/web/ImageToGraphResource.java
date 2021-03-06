package image2graph.web;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;
import java.net.URI;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jboss.resteasy.reactive.MultipartForm;

@Path("/")
public class ImageToGraphResource {

    @Inject ImageToGraphService imageToGraphService;

    @Inject Template index;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance index() {
        return index.instance();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("image/svg+xml")
    public Response convert(@MultipartForm UploadFormData input) {
        String result = imageToGraphService.convert(input);
        if (input.shouldRedirectToArrows()) {
            return Response.seeOther(URI.create(result)).build();
        } else {
            return Response.ok().header("Content-Disposition", "inline").entity(result).build();
        }
    }

    @POST
    @Path("/preview")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("image/svg+xml")
    public Uni<Response> preview(@MultipartForm UploadFormData input) {
        return Uni.createFrom()
                .item(() -> imageToGraphService.preview(input))
                .map(
                        content ->
                                Response.ok()
                                        .header("Content-Disposition", "inline")
                                        .entity(content)
                                        .build());
    }
}
