/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package py.pol.una.ii.pw.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import py.pol.una.ii.pw.data.CompraRepository;
import py.pol.una.ii.pw.data.ProductoCompradoRepository;
import py.pol.una.ii.pw.data.ProductoRepository;
import py.pol.una.ii.pw.data.ProveedorRepository;
import py.pol.una.ii.pw.model.Compra;
import py.pol.una.ii.pw.model.Producto;
import py.pol.una.ii.pw.model.ProductoComprado;
import py.pol.una.ii.pw.service.CompraRegistration;

/**
 * JAX-RS Example
 * <p/>
 * This class produces a RESTful service to read/write the contents of the compras table.
 */
@Path("/compras")
@RequestScoped
public class CompraResourceRESTService {
    @Inject
    private Logger log;

    @Inject
    private Validator validator;

    @Inject
    private CompraRepository repository;

    @Inject
    CompraRegistration registration;
    
    @Inject
    private ProductoRepository repoProducto;
    
    @Inject
    private ProductoCompradoRepository repoProductoComprado;
    
    @Inject
    private ProveedorRepository repoProveedor;

    @Context
    private HttpServletRequest request;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Compra> listAllCompras() {
        return repository.findAllOrderedByName();
    }

    @GET
    @Path("/{id:[0-9][0-9]*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Compra lookupCompraById(@PathParam("id") long id) {
        Compra compra = repository.findById(id);
        if (compra == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return compra;
    }

    /**
     * Creates a new compra from the values provided. Performs validation, and will return a JAX-RS response with either 200 ok,
     * or with a map of fields, and related errors.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCompra(Compra compra) {

        Response.ResponseBuilder builder = null;

        try {
            // Validates compra using bean validation
            validateCompra(compra);
            CompraRegistration bean = (CompraRegistration) request.getSession().getAttribute("compra");
            System.out.println("Sesion numero:"+request.getSession().getId());
            if(bean == null){
                // EJB is not present in the HTTP session
                // so let's fetch a new one from the container
                try {
                    InitialContext ic = new InitialContext();
                    bean = (CompraRegistration)
                            ic.lookup("java:global/EjbJaxRS-ear/EjbJaxRS-ejb/CompraRegistration");

                    // put EJB in HTTP session for future servlet calls
                    request.getSession().setAttribute(
                            "compra",
                            bean);
                    bean.register(compra);
                    System.out.println("Creo el bean: "+request.getSession().getId());
                } catch (NamingException e) {
                    System.out.println("No creo el bean");
                    throw new ServletException(e);

                }
            }else{
                Map<String, String> response = new HashMap<String, String>();
                response.put("error", "ya existe la compra");
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
     * Nuevo metodo para descargar archivos enviados desde el cliente
     */

    private final String UPLOADED_FILE_PATH = "/home/carlitos/Documentos/";

    @POST
    @Path("/masivas")
    @Consumes("multipart/form-data")
    public Response uploadFile(MultipartFormDataInput input) {

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

                log.info("La compra masiva se realizó con exito");

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return Response.status(200)
                .entity("El nombre del archivo descargado es:" + fileName).build();

    }

    /**
     * header sample
     * {
     * 	Content-Type=[image/png],
     * 	Content-Disposition=[form-data; name="file"; filename="filename.extension"]
     * }
     **/
    //get uploaded filename, is there a easy way in RESTEasy?
    private String getFileName(MultivaluedMap<String, String> header) {

        String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {

                String[] name = filename.split("=");

                String finalFileName = name[1].trim().replaceAll("\"", "");
                return finalFileName;
            }
        }
        return "unknown";
    }

    //save to somewhere
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


