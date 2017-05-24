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
import py.pol.una.ii.pw.service.ProductoRegistration;

import javax.validation.Validator;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * Created by carlitos on 20/05/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProductoResourceRESTServiceTest extends BaseServiceTest{
    private static final String RESOURCE_PATH = "/productos";

    private static final String
            DESCRIPCION_PRODUCTO = "descripcion1",
            NOMBRE_PRODUCTO = "producto1",
            NOMBRE_PROVEEDOR = "proveedor1",
            EMAIL_PROVEEDOR = "pruebaemail@gmail.com",
            TELEFONO_PROVEEDOR = "123456789";

    private static final float PRECIO_PRODUCTO = 50000;

    private static final long ID_PRODUCTO_EXISTENTE = 1000,
        ID_PROVEEDOR_EXISTENTE =  3000,
        ID_PRODUCTO_NO_EXISTENTE =  1233,
        ID_PROVEEDOR_NO_EXISTENTE = 2300;

    private Producto producto;

    private Proveedor proveedor;

    @InjectMocks
    public static py.pol.una.ii.pw.rest.ProductoResourceRESTService productoService = new py.pol.una.ii.pw.rest.ProductoResourceRESTService();

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ProveedorRepository proveedorRepository;

    @Mock
    private ProductoRegistration productoRegistration;

    @Mock
    Validator validator;

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
        proveedor = new Proveedor();
        proveedor.setId(ID_PROVEEDOR_EXISTENTE);
        proveedor.setNombre(NOMBRE_PROVEEDOR);
        proveedor.setEmail(EMAIL_PROVEEDOR);
        proveedor.setTelefono(TELEFONO_PROVEEDOR);

        producto = new Producto();
        producto.setNombre(NOMBRE_PRODUCTO);
        producto.setDescripcion(DESCRIPCION_PRODUCTO);
        producto.setPrecio(PRECIO_PRODUCTO);
        producto.setId(ID_PRODUCTO_EXISTENTE);
        producto.setProveedor(proveedor);

        when(this.productoRepository.findById(ID_PRODUCTO_EXISTENTE)).thenReturn(producto);

        List<Producto> listaProductos = new ArrayList<>();
        listaProductos.add(producto);

        when(this.productoRepository.findAllOrderedByName("","",0.0F)).thenReturn(listaProductos);
    }

    @After
    public void down(){
        response.close();
    }

    @Test
    public void existeProductoRetornaSuccess() throws Exception {

        response = llamarServicioGET(ID_PRODUCTO_EXISTENTE);

        Assert.assertEquals("Deben devolver un 200",
                Response.Status.OK.getStatusCode(),
                response.getStatus());
    }

    @Test
    public void noExisteProductoRetornaNotFound() throws Exception {
        response = llamarServicioGET(ID_PRODUCTO_NO_EXISTENTE);

        Assert.assertEquals("Deve devolver un 404", Response.Status.NOT_FOUND.getStatusCode(),
                response.getStatus());
    }

    @Test
    public void existenProductosRetornaSuccess() throws Exception {

        response = llamarServicioGET();

        Assert.assertEquals("Deben devolver un 200",
                Response.Status.OK.getStatusCode(),
                response.getStatus());
    }

    @Test
    public void crearProductoRetornaSuccess() throws Exception {
        response = llamarServicioPOST(producto);

        Assert.assertEquals("Deben devolver un 200",
                Response.Status.OK.getStatusCode(),
                response.getStatus());
    }

    @Test
    public void modificarProductoRetornaSuccess() throws Exception {
        when(productoRepository.findById(producto.getId())).thenReturn(producto);
        response = server.newRequest(RESOURCE_PATH+"/"+String.valueOf(producto.getId()))
                .request().buildPut(Entity.json(producto)).invoke();

        Assert.assertEquals("Deben devolver un 200",
                Response.Status.OK.getStatusCode(),
                response.getStatus());
    }

    @Test
    public void borrarProductoRetornaSuccess() throws Exception {
        response = server.newRequest(RESOURCE_PATH+"/"+String.valueOf(producto.getId()))
                .request().buildDelete().invoke();

        Assert.assertEquals("Deben devolver un 200",
                Response.Status.OK.getStatusCode(),
                response.getStatus());
    }


    private Response llamarServicioGET(long id) {
        MultivaluedMap<String, String> requestEntity = new MultivaluedHashMap<>();
        requestEntity.putSingle("id", String.valueOf(id));
        return server.newRequest(RESOURCE_PATH+"/"+String.valueOf(id))
                .request()
                .get();
    }

    private Response llamarServicioGET() {
        MultivaluedMap<String, String> requestEntity = new MultivaluedHashMap<>();
        requestEntity.putSingle("nombre","");
        requestEntity.putSingle("descripcion","");
        requestEntity.putSingle("precio","0.0");
        return server.newRequest(RESOURCE_PATH)
                .request()
                .get();
    }

    private Response llamarServicioPOST(Producto producto) {
        return server.newRequest(RESOURCE_PATH).request().buildPost(Entity.json(producto)).invoke();
    }

}
