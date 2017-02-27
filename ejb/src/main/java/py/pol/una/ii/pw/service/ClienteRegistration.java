package py.pol.una.ii.pw.service;

import py.pol.una.ii.pw.model.Cliente;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.logging.Logger;

// The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
public class ClienteRegistration {

    @Inject
    private Logger log;

    @Inject
    private EntityManager em;

    @Inject
    private Event<Cliente> clienteEventSrc;

    public void register(Cliente cliente) throws Exception {
        log.info("Registrando " + cliente.getName());
        em.persist(cliente);
        clienteEventSrc.fire(cliente);
    }
    
    public void update(Cliente cliente) throws Exception {
    	log.info("Actualizando Cliente, el nuevo nombre es: " + cliente.getName());
    	em.merge(cliente);
    	em.flush();
    	clienteEventSrc.fire(cliente);
    }
    
    public void remove(Cliente cliente) throws Exception {
    	cliente = em.merge(cliente);
    	em.remove(cliente);
    	em.flush();
    }
}
