package py.pol.una.ii.pw.data;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import py.pol.una.ii.pw.mappers.ProductoCompradoMapper;
import py.pol.una.ii.pw.model.ProductoComprado;
import py.pol.una.ii.pw.util.Factory;


@ApplicationScoped
public class ProductoCompradoRepository {

    @Inject
    private EntityManager em;

    public ProductoComprado findById(Long id_pc) {
        SqlSession sqlSession = Factory.getSqlSessionFactory().openSession();
        try {
            ProductoCompradoMapper Mapper = sqlSession.getMapper(ProductoCompradoMapper.class);
            return Mapper.findById(id_pc);
        } finally {
            sqlSession.close();
        }
    }

    public List<ProductoComprado> findAllOrderedByName() {
        SqlSession sqlSession = Factory.getSqlSessionFactory().openSession();
        try {
            ProductoCompradoMapper Mapper = sqlSession.getMapper(ProductoCompradoMapper.class);
            return Mapper.findAllOrderedByName();
        } finally {
            sqlSession.close();
        }
    }
}
