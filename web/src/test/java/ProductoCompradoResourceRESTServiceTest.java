import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import py.pol.una.ii.pw.data.ProductoCompradoRepository;
import py.pol.una.ii.pw.model.Producto;
import py.pol.una.ii.pw.model.ProductoComprado;
import py.pol.una.ii.pw.model.Proveedor;
import py.pol.una.ii.pw.rest.ProductoCompradoResourceRESTService;
import py.pol.una.ii.pw.service.ProductoCompradoRegistration;

import javax.validation.Validator;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.mockito.Mockito.when;

/**
 * Created by carlitos on 25/05/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProductoCompradoResourceRESTServiceTest extends BaseServiceTest{
    private static final String RESOURCE_PATH = "/productosComprados";

    private static final String DESCRIPCION_PRODUCTO = "descripcion1",
            NOMBRE_PRODUCTO = "producto1",
            NOMBRE_PROVEEDOR = "proveedor1",
            EMAIL_PROVEEDOR = "pruebaemail@gmail.com",
            TELEFONO_PROVEEDOR = "123456789";

    private static final long ID_PRODUCTO_EXISTENTE = 1000L,
            ID_PROVEEDOR_EXISTENTE =  3000L,
            ID_PRODUCTO_COMPRADO_NO_EXISTE = 4879L,
            ID_PRODUCTO_COMPRADO_EXISTE = 1560L;

    private static final Integer CANTIDAD_PRODUCTO_COMPRADO = 52;

    private static final Float PRECIO_PRODUCTO = 50000F;

    private ProductoComprado productoComprado = new ProductoComprado();

    private List<ProductoComprado> productoCompradoList = new ArrayList<>();

    @InjectMocks
    public static ProductoCompradoResourceRESTService productoCompradoResourceRESTService = new ProductoCompradoResourceRESTService();

    @Mock
    private Logger log;

    @Mock
    private Validator validator;

    @Mock
    private ProductoCompradoRepository repository;

    @Mock
    private ProductoCompradoRegistration registration;

    @BeforeClass
    public static void beforeClass() throws Exception {
        server = InMemoryRestServer.create(productoCompradoResourceRESTService);
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

        List<Producto> productoList = new ArrayList<>();
        productoList.add(producto);

        productoComprado.setCantidad(CANTIDAD_PRODUCTO_COMPRADO);
        productoComprado.setId(ID_PRODUCTO_COMPRADO_EXISTE);
        productoComprado.setProducto(producto);

        productoCompradoList.add(productoComprado);
        when(repository.findAllOrderedByName()).thenReturn(productoCompradoList);
        when(repository.findById(ID_PRODUCTO_COMPRADO_EXISTE)).thenReturn(productoComprado);

    }

    @After
    public void down() {
        response.close();
    }

    @Test
    public void listarProductosCompradosReturnSuccess() throws Exception {

        response = server.newRequest(RESOURCE_PATH)
                .request().get();

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void existeProductoCompradoRetornaSuccess() throws Exception {
        response = server.newRequest(RESOURCE_PATH+"/"+ID_PRODUCTO_COMPRADO_EXISTE)
                .request()
                .get();

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void noExisteProductoCompradoRetornaNotFound() throws Exception {
        response = server.newRequest(RESOURCE_PATH+"/"+ID_PRODUCTO_COMPRADO_NO_EXISTE)
                .request()
                .get();

        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void crearProductoCompradoRetornaSuccess() throws Exception {
        response = server.newRequest(RESOURCE_PATH).request().buildPost(Entity.json(productoComprado)).invoke();

        Assert.assertEquals("Deben devolver un 200",
                Response.Status.OK.getStatusCode(),
                response.getStatus());
    }

    @Test
    public void modificarProductoCompradoRetornaSuccess() throws Exception {
        response = server.newRequest(RESOURCE_PATH)
                .request().buildPut(Entity.json(productoComprado)).invoke();

        Assert.assertEquals("Deben devolver un 200",
                Response.Status.OK.getStatusCode(),
                response.getStatus());
    }

    @Test
    public void borrarProductoCompradoRetornaSuccess() throws Exception {
        response = server.newRequest(RESOURCE_PATH+"/"+String.valueOf(productoComprado.getId()))
                .request().buildDelete().invoke();

        Assert.assertEquals("Deben devolver un 200",
                Response.Status.OK.getStatusCode(),
                response.getStatus());
    }
}
