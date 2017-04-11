package py.pol.una.ii.pw.service;

import org.apache.ibatis.session.SqlSession;
import py.pol.una.ii.pw.mappers.ProveedorMapper;
import py.pol.una.ii.pw.model.Proveedor;
import py.pol.una.ii.pw.util.Factory;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.logging.Logger;

// The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
public class ProveedorRegistration {

    @Inject
    private Logger log;

    @Inject
    private EntityManager em;

    @Inject
    private Event<Proveedor> proveedorEventSrc;

    public void register(Proveedor proveedor) throws Exception {
        log.info("Registrando " + proveedor.getNombre());
        SqlSession sqlSession = Factory.getSqlSessionFactory().openSession();
        try {
            ProveedorMapper Mapper = sqlSession.getMapper(ProveedorMapper.class);
            Mapper.register(proveedor);
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }
    }
    
    public void update(Proveedor proveedor) throws Exception {
    	log.info("Actualizando Proveedor, el nuevo nombre es: " + proveedor.getNombre());
        SqlSession sqlSession = Factory.getSqlSessionFactory().openSession();
        try {
            ProveedorMapper Mapper = sqlSession.getMapper(ProveedorMapper.class);
            Mapper.update(proveedor);
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }
    }
    
    public void remove(long id) throws Exception {
        SqlSession sqlSession = Factory.getSqlSessionFactory().openSession();
        try {
            ProveedorMapper Mapper = sqlSession.getMapper(ProveedorMapper.class);
            Mapper.delete(id);
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }
    }
}