    /**
     * Creates a new compra from the values provided. Performs validation, and will return a JAX-RS response with either 200 ok,
     * or with a map of fields, and related errors.
     */
    @POST
    @Path("/agregar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response agregarCarrito(ProductoComprado pc) {

        Response.ResponseBuilder builder = null;

        try {
            // Validates compra using bean validation
            CompraRegistration bean = (CompraRegistration) request.getSession().getAttribute("compra");
            System.out.println("Sesion numero:"+request.getSession().getId());
            if(bean != null){
                bean.agregarCarrito(pc);
                builder = Response.ok();
            }else{

                Map<String, String> responseObj = new HashMap<String, String>();
                responseObj.put("error", "No existe la compra");
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
            // Validates compra usfaing bean validation
            CompraRegistration bean = (CompraRegistration) request.getSession().getAttribute("compra");
            System.out.println("Sesion numero:"+request.getSession().getId());
            if(bean != null){
                bean.removeItem(p);
                builder = Response.ok();
            }else{
                Map<String, String> responseObj = new HashMap<String, String>();
                responseObj.put("error", "No existe la compra");
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
            // Validates compra using bean validation
            CompraRegistration bean = (CompraRegistration) request.getSession().getAttribute("compra");
            System.out.println("Sesion numero:"+request.getSession().getId());
            if(bean != null){
                bean.completarCompra();
                request.getSession().setAttribute(
                        "compra",
                        null);
                builder = Response.ok();
            }else{
                Map<String, String> responseObj = new HashMap<String, String>();
                responseObj.put("error", "No existe la compra");
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
            // Validates compra using bean validation
            CompraRegistration bean = (CompraRegistration) request.getSession().getAttribute("compra");
            System.out.println("Sesion numero:"+request.getSession().getId());
            if(bean != null){
                bean.cancelarCompra();
                request.getSession().setAttribute("compra",null);
                System.out.println("Destruyo el bean: "+request.getSession().getId());
                builder = Response.ok();
            }else{
                Map<String, String> responseObj = new HashMap<String, String>();
                responseObj.put("error", "No existe la compra");
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
     * <p>
     * Validates the given Compra variable and throws validation exceptions based on the type of error. If the error is standard
     * bean validation errors then it will throw a ConstraintValidationException with the set of the constraints violated.
     * </p>
     * <p>
     * If the error is caused because an existing compra with the same email is registered it throws a regular validation
     * exception so that it can be interpreted separately.
     * </p>
     * 
     * @param compra Compra to be validated
     * @throws ConstraintViolationException If Bean Validation errors exist
     * @throws ValidationException If compra with the same email already exists
     */
    private void validateCompra(Compra compra) throws ConstraintViolationException, ValidationException {
        // Create a bean validator and check for issues.
        Set<ConstraintViolation<Compra>> violations = validator.validate(compra);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
        }

        // Check the uniqueness of the email address
        
    }

    /**
     * Creates a JAX-RS "Bad Request" response including a map of all violation fields, and their message. This can then be used
     * by clients to show violations.
     * 
     * @param violations A set of violations that needs to be reported
     * @return JAX-RS response containing all violations
     */
    private Response.ResponseBuilder createViolationResponse(Set<ConstraintViolation<?>> violations) {
        log.fine("Validation completed. violations found: " + violations.size());

        Map<String, String> responseObj = new HashMap<String, String>();

        for (ConstraintViolation<?> violation : violations) {
            responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
        }

        return Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    }

    
    
    /**
     * Update compra from the values provided. Performs validation, and will return a JAX-RS response with either 200 ok,
     * or with a map of fields, and related errors.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response modificarCompra(Compra compra) {

        Response.ResponseBuilder builder = null;

        try {
            // Validates compra using bean validation
            validateCompra(compra);
            
            registration.update(compra);

            // Create an "ok" response
            builder = Response.ok();
        } catch (ConstraintViolationException ce) {
            // Handle bean validation issues
            builder = createViolationResponse(ce.getConstraintViolations());
        } catch (ValidationException e) {
            // Handle the unique constrain violation
            Map<String, String> responseObj = new HashMap<String, String>();
            responseObj.put("email", "Email taken");
            builder = Response.status(Response.Status.CONFLICT).entity(responseObj);
        } catch (Exception e) {
            // Handle generic exceptions
            Map<String, String> responseObj = new HashMap<String, String>();
            responseObj.put("error", e.getMessage());
            builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
        }

        return builder.build();
    }
    
    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Compra deleteCompraById(@PathParam("id") long id) {
        Compra compra = null;
    	try {
        	compra = repository.findById(id);
        	registration.remove(compra);
            if (compra == null) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
        } catch (Exception e){
        	log.info(e.toString());
        	compra = null;
        }
        return compra;
    }
}
