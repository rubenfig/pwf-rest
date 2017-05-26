import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import py.pol.una.ii.pw.data.VentaRepository;
import py.pol.una.ii.pw.model.*;
import py.pol.una.ii.pw.rest.VentaResourceRESTService;
import py.pol.una.ii.pw.service.VentaMasivaRegistration;
import py.pol.una.ii.pw.service.VentaRegistration;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Validator;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * Created by carlitos on 24/05/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class VentaResourceRESTServiceTest extends BaseServiceTest {

    private static final String RESOURCE_PATH = "/ventas";

    private static final String NOMBRE_CLIENTE = "name",
            EMAIL_CLIENTE = "cliente@email.com",
            TELEFONO_CLIENTE = "1234567894",
            FECHA_VENTA = "05/12/2006",
            DESCRIPCION_PRODUCTO = "descripcion1",
            NOMBRE_PRODUCTO = "producto1",
            NOMBRE_PROVEEDOR = "proveedor1",
            EMAIL_PROVEEDOR = "pruebaemail@gmail.com",
            TELEFONO_PROVEEDOR = "123456789",
            TEXTO_CORRECTO = "Un texto correcto";

    private static final long ID_CLIENTE_EXISTENTE = 1000L,
            ID_VENTA_EXISTENTE = 1500L,
            ID_PRODUCTO_EXISTENTE = 1000L,
            ID_PROVEEDOR_EXISTENTE =  3000L,
            ID_PRODUCTO_COMPRADO_EXISTE = 1560L,
            ID_VENTA_INEXISTENTE = 1566L;

    private static final Integer CANTIDAD_PRODUCTO_COMPRADO = 52;

    private static final Float CUENTA_CLIENTE = 500000F, PRECIO_PRODUCTO = 50000F;


    @InjectMocks
    public static VentaResourceRESTService ventaResourceRESTService = new VentaResourceRESTService();

    @Mock
    private Logger log;

    @Mock
    private Validator validator;

    @Mock
    private VentaRepository repository;

    @Mock
    private VentaRegistration registration;

    @Mock
    private VentaMasivaRegistration registrationMasivo;

    HttpServletRequest  mockedRequest = Mockito.mock(HttpServletRequest.class);

    private Venta venta = new Venta();

    private List<Venta> ventas = new ArrayList<>();

    @BeforeClass
    public static void beforeClass() throws Exception {
        server = InMemoryRestServer.create(ventaResourceRESTService);
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

        Proveedor proveedor = new Proveedor();
        proveedor.setId(ID_PROVEEDOR_EXISTENTE);
        proveedor.setNombre(NOMBRE_PROVEEDOR);
        proveedor.setEmail(EMAIL_PROVEEDOR);
        proveedor.setTelefono(TELEFONO_PROVEEDOR);

        Producto producto = new Producto();
        producto.setNombre(NOMBRE_PRODUCTO);
        producto.setDescripcion(DESCRIPCION_PRODUCTO);
        producto.setPrecio(PRECIO_PRODUCTO);
        producto.setId(ID_PRODUCTO_EXISTENTE);
        producto.setProveedor(proveedor);

        ProductoComprado productoComprado = new ProductoComprado();
        productoComprado.setCantidad(CANTIDAD_PRODUCTO_COMPRADO);
        productoComprado.setId(ID_PRODUCTO_COMPRADO_EXISTE);
        productoComprado.setProducto(producto);

        List<ProductoComprado> productoCompradoList = new ArrayList<>();
        productoCompradoList.add(productoComprado);

        venta.setId(ID_VENTA_EXISTENTE);
        venta.setFecha(FECHA_VENTA);
        venta.setProductos(productoCompradoList);
        venta.setCliente(cliente);

        ventas.add(venta);

        when(repository.findAllOrderedById(null)).thenReturn(ventas);
        when(repository.findById(ID_VENTA_EXISTENTE)).thenReturn(venta);

    }

    @After
    public void down() {
        response.close();
    }

    @Test
    public void listaVentasExistentesRetornaSuccess() throws Exception {
        response = server.newRequest(RESOURCE_PATH).request().get();

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void existeVentaRetornaSuccess() throws Exception {
        response = server.newRequest(RESOURCE_PATH + "/" + String.valueOf(ID_VENTA_EXISTENTE)).request().get();

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void noExisteVentaRetornaNotFound() throws Exception {
        response = server.newRequest(RESOURCE_PATH + "/" + String.valueOf(ID_VENTA_INEXISTENTE)).request().get();

        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void crearVentaRetornaSuccess() throws Exception {
        when(registrationMasivo.registerVentasMasivas("/home/carlitos/desconocido")).thenReturn("");
        response = server.newRequest(RESOURCE_PATH).request().buildPost(Entity.json(venta)).invoke();

        Assert.assertEquals("Deben devolver un 200",
                Response.Status.OK.getStatusCode(),
                response.getStatus());
    }

    /*@Test
    public void crearVentaCarritoRetornaSuccess() throws Exception {
        HttpServletRequest stubHttpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse stubHttpServletResponse = mock(HttpServletResponse.class);
        HttpSession stubHttpSession = mock(HttpSession.class);

        when(stubHttpServletRequest.getSession()).thenReturn(stubHttpSession);
        when(stubHttpSession.getAttribute("venta")).thenReturn("algo");
        response = server.newRequest(RESOURCE_PATH + "/iniciar").request().buildPost(Entity.json(venta)).invoke();
    }
    @Test
    public void ventasMasivasRetornaSucess() throws Exception {
        MultipartFormDataOutput multipartFormDataOutput = new MultipartFormDataOutput();
        multipartFormDataOutput.addFormData("uploadedFile", " aksldfjalsd ", MediaType.TEXT_PLAIN_TYPE);

        response = server.newRequest(RESOURCE_PATH + "/masivas")
                .request()
                .post(Entity.entity(multipartFormDataOutput, MediaType.MULTIPART_FORM_DATA));
    }*/

    @Test
    public void listarVentasPaginadoRetornaSuccess() throws Exception {
        when(registrationMasivo.queryVentaRecordsSize()).thenReturn(1);
        when(registrationMasivo.listAllVentaEntities(0,100)).thenReturn(ventas);
        response = server.newRequest(RESOURCE_PATH+"/list").request().get();

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void listarVentasPaginadoRetornaExcepcion() throws Exception {
        doThrow(new RuntimeException()).when(registrationMasivo).queryVentaRecordsSize();
        response = server.newRequest(RESOURCE_PATH+"/list").request().get();

        Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }


}
