package py.pol.una.ii.pw.test;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import py.pol.una.ii.pw.data.ProductoRepository;
import py.pol.una.ii.pw.data.ProveedorRepository;
import py.pol.una.ii.pw.model.Producto;
import py.pol.una.ii.pw.model.Proveedor;
import py.pol.una.ii.pw.rest.ProductoResourceRESTService;
import py.pol.una.ii.pw.service.ProductoRegistration;

import javax.validation.Validator;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by carlitos on 20/05/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProductoServiceTest extends BaseServiceTest{
    private static final String RESOURCE_PATH = "/productos";

    private static final String
            DESCRIPCION_PRODUCTO = "descripcion1",
            NOMBRE_PRODUCTO = "producto1",
            NOMBRE_PROVEEDOR = "proveedor1",
            EMAIL_PROVEEDOR = "pruebaemail@gmail.com",
            TELEFONO_PROVEEDOR = "123456789";

    private static final float PRECIO_PRODUCTO = 50000;

    private static final long ID_PRODUCTO_EXISTENTE = 1000,
        ID_PROVEEDOR_EXISTENTE =  3000;

    @InjectMocks
    public static ProductoResourceRESTService productoService = new ProductoResourceRESTService();

    @Mock
    private Validator validator;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ProveedorRepository proveedorRepository;

    @Mock
    private ProductoRegistration productoRegistration;

    @BeforeClass
    public static void beforeClass() throws Exception {
        server = InMemoryRestServer.create(productoService);
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

        when(this.productoRepository.findById(ID_PRODUCTO_EXISTENTE)).thenReturn(producto);
    }

    @Test
    public void existeProductoRetornaSuccess() throws Exception {

        response = llamarServicio(ID_PRODUCTO_EXISTENTE);
        int responseStatus = response.getStatus();

        Assert.assertEquals("Deben devolver un 200",
                Response.Status.OK.getStatusCode(),
                responseStatus);

        verify(this.productoRepository,times(1)).findById(ID_PRODUCTO_EXISTENTE);
    }

    private Response llamarServicio(long id) {
        MultivaluedMap<String, String> requestEntity = new MultivaluedHashMap<>();
        requestEntity.putSingle("id", String.valueOf(id));
        return server.newRequest(RESOURCE_PATH+"/"+String.valueOf(id))
                .request()
                .get();
    }

}
