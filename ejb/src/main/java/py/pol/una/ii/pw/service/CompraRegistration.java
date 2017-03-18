package py.pol.una.ii.pw.service;

import py.pol.una.ii.pw.data.ProveedorRepository;
import py.pol.una.ii.pw.model.Compra;
import py.pol.una.ii.pw.model.Producto;
import py.pol.una.ii.pw.model.ProductoComprado;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

// The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateful
@StatefulTimeout(value = 15, unit = TimeUnit.MINUTES)
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

    @PostConstruct
    public void initializateBean(){
        compra_actual = new Compra();
    }

    public void register(Compra compra) throws Exception {
        //Se ingresa la fecha en un formato lindo
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        Date today = Calendar.getInstance().getTime();
        String reportDate = df.format(today);
        compra.setFecha(reportDate);

    	log.info("Registrando compra de:" + compra.getProveedor());
    	tx=context.getUserTransaction();
    	compra_actual=compra;
    	tx.begin();
    	em.persist(compra_actual);
        compraEventSrc.fire(compra);
    }

    public void agregarCarrito (ProductoComprado pc) throws Exception{
        compra_actual.getProductos().add(pc);
        em.persist(compra_actual);
    }

    public void removeItem(Producto p) throws Exception{
        int cont = 0;
        boolean bandera = false;
        for(ProductoComprado pc: compra_actual.getProductos()){
            if(p.getId().equals(pc.getProducto().getId())){
                compra_actual.getProductos().remove(cont);
                em.persist(compra_actual);
                bandera = true;
            }
        }
        if(!bandera)
            log.info("No existe el item que se desea eliminar");
    }

    @Remove
    public void completarCompra() throws Exception {
        tx.commit();
    }

    @Remove
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
