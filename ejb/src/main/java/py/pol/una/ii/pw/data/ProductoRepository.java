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

    public List<Producto> findAllOrderedByName() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Producto> criteria = cb.createQuery(Producto.class);
        Root<Producto> proveedor = criteria.from(Producto.class);
        criteria.select(proveedor).orderBy(cb.asc(proveedor.get("nombre")));
        return em.createQuery(criteria).getResultList();
    }
}
