package py.pol.una.ii.pw.test;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import py.pol.una.ii.pw.data.ProveedorRepository;
import py.pol.una.ii.pw.model.Proveedor;
import py.pol.una.ii.pw.rest.ProveedorResourceRESTService;
import py.pol.una.ii.pw.service.ProveedorRegistration;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;

/**
 * Created by carlitos on 24/05/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProveedorResourceRESTServiceTest extends BaseServiceTest {
    private static final String RESOURCE_PATH = "/proveedores";

    private static final String NOMBRE_PROVEEDOR = "nombre",
        EMAIL_PROVEEDOR = "proveedor@email.com",
        TELEFONO_PROVEEDOR = "1234567894";

    private static final long ID_PROVEEDOR_EXISTENTE = 1000L;

    private static final long ID_PROVEEDOR_INEXISTENTE = 15L;

    @InjectMocks
    public static ProveedorResourceRESTService proveedorService = new ProveedorResourceRESTService();

    @Mock
    private ProveedorRepository proveedorRepository;

    @Mock
    private ProveedorRegistration proveedorRegistration;

    @Mock
    private Validator validator;

    private Proveedor proveedor = new Proveedor();

    private List<Proveedor> listaProveedores = new ArrayList<>();

    @BeforeClass
    public static void beforeClass() throws Exception {
        server = InMemoryRestServer.create(proveedorService);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        server.close();
    }

    @Before
    public void setUp() {
        proveedor.setId(ID_PROVEEDOR_EXISTENTE);
        proveedor.setNombre(NOMBRE_PROVEEDOR);
        proveedor.setEmail(EMAIL_PROVEEDOR);
        proveedor.setTelefono(TELEFONO_PROVEEDOR);

        listaProveedores.add(proveedor);

        when(proveedorRepository.findAllOrderedByName("","","")).thenReturn(listaProveedores);
        when(proveedorRepository.findById(ID_PROVEEDOR_EXISTENTE)).thenReturn(proveedor);
        when(proveedorRepository.findById(ID_PROVEEDOR_INEXISTENTE)).thenReturn(null);
        Set<ConstraintViolation<Proveedor>> violations = Collections.emptySet();
        when(validator.validate(proveedor)).thenReturn(violations);
    }

    @After
    public void down() {
        response.close();
    }

    @Test
    public void listarProveedoresReturnSuccess() throws Exception {
        response = llamarServicioGET("","","");

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void existeProveedorRetornaSuccess() throws Exception {
        response = llamarServicioGET(String.valueOf(ID_PROVEEDOR_EXISTENTE));

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void noExisteProveedorRetornaNotFound() throws Exception {
        response = llamarServicioGET(String.valueOf(ID_PROVEEDOR_INEXISTENTE));

        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void crearProveedorRetornaSuccess() throws Exception {
        response = llamarServicioPOST(proveedor);

        Assert.assertEquals("Deben devolver un 200",
                Response.Status.OK.getStatusCode(),
                response.getStatus());
    }

    /*@Test
    public void errorAlCrearProveedorRetornaBadRequest() throws Exception {
        doThrow(new Exception()).when(proveedorRegistration).register(proveedor);
        response = llamarServicioPOST(proveedor);
        Assert.assertEquals("Deben devolver un 400",
                Response.Status.BAD_REQUEST.getStatusCode(),
                response.getStatus());
    }*/

    @Test
    public void modificarProveedorRetornaSuccess() throws Exception {
        response = server.newRequest(RESOURCE_PATH+"/"+String.valueOf(proveedor.getId()))
                .request().buildPut(Entity.json(proveedor)).invoke();

        Assert.assertEquals("Deben devolver un 200",
                Response.Status.OK.getStatusCode(),
                response.getStatus());
    }

    @Test
    public void borrarProveedorRetornaSuccess() throws Exception {
        response = server.newRequest(RESOURCE_PATH+"/"+String.valueOf(proveedor.getId()))
                .request().buildDelete().invoke();

        Assert.assertEquals("Deben devolver un 200",
                Response.Status.OK.getStatusCode(),
                response.getStatus());
    }


    private Response llamarServicioGET(String nombre, String email, String telefono) {
        MultivaluedMap<String, String> requestEntity = new MultivaluedHashMap<>();
        requestEntity.putSingle("nombre", nombre);
        requestEntity.putSingle("email", email);
        requestEntity.putSingle("telefono", telefono);
        return server.newRequest(RESOURCE_PATH)
                .request()
                .get();
    }

    private Response llamarServicioGET(String id){
        MultivaluedMap<String, String> requestEntity = new MultivaluedHashMap<>();
        requestEntity.putSingle("id", id);
        return server.newRequest(RESOURCE_PATH+"/"+id)
                .request()
                .get();
    }

    private Response llamarServicioPOST(Proveedor proveedor) {
        return server.newRequest(RESOURCE_PATH).request().buildPost(Entity.json(proveedor)).invoke();
    }

}
