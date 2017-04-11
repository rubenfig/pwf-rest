package py.pol.una.ii.pw.data;

import org.apache.ibatis.session.SqlSession;
import py.pol.una.ii.pw.mappers.ProveedorMapper;
import py.pol.una.ii.pw.model.Proveedor;
import py.pol.una.ii.pw.util.Factory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ProveedorRepository {

    @Inject
    private EntityManager em;

    public Proveedor findById(Long id) {
        SqlSession sqlSession = Factory.getSqlSessionFactory().openSession();
        try
        {
            ProveedorMapper Mapper = sqlSession.getMapper(ProveedorMapper.class);
            return Mapper.findById(id);
        } finally
        {
            sqlSession.close();
        }
    }


    public List<Proveedor> findAllOrderedByName(String name,String  email,String  telefono) {
        SqlSession sqlSession = Factory.getSqlSessionFactory().openSession();
        try
        {
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("name", name);
            param.put("telefono", telefono);
            param.put("email", email);
            ProveedorMapper Mapper = sqlSession.getMapper(ProveedorMapper.class);
            return Mapper.findAllOrderedByName(param);
        } finally
        {
            sqlSession.close();
        }
    }

    public Proveedor findByEmail(String email) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Proveedor> criteria = cb.createQuery(Proveedor.class);
        Root<Proveedor> proveedor = criteria.from(Proveedor.class);
        criteria.select(proveedor).where(cb.equal(proveedor.get("email"), email));
        return em.createQuery(criteria).getSingleResult();
    }
    

    
}
