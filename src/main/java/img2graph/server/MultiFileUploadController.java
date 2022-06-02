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
                 <style type='text/css'>
                 .upload-btn-wrapper {
                   position: relative;
                   overflow: hidden;
                   display: inline-block;
                 }
                 
                 .btn {
                   border: 2px solid gray;
                   color: gray;
                   background-color: white;
                   padding: 8px 20px;
                   border-radius: 8px;
                   font-size: 20px;
                   font-weight: bold;
                 }
                 
                 .upload-btn-wrapper input[type=file] {
                   font-size: 100px;
                   position: absolute;
                   left: 0;
                   top: 0;
                   opacity: 0;
                 }
                 </style>
                 <div class="upload-btn-wrapper">
                     <form method='post' enctype='multipart/form-data'>
                        <button class='btn'>Upload a file</button>
                        <input type='file' name='file' accept='image/*' multiple='false' onchange="submit()" />
                     </form>
                  </div>
                """;
    }
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("image/svg+xml")
    public Response fileUpload(@MultipartForm MultipartFormDataInput input) {
        return Response.ok().header("Content-Disposition","inline").entity(fileUploadService.uploadFile(input)).build();
    }
}