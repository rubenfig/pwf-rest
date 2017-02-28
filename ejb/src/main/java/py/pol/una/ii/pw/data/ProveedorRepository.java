package py.pol.una.ii.pw.data;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

import py.pol.una.ii.pw.model.Proveedor;

@ApplicationScoped
public class ProveedorRepository {

    @Inject
    private EntityManager em;

    public Proveedor findById(Long id) {
        return em.find(Proveedor.class, id);
    }

    public Proveedor findByEmail(String email) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Proveedor> criteria = cb.createQuery(Proveedor.class);
        Root<Proveedor> proveedor = criteria.from(Proveedor.class);
        criteria.select(proveedor).where(cb.equal(proveedor.get("email"), email));
        return em.createQuery(criteria).getSingleResult();
    }
    
    public Proveedor findByNombre(String nombre) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Proveedor> criteria = cb.createQuery(Proveedor.class);
        Root<Proveedor> proveedor = criteria.from(Proveedor.class);
        criteria.select(proveedor).where(cb.equal(proveedor.get("nombre"), nombre));
        return em.createQuery(criteria).getSingleResult();
    }

    public List<Proveedor> findAllOrderedByName(String nombre, String telefono) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Proveedor> criteria = cb.createQuery(Proveedor.class);
        Root<Proveedor> proveedor = criteria.from(Proveedor.class);
        criteria.select(proveedor).orderBy(cb.asc(proveedor.get("nombre")));
        if (nombre !=null){
        	if(telefono!=null){
        	criteria.where(cb.equal(proveedor.get("nombre"), nombre),
        			cb.equal(proveedor.get("telefono"), telefono));
        	}else
        		criteria.where(cb.equal(proveedor.get("nombre"), nombre));
        }
        else if (telefono !=null){
        	criteria.where(cb.equal(proveedor.get("telefono"), telefono));
        }
        return em.createQuery(criteria).getResultList();
    }
}
