package seth.seth.webservice;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;
import de.hu.berlin.wbi.objects.MutationMention;
import seth.SETH;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;



@Path( "message" )
public class MessageResource     {

    public static SETH seth = new SETH("resources/mutations.txt", true, true);

    //http://localhost:8080/rest/message/get/p.T123C%20and%20Val158Tyr
    //curl http://localhost:8080/rest/message/get/p.T123C%20and%20Val158Tyr | jq
    @GET
    @Path("/get/{param}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public List<MutationMention> getMutationsGet(@PathParam("param") String msg) {

        return seth.findMutations(msg);

    }

    //curl -X POST http://localhost:8080/rest/message/post/p.T123C%20and%20Val158Tyr
    @POST
    @Path("/post/{param}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createTrackInJSON(@PathParam("param") String msg) {

        List<MutationMention> mentions = seth.findMutations(msg);

        return Response.status(200).entity(mentions).build();

    }

    public static void main(String[] args) throws SQLException, IOException {
        HttpServer server = HttpServerFactory.create( "http://localhost:8080/rest" );
        server.start();
    }
}