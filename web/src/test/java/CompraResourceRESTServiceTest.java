import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import py.pol.una.ii.pw.data.CompraRepository;
import py.pol.una.ii.pw.data.ProductoCompradoRepository;
import py.pol.una.ii.pw.data.ProductoRepository;
import py.pol.una.ii.pw.data.ProveedorRepository;
import py.pol.una.ii.pw.model.Compra;
import py.pol.una.ii.pw.model.Producto;
import py.pol.una.ii.pw.model.ProductoComprado;
import py.pol.una.ii.pw.model.Proveedor;
import py.pol.una.ii.pw.rest.CompraResourceRESTService;
import py.pol.una.ii.pw.service.CompraMasivaRegistration;
import py.pol.una.ii.pw.service.CompraRegistration;

import javax.validation.Validator;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * Created by carlitos on 25/05/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class CompraResourceRESTServiceTest extends BaseServiceTest{
    
    private static final String RESOURCE_PATH = "/compras";

    private static final String FECHA_COMPRA = "05/12/2006",
            DESCRIPCION_PRODUCTO = "descripcion1",
            NOMBRE_PRODUCTO = "producto1",
            NOMBRE_PROVEEDOR = "proveedor1",
            EMAIL_PROVEEDOR = "pruebaemail@gmail.com",
            TELEFONO_PROVEEDOR = "123456789";

    private static final long ID_COMPRA_EXISTENTE = 1500L,
            ID_PRODUCTO_EXISTENTE = 1000L,
            ID_PROVEEDOR_EXISTENTE= 5100L,
            ID_PRODUCTO_COMPRADO_EXISTE = 1560L,
            ID_COMPRA_INEXISTENTE = 1566L;

    private static final Integer CANTIDAD_PRODUCTO_COMPRADO = 52;

    private static final Float PRECIO_PRODUCTO = 50000F;
    
    @InjectMocks
    public static CompraResourceRESTService compraResourceRESTService = new CompraResourceRESTService();
    
    @Mock
    private Logger log;

    @Mock
    private Validator validator;

    @Mock
    private CompraRepository compraRepository;

    @Mock
    private CompraRegistration registration;

    @Mock
    private ProductoRepository repoProducto;

    @Mock
    private ProductoCompradoRepository repoProductoComprado;

    @Mock
    private ProveedorRepository repoProveedor;

    @Mock
    private CompraMasivaRegistration registrationMasivo;
    
    private Compra compra = new Compra();
    
    private List<Compra> compras = new ArrayList<>();

    @BeforeClass
    public static void beforeClass() throws Exception {
        server = InMemoryRestServer.create(compraResourceRESTService);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        server.close();
    }
    
    @Before
    public void setUp() {
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

        compra.setProductos(productoCompradoList);
        compra.setId(ID_COMPRA_EXISTENTE);
        compra.setFecha(FECHA_COMPRA);
        compra.setProveedor(proveedor);

        compras.add(compra);

        when(compraRepository.findAllOrderedByName(null)).thenReturn(compras);
        when(compraRepository.findById(ID_COMPRA_EXISTENTE)).thenReturn(compra);
    }

    @After
    public void down() {
        response.close();
    }

    @Test
    public void listaComprasExistentesRetornaSuccess() throws Exception {

        response = server.newRequest(RESOURCE_PATH).request().get();

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void existeCompraRetornaSuccess() throws Exception {
        response = server.newRequest(RESOURCE_PATH + "/" + String.valueOf(ID_COMPRA_EXISTENTE)).request().get();

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void noExisteCompraRetornaNotFound() throws Exception {
        response = server.newRequest(RESOURCE_PATH + "/" + String.valueOf(ID_COMPRA_INEXISTENTE)).request().get();

        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void crearCompraRetornaSuccess() throws Exception {
        when(registrationMasivo.registerComprasMasivas("/home/carlitos/desconocido")).thenReturn("");
        response = server.newRequest(RESOURCE_PATH).request().buildPost(Entity.json(compra)).invoke();

        Assert.assertEquals("Deben devolver un 200",
                Response.Status.OK.getStatusCode(),
                response.getStatus());
    }

    @Test
    public void listarVentasPaginadoRetornaSuccess() throws Exception {
        when(registrationMasivo.queryCompraRecordsSize()).thenReturn(1);
        when(registrationMasivo.listAllCompraEntities(0,100)).thenReturn(compras);
        response = server.newRequest(RESOURCE_PATH+"/list").request().get();

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void listarVentasPaginadoRetornaExcepcion() throws Exception {
        doThrow(new RuntimeException()).when(registrationMasivo).queryCompraRecordsSize();
        response = server.newRequest(RESOURCE_PATH+"/list").request().get();

        Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    public void borrarCompraRetornaSuccess() throws Exception {
        response = server.newRequest(RESOURCE_PATH+"/"+String.valueOf(compra.getId()))
                .request().buildDelete().invoke();

        Assert.assertEquals("Deben devolver un 200",
                Response.Status.OK.getStatusCode(),
                response.getStatus());
    }

    
}
