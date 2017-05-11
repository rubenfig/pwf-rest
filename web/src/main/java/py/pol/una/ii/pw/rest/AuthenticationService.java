package py.pol.una.ii.pw.rest;

/**
 * Created by carlitos on 25/04/17.
 */

import org.apache.commons.codec.binary.Base64;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.StringTokenizer;

@Path("/login")
@RequestScoped
public class AuthenticationService {


    public boolean authenticate(String authCredentials) {

        if (null == authCredentials)
            return false;
        // header value format will be "Basic encodedstring" for Basic
        // authentication. Example "Basic YWRtaW46YWRtaW4="
        final String encodedUserPassword = authCredentials.replaceFirst("Basic"
                + " ", "");
        String usernameAndPassword = "";
        try {
            byte[] decodedBytes = Base64.decodeBase64(
                    encodedUserPassword);
            usernameAndPassword = new String(decodedBytes, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        final StringTokenizer tokenizer = new StringTokenizer(
                usernameAndPassword, ":");
        final String username = tokenizer.nextToken();
        final String password = tokenizer.nextToken();

        // we have fixed the userid and password as admin
        // call some UserService/LDAP here
        boolean authenticationStatus = "admin".equals(username)
                && "admin".equals(password);
        return authenticationStatus;
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String login (@FormParam("username") String username,
                         @FormParam("password") String password){
        if (username.equals("admin") && password.equals("admin") ){
            return "Basic YWRtaW46YWRtaW4=";}
        else
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
}
