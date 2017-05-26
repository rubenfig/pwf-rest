import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import py.pol.una.ii.pw.data.ClienteRepository;
import py.pol.una.ii.pw.data.PagoRepository;
import py.pol.una.ii.pw.model.Cliente;
import py.pol.una.ii.pw.model.Pago;
import py.pol.una.ii.pw.rest.PagoResourceRESTService;
import py.pol.una.ii.pw.service.ClienteRegistration;
import py.pol.una.ii.pw.service.PagoRegistration;

import javax.validation.Validator;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import static org.mockito.Mockito.when;

/**
 * Created by carlitos on 25/05/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class PagoResourceRESTServiceTest extends BaseServiceTest {
    private static final String RESOURCE_PATH = "/pagos";

    private static final String NOMBRE_CLIENTE = "name",
            EMAIL_CLIENTE = "cliente@email.com",
            TELEFONO_CLIENTE = "1234567894";
    
    private static final Date FECHA_PAGO = Calendar.getInstance().getTime();

    private static final long ID_CLIENTE_EXISTENTE = 1000L,
            ID_PAGO = 15000L,
            ID_PAGO_INEXISTENTE = 15666L;

    private static final Float CUENTA_CLIENTE = 500000F, MONTO_PAGO = 156000F;

    @InjectMocks
    public static PagoResourceRESTService pagoService = new PagoResourceRESTService();

    @Mock
    private Logger log;

    @Mock
    private Validator validator;

    @Mock
    private PagoRepository repository;

    @Mock
    private PagoRegistration registration;

    @Mock
    private ClienteRepository repoCliente;

    @Mock
    private ClienteRegistration regCliente;
    
    private Pago pago = new Pago();

    private List<Pago> listaPagos = new ArrayList<>();

    @BeforeClass
    public static void beforeClass() throws Exception {
        server = InMemoryRestServer.create(pagoService);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        server.close();
    }

    @Before
    public void setUp() {
        Cliente cliente = new Cliente();
        cliente.setId(ID_CLIENTE_EXISTENTE);
        cliente.setName(NOMBRE_CLIENTE);
        cliente.setEmail(EMAIL_CLIENTE);
        cliente.setPhoneNumber(TELEFONO_CLIENTE);
        cliente.setCuenta(CUENTA_CLIENTE);
        
        pago.setCliente(cliente);
        pago.setId(ID_PAGO);
        pago.setFecha(FECHA_PAGO);
        pago.setMonto(MONTO_PAGO);
        
        listaPagos.add(pago);
        when(repository.findById(ID_PAGO)).thenReturn(pago);
        when(repository.findAllOrderedById()).thenReturn(listaPagos);
        when(repoCliente.findById(cliente.getId())).thenReturn(cliente);
        
    }

    @After
    public void down() {
        response.close();
    }

    @Test
    public void listarPagosReturnSuccess() throws Exception {
        response = server.newRequest(RESOURCE_PATH)
                .request()
                .get();

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void existePagoRetornaSuccess() throws Exception {
        response = server.newRequest(RESOURCE_PATH+"/"+ID_PAGO)
                .request()
                .get();

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void noExistePagoRetornaNotFound() throws Exception {
        response = server.newRequest(RESOURCE_PATH+"/"+ID_PAGO_INEXISTENTE)
                .request()
                .get();

        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void crearPagoRetornaSuccess() throws Exception {
        response = server.newRequest(RESOURCE_PATH).request().buildPost(Entity.json(pago)).invoke();

        Assert.assertEquals("Deben devolver un 200",
                Response.Status.OK.getStatusCode(),
                response.getStatus());
    }
    
}
