package py.pol.una.ii.pw.data;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import py.pol.una.ii.pw.model.Cliente;

@RequestScoped
public class ClienteListProduce {

    @Inject
    private ClienteRepository clienteRepository;

    private List<Cliente> clientes;

    // @Named provides access the return value via the EL variable name "clientes" in the UI (e.g.,
    // Facelets or JSP view)
    @Produces
    @Named
    public List<Cliente> getClientes() {
        return clientes;
    }

    public void onClienteListChanged(@Observes(notifyObserver = Reception.IF_EXISTS) final Cliente cliente) {
        retrieveAllClientesOrderedByName(null, null, null);
    }

    @PostConstruct
    public void retrieveAllClientesOrderedByName(String name, String email, String phoneNumber) {
        clientes = clienteRepository.findAllOrderedByName(name, email, phoneNumber);
    }
}
