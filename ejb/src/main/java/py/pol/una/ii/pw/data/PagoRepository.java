package py.pol.una.ii.pw.data;

import org.apache.ibatis.session.SqlSession;
import py.pol.una.ii.pw.mappers.PagoMapper;
import py.pol.una.ii.pw.model.Pago;
import py.pol.una.ii.pw.util.Factory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;


@ApplicationScoped
public class PagoRepository {

    @Inject
    private EntityManager em;

    public Pago findById(Long id) {
        SqlSession sqlSession = Factory.getSqlSessionFactory().openSession();
        try {
            PagoMapper Mapper = sqlSession.getMapper(PagoMapper.class);
            return Mapper.findById(id);
        } finally {
            sqlSession.close();
        }
    }
    
    public List<Pago> findAllOrderedById() {
        SqlSession sqlSession = Factory.getSqlSessionFactory().openSession();
        try {
            PagoMapper Mapper = sqlSession.getMapper(PagoMapper.class);
            return Mapper.findAllOrderedById();
        } finally {
            sqlSession.close();
        }
    }
}
