package py.pol.una.ii.pw.service;

import py.pol.una.ii.pw.model.Producto;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import java.util.logging.Logger;

// The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
public class ProductoRegistration {

    @Inject
    private Logger log;

    @Inject
    private EntityManager em;

    @Inject
    private Event<Producto> productoEventSrc;

    public void register(Producto producto) throws Exception {
        log.info("Registrando " + producto.getNombre());
        em.persist(producto);
        productoEventSrc.fire(producto);
    }
    
    public void update(Producto producto) throws Exception {
    	log.info("Actualizando Producto, el nuevo nombre es: " + producto.getNombre());
    	em.merge(producto);
    	em.flush();
    	productoEventSrc.fire(producto);
    }
    
    public void remove(Producto producto) throws Exception {
    	producto = em.merge(producto);
    	em.remove(producto);
    	em.flush();
    }
}
