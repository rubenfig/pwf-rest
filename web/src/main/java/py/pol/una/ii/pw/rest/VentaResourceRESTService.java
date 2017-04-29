package py.pol.una.ii.pw.rest;

import com.google.gson.Gson;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import py.pol.una.ii.pw.data.VentaRepository;
import py.pol.una.ii.pw.model.Producto;
import py.pol.una.ii.pw.model.ProductoComprado;
import py.pol.una.ii.pw.model.Venta;
import py.pol.una.ii.pw.service.VentaMasivaRegistration;
import py.pol.una.ii.pw.service.VentaRegistration;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;

@Path("/ventas")
@RequestScoped
public class VentaResourceRESTService {
	@Inject
    private Logger log;

    @Inject
    private Validator validator;

    @Inject
    private VentaRepository repository;

    @Inject
    VentaRegistration registration;
    
    @Inject
    VentaMasivaRegistration registrationMasivo;

    @Context
    private HttpServletRequest request;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Venta> listAllVentas() {
        return repository.findAllOrderedById(null);
    }

    @GET
    @Path("/{id:[0-9][0-9]*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Venta lookupVentaById(@PathParam("id") long id) {
        Venta venta = repository.findById(id);
        if (venta == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return venta;
    }

    /**
     * Creates a new venta from the values provided. Performs validation, and will return a JAX-RS response with either 200 ok,
     * or with a map of fields, and related errors.
     */

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crearVenta(Venta venta) {

        Response.ResponseBuilder builder = null;

        try {
            validateVenta(venta);
            registrationMasivo.registerSingle(venta);
            // Create an "ok" response
            builder = Response.ok();
        } catch (ConstraintViolationException ce) {
            // Handle bean validation issues
            builder = createViolationResponse(ce.getConstraintViolations());
        } catch (Exception e) {
            // Handle generic exceptions
            Map<String, String> responseObj = new HashMap<String, String>();
            responseObj.put("error", e.getMessage());
            builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
        }

        return builder.build();
    }

    @POST
    @Path("/iniciar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createVenta(Venta venta) {

        Response.ResponseBuilder builder = null;

        try {
            // Validates venta using bean validation
            validateVenta(venta);
            VentaRegistration bean = (VentaRegistration) request.getSession().getAttribute("venta");
            System.out.println("Sesion numero:"+request.getSession().getId());
            if(bean == null){
                // EJB is not present in the HTTP session
                // so let's fetch a new one from the container
                try {
                    InitialContext ic = new InitialContext();
                    bean = (VentaRegistration)
                            ic.lookup("java:global/EjbJaxRS-ear/EjbJaxRS-ejb/VentaRegistration");

                    // put EJB in HTTP session for future servlet calls
                    request.getSession().setAttribute(
                            "venta",
                            bean);
                    bean.register(venta);
                    System.out.println("Creo el bean: "+request.getSession().getId());
                } catch (NamingException e) {
                    System.out.println("No creo el bean");
                    throw new ServletException(e);

                }
            }else{
                Map<String, String> response = new HashMap<String, String>();
                response.put("error", "ya existe la venta");
            }
            // Create an "ok" response
            builder = Response.ok();
        } catch (ConstraintViolationException ce) {
            // Handle bean validation issues
            builder = createViolationResponse(ce.getConstraintViolations());
        } catch (Exception e) {
            // Handle generic exceptions
            Map<String, String> responseObj = new HashMap<String, String>();
            responseObj.put("error", e.getMessage());
            builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
        }

        return builder.build();
    }


    /**
     * Creates a new venta from the values provided. Performs validation, and will return a JAX-RS response with either 200 ok,
     * or with a map of fields, and related errors.
     */
    @POST
    @Path("/agregar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response agregarCarrito(ProductoComprado pc) {

        Response.ResponseBuilder builder = null;

        try {
            // Validates venta using bean validation
            VentaRegistration bean = (VentaRegistration) request.getSession().getAttribute("venta");
            System.out.println("Sesion numero:"+request.getSession().getId());
            if(bean != null){
                bean.agregarCarrito(pc);
                builder = Response.ok();
            }else{

                Map<String, String> responseObj = new HashMap<String, String>();
                responseObj.put("error", "No existe la venta");
                builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
            }
            // Create an "ok" response
        } catch (ConstraintViolationException ce) {
            // Handle bean validation issues
            builder = createViolationResponse(ce.getConstraintViolations());
        } catch (Exception e) {
            // Handle generic exceptions
            Map<String, String> responseObj = new HashMap<String, String>();
            responseObj.put("error", e.getMessage());
            builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
        }

        return builder.build();
    }


    @POST
    @Path("/confirmar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response confirmar() {

        Response.ResponseBuilder builder = null;

        try {
            // Validates venta using bean validation
            VentaRegistration bean = (VentaRegistration) request.getSession().getAttribute("venta");
            System.out.println("Sesion numero:"+request.getSession().getId());
            if(bean != null){
                bean.completarVenta();
                request.getSession().setAttribute(
                        "venta",
                        null);
                builder = Response.ok();
            }else{
                Map<String, String> responseObj = new HashMap<String, String>();
                responseObj.put("error", "No existe la venta");
                builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
            }


            // Create an "ok" response

        } catch (ConstraintViolationException ce) {
            // Handle bean validation issues
            builder = createViolationResponse(ce.getConstraintViolations());
        } catch (Exception e) {
            // Handle generic exceptions
            Map<String, String> responseObj = new HashMap<String, String>();
            responseObj.put("error", e.getMessage());
            builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
        }

        return builder.build();
    }

    @POST
    @Path("/remover")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response removerItem(Producto p) {

        Response.ResponseBuilder builder = null;

        try {
            // Validates venta usfaing bean validation
            VentaRegistration bean = (VentaRegistration) request.getSession().getAttribute("venta");
            System.out.println("Sesion numero:"+request.getSession().getId());
            if(bean != null){
                bean.removeItem(p);
                builder = Response.ok();
            }else{
                Map<String, String> responseObj = new HashMap<String, String>();
                responseObj.put("error", "No existe la venta");
                builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
            }
            // Create an "ok" response
        } catch (ConstraintViolationException ce) {
            // Handle bean validation issues
            builder = createViolationResponse(ce.getConstraintViolations());
        } catch (Exception e) {
            // Handle generic exceptions
            Map<String, String> responseObj = new HashMap<String, String>();
            responseObj.put("error", e.getMessage());
            builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
        }

        return builder.build();
    }


    @POST
    @Path("/cancelar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response cancelar() {

        Response.ResponseBuilder builder = null;

        try {
            // Validates venta using bean validation
            VentaRegistration bean = (VentaRegistration) request.getSession().getAttribute("venta");
            System.out.println("Sesion numero:"+request.getSession().getId());
            if(bean != null){
                bean.cancelarVenta();
                request.getSession().setAttribute("venta",null);
                System.out.println("Destruyo el bean: "+request.getSession().getId());
                builder = Response.ok();
            }else{
                Map<String, String> responseObj = new HashMap<String, String>();
                responseObj.put("error", "No existe la venta");
                builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
            }
            // Create an "ok" response
        } catch (ConstraintViolationException ce) {
            // Handle bean validation issues
            builder = createViolationResponse(ce.getConstraintViolations());
        } catch (Exception e) {
            // Handle generic exceptions
            Map<String, String> responseObj = new HashMap<String, String>();
            responseObj.put("error", e.getMessage());
            builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
        }

        return builder.build();
    }


    /**
     * Nuevo metodo para descargar archivos enviados desde el cliente
     */


    private final String UPLOADED_FILE_PATH = System.getProperty("user.home")+"/";

    @POST
    @Path("/masivas")
    @Consumes("multipart/form-data")
    public Response uploadFile(MultipartFormDataInput input) {

        Response.ResponseBuilder builder = null;
        String fileName = "";
        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<InputPart> inputParts = uploadForm.get("uploadedFile");

        for (InputPart inputPart : inputParts) {

            try {

                MultivaluedMap<String, String> header = inputPart.getHeaders();
                fileName = getFileName(header);

                //convert the uploaded file to inputstream
                InputStream inputStream = inputPart.getBody(InputStream.class,null);

                byte [] bytes = org.apache.commons.io.IOUtils.toByteArray(inputStream);

                //constructs upload file path
                fileName = UPLOADED_FILE_PATH + fileName;

                writeFile(bytes,fileName);
                String m =registrationMasivo.registerVentasMasivas(fileName);
                if (!m.equals(""))
                {
                    builder = Response.status(Response.Status.BAD_REQUEST).entity(m);
                }else{

                // Create an "ok" response
                    builder = Response.ok();
                    builder.entity("El nombre del archivo descargado es:" + fileName).build();
                }
            } catch (ConstraintViolationException ce) {
                // Handle bean validation issues
                builder = createViolationResponse(ce.getConstraintViolations());
            } catch (Exception e) {
                e.printStackTrace();
                Map<String, String> responseObj = new HashMap<String, String>();
                responseObj.put("error", e.getMessage());
                builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);

            } finally {
                deleteFile(fileName);
            }

        }

        return builder.build();
    }

