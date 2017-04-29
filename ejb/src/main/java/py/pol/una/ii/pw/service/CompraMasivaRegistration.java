package py.pol.una.ii.pw.service;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.apache.ibatis.session.SqlSession;
import py.pol.una.ii.pw.mappers.CompraMapper;
import py.pol.una.ii.pw.mappers.CompraMasivaMapper;
import py.pol.una.ii.pw.mappers.ProductoCompradoMapper;
import py.pol.una.ii.pw.model.Compra;
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
public class CompraMasivaRegistration {

    @Inject
    private Logger log;

    @Inject
    private EntityManager em;

    @Inject
    private Event<Compra> CompraMasivaEventSrc;

    @Resource
    private EJBContext context;

    private UserTransaction tx;

    private SqlSession sqlSession;

    private CompraMapper Mapper;

    private ProductoCompradoMapper mapperProducto;

    public void register(Compra compra) throws Exception {
        //Se ingresa la fecha en un formato lindo
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        Date today = Calendar.getInstance().getTime();
        String reportDate = df.format(today);
        compra.setFecha(reportDate);

        log.info("Registrando compra de:" + compra.getProveedor());
        Mapper = sqlSession.getMapper(CompraMapper.class);
        mapperProducto = sqlSession.getMapper(ProductoCompradoMapper.class);
        Mapper.register(compra);
        int n=compra.getProductos().size();
        for(int i=0;i<n;i++){
            mapperProducto.register(compra.getProductos().get(i));
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("id_compra", compra.getId());
            param.put("id_productocomprado", compra.getProductos().get(i).getId());
            Mapper.addProducto(param);
            compra.getProductos().add(compra.getProductos().get(i));
        }
    }

    public void registerSingle(Compra compra) throws Exception {
        try{
            sqlSession = Factory.getSqlSessionFactory().openSession();
            register(compra);
            sqlSession.commit();
        }catch (Exception e) {
            e.printStackTrace();
            log.info("Se produjo un error inesperado");
            cancelarCompras();
        }

    }

    public String registerComprasMasivas(String path) throws IOException {
        FileInputStream inputStream = null;
        Scanner sc = null;
        try {
            inputStream = new FileInputStream(path);
            sc = new Scanner(inputStream, "UTF-8");
            sqlSession = Factory.getSqlSessionFactory().openSession();
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                Gson gs = new Gson();
                Compra Compra = gs.fromJson(line, Compra.class);
                register(Compra);
            }
            sqlSession.commit();
            log.info("La compra masiva se realizó con exito");
            // note that Scanner suppresses exceptions
            if (sc.ioException() != null) {
                throw sc.ioException();
            }
            return "";
        } catch (JsonParseException e) {
            e.printStackTrace();
            cancelarCompras();
            return "Error en parsear. No se registraron las compras";
        } catch (Exception e) {
            e.printStackTrace();
            cancelarCompras();
            return "Se produjo un error inesperado";
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (sc != null) {
                sc.close();
            }
        }
    }

    public void cancelarCompras(){
        try {
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }

    }

    public int queryCompraRecordsSize() {
        SqlSession sqlSession = Factory.getSqlSessionFactory().openSession();
        try {
            CompraMasivaMapper mapper = sqlSession.getMapper(CompraMasivaMapper.class);
            return mapper.compraRecordSize();
        } finally {
            sqlSession.close();
        }
    }

    public List<Compra> listAllCompraEntities(int recordPosition, int recordsPerRoundTrip ) {
        SqlSession sqlSession = Factory.getSqlSessionFactory().openSession();
        try {
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("limit", recordsPerRoundTrip);
            param.put("offset", recordPosition);
            CompraMasivaMapper Mapper = sqlSession.getMapper(CompraMasivaMapper.class);
            return Mapper.listAll(param);
        } finally {
            sqlSession.close();
        }
    }

}
