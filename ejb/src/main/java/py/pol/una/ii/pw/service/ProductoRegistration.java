package py.pol.una.ii.pw.service;

import org.apache.ibatis.session.SqlSession;
import py.pol.una.ii.pw.mappers.ProductoMapper;
import py.pol.una.ii.pw.model.Producto;
import py.pol.una.ii.pw.util.Factory;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import java.util.logging.Logger;

// The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
public class ProductoRegistration {

    @Inject
    private Logger log;

    @Inject
    private EntityManager em;

    @Inject
    private Event<Producto> productoEventSrc;

    public void register(Producto producto) throws Exception {
        log.info("Registrando " + producto.getNombre());
        SqlSession sqlSession = Factory.getSqlSessionFactory().openSession();
        try {
            ProductoMapper Mapper = sqlSession.getMapper(ProductoMapper.class);
            Mapper.register(producto);
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }
    }
    
    public void update(Producto producto) throws Exception {
        log.info("Actualizando Producto, el nuevo nombre es: " + producto.getNombre());
        SqlSession sqlSession = Factory.getSqlSessionFactory().openSession();
        try {
            ProductoMapper Mapper = sqlSession.getMapper(ProductoMapper.class);
            Mapper.update(producto);
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }
    }
    
    public void remove(long id) throws Exception {
        SqlSession sqlSession = Factory.getSqlSessionFactory().openSession();
        try {
            ProductoMapper Mapper = sqlSession.getMapper(ProductoMapper.class);
            Mapper.delete(id);
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }
    }
}
