package py.pol.una.ii.pw.service;

import py.pol.una.ii.pw.data.ProveedorRepository;
import py.pol.una.ii.pw.model.Compra;
import py.pol.una.ii.pw.model.Producto;
import py.pol.una.ii.pw.model.ProductoComprado;
import py.pol.una.ii.pw.model.Proveedor;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.transaction.UserTransaction;

import java.util.logging.Logger;

// The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateful
@TransactionManagement(TransactionManagementType.BEAN)
public class CompraRegistration {

    @Inject
    private Logger log;

    @Inject
    private EntityManager em;

    @Inject
    private Event<Compra> compraEventSrc;

    @Inject
    private ProveedorRepository repoProveedor;

    @Inject
    ProductoCompradoRegistration registration;

    @Resource
    private EJBContext context;

    private UserTransaction tx;

    private Compra compra_actual;

    public void register(Compra compra) throws Exception {
    	log.info("Registrando compra de:" + compra.getProveedor());
    	tx=context.getUserTransaction();
    	compra_actual=compra;
    	tx.begin();
    	em.persist(compra_actual);
        compraEventSrc.fire(compra);
    }

    public void agregarCarrito (ProductoComprado pc) throws Exception{
        compra_actual.getProductos().add(pc);
    }

    public void completarCompra() throws Exception {
        tx.commit();
    }
    public void cancelarCompra() throws Exception {
        tx.rollback();
    }
    
    public void update(Compra compra) throws Exception {
    	log.info("Actualizando Compra, el nuevo nombre es: " + compra.getId());
    	em.merge(compra);
    	em.flush();
    	compraEventSrc.fire(compra);
    }
    
    public void remove(Compra compra) throws Exception {
    	compra = em.merge(compra);
    	em.remove(compra);
    	em.flush();
    }
}
