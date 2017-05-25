package py.pol.una.ii.pw.rest;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * Created by ruben on 10/05/17.
 */
@Path("/injection")
@RequestScoped
public class InjectionResourceREST {

    @POST
    @Path("/command")
    @Consumes(MediaType.TEXT_PLAIN)
    public String[] crearCompra(String directorio) throws IOException {
        Runtime r = Runtime.getRuntime();
        String [] comando =new String[] {"/bin/sh", "-c", "mkdir "+ System.getProperty("user.home") + "/" + directorio};
        r.exec(comando);
        return comando;
    }
}