    /**
     * header sample
     * {
     * 	Content-Type=[image/png],
     * 	Content-Disposition=[form-data; name="file"; filename="filename.extension"]
     * }
     **/
    private String getFileName(MultivaluedMap<String, String> header) {

        String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {

                String[] name = filename.split("=");

                String finalFileName = name[1].trim().replaceAll("\"", "");
                return finalFileName;
            }
        }
        return "desconocido";
    }

    private void writeFile(byte[] content, String filename) throws IOException {

        File file = new File(filename);

        if (!file.exists()) {
            file.createNewFile();
        }

        FileOutputStream fop = new FileOutputStream(file);

        fop.write(content);
        fop.flush();
        fop.close();

    }

    private void deleteFile(String path){
        try{
            File file = new File(path);
            if(file.delete()){
                log.info(file.getName() + " fue eliminado!");
            }else{
                log.info("No se pudo eliminar el archivo intermedio");
            }
        }catch(Exception e){
            e.printStackTrace();
            log.info("No se pudo eliminar el archivo intermedio");
        }

    }

    /**
     * Evitar el cacheo de respuesta
     */
    protected Response.ResponseBuilder getNoCacheResponseBuilder( Response.Status status ) {
        CacheControl cc = new CacheControl();
        cc.setNoCache( true );
        cc.setMaxAge( -1 );
        cc.setMustRevalidate( true );

        return Response.status( status ).cacheControl( cc );
    }

    @GET
    @Path( "/list" )
    @Produces( "application/json" )
    public Response streamGenerateVentas() {

        return getNoCacheResponseBuilder( Response.Status.OK ).entity( new StreamingOutput() {

            // Instruct how StreamingOutput's write method is to stream the data
            @Override
            public void write( OutputStream os ) throws IOException, WebApplicationException {
                int recordsPerRoundTrip = 100;                      // Number of records for every round trip to the database
                int recordPosition = 0;                             // Initial record position index
                int recordSize = registrationMasivo.queryVentaRecordsSize();   // Total records found for the query

                // Empezar el streaming de datos
                try ( PrintWriter writer = new PrintWriter( new BufferedWriter( new OutputStreamWriter( os ) ) ) ) {

                    writer.print( "{\"result\": [" );

                    while ( recordSize > 0 ) {
                        // Conseguir los datos paginados de la BD
                        List<Venta> ventas = registrationMasivo.listAllVentaEntities( recordPosition, recordsPerRoundTrip );
                        Gson gs = new Gson();
                        for ( Venta venta : ventas ) {
                            if ( recordPosition > 0 ) {
                                writer.print( "," );
                            }

                            // Stream de los datos en json

                            writer.print(gs.toJson(venta));

                            // Aumentar la posicion de la pagina
                            recordPosition++;
                        }

                        // Actualizar el numero de datos restantes
                        recordSize -= recordsPerRoundTrip;
                    }

                    // Se termina el json
                    writer.print( "]}" );
                }
            }
        } ).build();
    }
    
    private void validateVenta(Venta venta) throws ConstraintViolationException, ValidationException {
        // Create a bean validator and check for issues.
        Set<ConstraintViolation<Venta>> violations = validator.validate(venta);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
        }
    }
    
    private Response.ResponseBuilder createViolationResponse(Set<ConstraintViolation<?>> violations) {
        log.fine("Validation completed. violations found: " + violations.size());

        Map<String, String> responseObj = new HashMap<String, String>();

        for (ConstraintViolation<?> violation : violations) {
            responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
        }

        return Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    }
}
