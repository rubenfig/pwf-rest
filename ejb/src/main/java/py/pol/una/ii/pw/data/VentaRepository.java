package py.pol.una.ii.pw.data;

import org.apache.ibatis.session.SqlSession;
import py.pol.una.ii.pw.mappers.VentaMapper;
import py.pol.una.ii.pw.model.Venta;
import py.pol.una.ii.pw.util.Factory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@ApplicationScoped
public class VentaRepository {

    @Inject
    private EntityManager em;

    public Venta findById(Long id) {

        SqlSession sqlSession = Factory.getSqlSessionFactory().openSession();
        try {
            VentaMapper Mapper = sqlSession.getMapper(VentaMapper.class);
            return Mapper.findById(id);
        } finally {
            sqlSession.close();
        }
    }
    
    public List<Venta> findAllOrderedById(String fecha) {
        SqlSession sqlSession = Factory.getSqlSessionFactory().openSession();
        try {
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("fecha", fecha);
            VentaMapper Mapper = sqlSession.getMapper(VentaMapper.class);
            return Mapper.findAllOrderedByName(param);
        } finally {
            sqlSession.close();
        }
    }
}
