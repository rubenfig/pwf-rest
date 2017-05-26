import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import py.pol.una.ii.pw.data.ClienteRepository;
import py.pol.una.ii.pw.model.Cliente;
import py.pol.una.ii.pw.rest.ClienteResourceRESTService;
import py.pol.una.ii.pw.service.ClienteRegistration;

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
public class ClienteResourceRESTServiceTest extends BaseServiceTest {
    private static final String RESOURCE_PATH = "/clientes";

    private static final String NOMBRE_CLIENTE = "name",
            EMAIL_CLIENTE = "cliente@email.com",
            TELEFONO_CLIENTE = "1234567894";

    private static final long ID_CLIENTE_EXISTENTE = 1000L;

    private static final long ID_CLIENTE_INEXISTENTE = 15L;

    private static final Float CUENTA_CLIENTE = 500000F;

    @InjectMocks
    public static ClienteResourceRESTService clienteService = new ClienteResourceRESTService();

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteRegistration clienteRegistration;

    @Mock
    private Validator validator;

    private Cliente cliente = new Cliente();

    private List<Cliente> listaClientees = new ArrayList<>();

    @BeforeClass
    public static void beforeClass() throws Exception {
        server = InMemoryRestServer.create(clienteService);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        server.close();
    }

    @Before
    public void setUp() {
        cliente.setId(ID_CLIENTE_EXISTENTE);
        cliente.setName(NOMBRE_CLIENTE);
        cliente.setEmail(EMAIL_CLIENTE);
        cliente.setPhoneNumber(TELEFONO_CLIENTE);
        cliente.setCuenta(CUENTA_CLIENTE);

        listaClientees.add(cliente);

        when(clienteRepository.findAllOrderedByName("","","")).thenReturn(listaClientees);
        when(clienteRepository.findById(ID_CLIENTE_EXISTENTE)).thenReturn(cliente);
        when(clienteRepository.findById(ID_CLIENTE_INEXISTENTE)).thenReturn(null);
        Set<ConstraintViolation<Cliente>> violations = Collections.emptySet();
        when(validator.validate(cliente)).thenReturn(violations);
    }

    @After
    public void down() {
        response.close();
    }

    @Test
    public void listarClienteesReturnSuccess() throws Exception {
        response = llamarServicioGET("","","");

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void existeClienteRetornaSuccess() throws Exception {
        response = llamarServicioGET(String.valueOf(ID_CLIENTE_EXISTENTE));

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void noExisteClienteRetornaNotFound() throws Exception {
        response = llamarServicioGET(String.valueOf(ID_CLIENTE_INEXISTENTE));

        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void crearClienteRetornaSuccess() throws Exception {
        response = llamarServicioPOST(cliente);

        Assert.assertEquals("Deben devolver un 200",
                Response.Status.OK.getStatusCode(),
                response.getStatus());
    }

    /*@Test
    public void errorAlCrearClienteRetornaBadRequest() throws Exception {
        doThrow(new Exception()).when(clienteRegistration).register(cliente);
        response = llamarServicioPOST(cliente);
        Assert.assertEquals("Deben devolver un 400",
                Response.Status.BAD_REQUEST.getStatusCode(),
                response.getStatus());
    }*/

    @Test
    public void modificarClienteRetornaSuccess() throws Exception {
        response = server.newRequest(RESOURCE_PATH+"/"+String.valueOf(cliente.getId()))
                .request().buildPut(Entity.json(cliente)).invoke();

        Assert.assertEquals("Deben devolver un 200",
                Response.Status.OK.getStatusCode(),
                response.getStatus());
    }

    @Test
    public void borrarClienteRetornaSuccess() throws Exception {
        response = server.newRequest(RESOURCE_PATH+"/"+String.valueOf(cliente.getId()))
                .request().buildDelete().invoke();

        Assert.assertEquals("Deben devolver un 200",
                Response.Status.OK.getStatusCode(),
                response.getStatus());
    }


    private Response llamarServicioGET(String nombre, String email, String telefono) {
        MultivaluedMap<String, String> requestEntity = new MultivaluedHashMap<>();
        requestEntity.putSingle("name", nombre);
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

    private Response llamarServicioPOST(Cliente cliente) {
        return server.newRequest(RESOURCE_PATH).request().buildPost(Entity.json(cliente)).invoke();
    }
}
