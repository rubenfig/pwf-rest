package py.pol.una.ii.pw.service;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.apache.ibatis.session.SqlSession;
import py.pol.una.ii.pw.data.ClienteRepository;
import py.pol.una.ii.pw.data.ProductoRepository;
import py.pol.una.ii.pw.mappers.*;
import py.pol.una.ii.pw.model.Cliente;
import py.pol.una.ii.pw.model.Producto;
import py.pol.una.ii.pw.model.ProductoComprado;
import py.pol.una.ii.pw.model.Venta;
import py.pol.una.ii.pw.util.Factory;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

// The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class VentaMasivaRegistration {

    @Inject
    private Logger log;

    @Inject
    private EntityManager em;

    @Inject
    private Event<Venta> VentaMasivaEventSrc;

    @Inject
    private ClienteRepository repoCliente;

    @Inject
    private ClienteRegistration regCliente;

    @Inject
    private ProductoRepository repoProducto;

    @Resource
    private EJBContext context;

    private UserTransaction tx;

    private SqlSession sqlSession;

    private VentaMapper Mapper;

    private ProductoCompradoMapper mapperProducto;

    private ClienteMapper mapperCliente;

    private ProductoMapper mapperProd;

    public void register(Venta venta) throws Exception {
        //Se ingresa la fecha en un formato lindo
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        Date today = Calendar.getInstance().getTime();
        String reportDate = df.format(today);
        venta.setFecha(reportDate);

        log.info("Registrando compra de:" + venta.getCliente());
        Mapper = sqlSession.getMapper(VentaMapper.class);
        mapperProducto = sqlSession.getMapper(ProductoCompradoMapper.class);
        Mapper.register(venta);
        int n=venta.getProductos().size();
        for(int i=0;i<n;i++){
            mapperProducto.register(venta.getProductos().get(i));
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("id_venta", venta.getId());
            param.put("id_productocomprado", venta.getProductos().get(i).getId());
            Mapper.addProducto(param);
            venta.getProductos().add(venta.getProductos().get(i));
        }

        mapperCliente = sqlSession.getMapper(ClienteMapper.class);
        mapperProd = sqlSession.getMapper(ProductoMapper.class);
        Cliente cliente = mapperCliente.findById(venta.getCliente().getId());
        //Agregar cuenta de cliente
        Float cuenta = cliente.getCuenta();
        for (ProductoComprado pc : venta.getProductos()) {
            Producto p = mapperProd.findById(pc.getProducto().getId());
            cuenta = cuenta + (p.getPrecio() * pc.getCantidad());
        }
        cliente.setCuenta(cuenta);
        mapperCliente.update(cliente);

    }

    public void registerSingle(Venta venta) throws Exception {

        try{
            sqlSession = Factory.getSqlSessionFactory().openSession();
            register(venta);
            sqlSession.commit();
            sqlSession.close();
            log.info("La venta masiva se realizó con exito");
        }catch (Exception e){
            e.printStackTrace();
            cancelarVentas();
            log.info("La venta masiva tuvo un error y no persistió");
        }

    }

    public String registerVentasMasivas(String path) throws IOException {
        FileInputStream inputStream = null;
        Scanner sc = null;
        try {
            inputStream = new FileInputStream(path);
            sc = new Scanner(inputStream, "UTF-8");
            sqlSession = Factory.getSqlSessionFactory().openSession();
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                Gson gs = new Gson();
                Venta Venta = gs.fromJson(line, Venta.class);
                register(Venta);
            }
            sqlSession.commit();
            log.info("La venta masiva se realizó con exito");
            // note that Scanner suppresses exceptions
            if (sc.ioException() != null) {
                throw sc.ioException();
            }
            return "";
        } catch (JsonParseException e){
            e.printStackTrace();
            cancelarVentas();
            return "Error en parsear. No se registraron las ventas";
        } catch (Exception e){
            e.printStackTrace();
            cancelarVentas();
            return "Se produjo un error inesperado";
        } finally {
            if (inputStream != null){
                inputStream.close();
            }
            if (sc != null) {
                sc.close();
            }
        }

    }

    public void cancelarVentas(){
        try {
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }

    }

    public int queryVentaRecordsSize() {
        SqlSession sqlSession = Factory.getSqlSessionFactory().openSession();
        try {
            VentaMasivaMapper mapper = sqlSession.getMapper(VentaMasivaMapper.class);
            return mapper.ventaRecordSize();
        } finally {
            sqlSession.close();
        }
    }

    public List<Venta> listAllVentaEntities(int recordPosition, int recordsPerRoundTrip ) {
        SqlSession sqlSession = Factory.getSqlSessionFactory().openSession();
        try {
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("limit", recordsPerRoundTrip);
            param.put("offset", recordPosition);
            VentaMasivaMapper Mapper = sqlSession.getMapper(VentaMasivaMapper.class);
            return Mapper.listAll(param);
        } finally {
            sqlSession.close();
        }
    }

}
