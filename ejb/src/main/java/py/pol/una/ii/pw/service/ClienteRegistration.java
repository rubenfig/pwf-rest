package py.pol.una.ii.pw.service;

import org.apache.ibatis.session.SqlSession;
import py.pol.una.ii.pw.mappers.ClienteMapper;
import py.pol.una.ii.pw.model.Cliente;
import py.pol.una.ii.pw.util.Factory;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.logging.Logger;

// The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
public class ClienteRegistration {

    @Inject
    private Logger log;

    @Inject
    private EntityManager em;

    @Inject
    private Event<Cliente> clienteEventSrc;

    public void register(Cliente cliente) throws Exception {
        log.info("Registrando Cliente, el nuevo nombre es: " + cliente.getName());
        SqlSession sqlSession = Factory.getSqlSessionFactory().openSession();
        try {
            ClienteMapper Mapper = sqlSession.getMapper(ClienteMapper.class);
            Mapper.register(cliente);
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }
    }
    
    public void update(Cliente cliente) throws Exception {
    	log.info("Actualizando Cliente, el nuevo nombre es: " + cliente.getName());
        SqlSession sqlSession = Factory.getSqlSessionFactory().openSession();
        try {
            ClienteMapper Mapper = sqlSession.getMapper(ClienteMapper.class);
            Mapper.update(cliente);
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }
    }
    
    public void remove(Long id) throws Exception {
        SqlSession sqlSession = Factory.getSqlSessionFactory().openSession();
        try {
            ClienteMapper Mapper = sqlSession.getMapper(ClienteMapper.class);
            Mapper.delete(id);
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }
    }
}
