package py.pol.una.ii.pw.service;

import org.apache.ibatis.session.SqlSession;
import py.pol.una.ii.pw.data.ProveedorRepository;
import py.pol.una.ii.pw.mappers.CompraMapper;
import py.pol.una.ii.pw.mappers.ProductoCompradoMapper;
import py.pol.una.ii.pw.model.Compra;
import py.pol.una.ii.pw.model.Producto;
import py.pol.una.ii.pw.model.ProductoComprado;
import py.pol.una.ii.pw.util.Factory;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

// The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateful
@StatefulTimeout(value = 15, unit = TimeUnit.MINUTES)
@TransactionManagement(TransactionManagementType.BEAN)
public class CompraRegistration {

    @Inject
    private Logger log;

    @Inject
    private EntityManager em;

    @Inject
    private Event<Compra> compraEventSrc;

    @Inject
    private ProveedorRepository repoProveedor;

    @Inject
    ProductoCompradoRegistration registration;



    private SqlSession sqlSession;

    private Compra compra_actual;

    private CompraMapper Mapper;

    private ProductoCompradoMapper mapperProducto;

    @PostConstruct
    public void initializateBean(){
        compra_actual = new Compra();
    }

    public void register(Compra compra) throws Exception {
        //Se ingresa la fecha en un formato lindo
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        Date today = Calendar.getInstance().getTime();
        String reportDate = df.format(today);
        compra.setFecha(reportDate);

    	log.info("Registrando compra de:" + compra.getProveedor());
    	compra_actual=compra;
        sqlSession = Factory.getSqlSessionFactory().openSession();
        Mapper = sqlSession.getMapper(CompraMapper.class);
        mapperProducto = sqlSession.getMapper(ProductoCompradoMapper.class);
        Mapper.register(compra_actual);
        int n=compra_actual.getProductos().size();
        for(int i=0;i<n;i++)
            this.agregarCarrito(compra_actual.getProductos().get(i));
    }

    public void agregarCarrito (ProductoComprado pc) throws Exception{
        mapperProducto.register(pc);
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("id_compra", compra_actual.getId());
        param.put("id_productocomprado", pc.getId());
        Mapper.addProducto(param);
        compra_actual.getProductos().add(pc);
    }

    public void removeItem(Producto p) throws Exception{
        boolean bandera = false;
        Map<String, Object> param = new HashMap<String, Object>();
        int n=compra_actual.getProductos().size();
        for(int i=0;i<n;i++){
            ProductoComprado pc = compra_actual.getProductos().get(i);
            if(p.getId().equals(pc.getProducto().getId())){
                compra_actual.getProductos().remove(i);
                param.put("id_compra", compra_actual.getId());
                param.put("id_productocomprado", pc.getId());
                Mapper.deleteProducto(param);
                bandera = true;
                n--;
            }
        }
        if(!bandera)
            log.info("No existe el item que se desea eliminar");
    }

    @Remove
    public void completarCompra() throws Exception {
        try {
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }

    }

    @Remove
    public void cancelarCompra() throws Exception {
        try {
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
    }
    
    public void update(Compra compra) throws Exception {
    	log.info("Actualizando Compra, el nuevo nombre es: " + compra.getId());
        SqlSession sqlSession = Factory.getSqlSessionFactory().openSession();
        try {
            CompraMapper mapper = sqlSession.getMapper(CompraMapper.class);
            mapper.update(compra);
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }
    }
    
    public void remove(Compra compra) throws Exception {
        SqlSession sqlSession = Factory.getSqlSessionFactory().openSession();
        try {
            CompraMapper mapper = sqlSession.getMapper(CompraMapper.class);
            mapper.delete(compra.getId());
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }
    }
}
