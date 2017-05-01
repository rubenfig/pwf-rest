package py.pol.una.ii.pw.data;

import org.apache.ibatis.session.SqlSession;
import py.pol.una.ii.pw.mappers.ProductoMapper;
import py.pol.una.ii.pw.model.Producto;
import py.pol.una.ii.pw.util.EjbInterceptor;
import py.pol.una.ii.pw.util.Factory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Interceptors(EjbInterceptor.class)
@ApplicationScoped
public class ProductoRepository {

    @Inject
    private EntityManager em;

    public Producto findById(Long id) {
        SqlSession sqlSession = Factory.getSqlSessionFactory().openSession();
        try {
            ProductoMapper Mapper = sqlSession.getMapper(ProductoMapper.class);
            return Mapper.findById(id);
        } finally {
            sqlSession.close();
        }
    }

    public List<Producto> findAllOrderedByName(String nombre, String descripcion, Float precio) {
        SqlSession sqlSession = Factory.getSqlSessionFactory().openSession();
        try {
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("nombre", nombre);
            param.put("descripcion", descripcion);
            param.put("precio", precio);
            ProductoMapper Mapper = sqlSession.getMapper(ProductoMapper.class);
            return Mapper.findAllOrderedByName(param);
        } finally {
            sqlSession.close();
        }
    }
    
    public Producto findByDescripcion(String descripcion) {
        SqlSession sqlSession = Factory.getSqlSessionFactory().openSession();
        try{
            ProductoMapper Mapper = sqlSession.getMapper(ProductoMapper.class);
            return Mapper.findByDescripcion(descripcion);
        } finally {
            sqlSession.close();
        }
    }
}
