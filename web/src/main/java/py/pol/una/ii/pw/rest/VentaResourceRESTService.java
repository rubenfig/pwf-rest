package py.pol.una.ii.pw.rest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

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
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import py.pol.una.ii.pw.data.ClienteRepository;
import py.pol.una.ii.pw.data.ProductoRepository;
import py.pol.una.ii.pw.data.VentaRepository;
import py.pol.una.ii.pw.model.*;
import py.pol.una.ii.pw.service.ClienteRegistration;
import py.pol.una.ii.pw.service.VentaRegistration;
import py.pol.una.ii.pw.service.VentaRegistration;

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
    

    @Context
    private HttpServletRequest request;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Venta> listAllVentas() {
        return repository.findAllOrderedById();
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
