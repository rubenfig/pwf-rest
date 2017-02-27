package py.pol.una.ii.pw.data;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import java.util.List;

import py.pol.una.ii.pw.model.Producto;


@ApplicationScoped
public class ProductoRepository {

    @Inject
    private EntityManager em;

    public Producto findById(Long id) {
        return em.find(Producto.class, id);
    }

    public List<Producto> findAllOrderedByName(String nombre, String descripcion, Float precio) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Producto> criteria = cb.createQuery(Producto.class);
        Root<Producto> producto = criteria.from(Producto.class);
        criteria.select(producto).orderBy(cb.asc(producto.get("nombre")));
        if (nombre !=null){
        	criteria.select(producto).where(cb.equal(producto.get("nombre"), nombre));
        }
        if (descripcion !=null){
        	criteria.select(producto).where(cb.equal(producto.get("descripcion"), descripcion));
        }
        if (precio !=null){
        	criteria.select(producto).where(cb.equal(producto.get("precio"), precio));
        }
        return em.createQuery(criteria).getResultList();
    }
    
    public Producto findByDescripcion(String descripcion) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Producto> criteria = cb.createQuery(Producto.class);
        Root<Producto> producto = criteria.from(Producto.class);
        criteria.select(producto).where(cb.equal(producto.get("descripcion"), descripcion));
        return em.createQuery(criteria).getSingleResult();
    }
}
