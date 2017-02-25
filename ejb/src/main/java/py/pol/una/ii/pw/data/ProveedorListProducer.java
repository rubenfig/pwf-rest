package py.pol.una.ii.pw.data;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import java.util.List;

import py.pol.una.ii.pw.model.Proveedor;

@RequestScoped
public class ProveedorListProducer {

    @Inject
    private ProveedorRepository proveedorRepository;

    private List<Proveedor> proveedores;

    // @Named provides access the return value via the EL variable name "members" in the UI (e.g.,
    // Facelets or JSP view)
    @Produces
    @Named
    public List<Proveedor> getProveedores() {
        return proveedores;
    }

    public void onProveedorListChanged(@Observes(notifyObserver = Reception.IF_EXISTS) final Proveedor proveedor) {
        retrieveAllProveedoresOrderedByName();
    }

    @PostConstruct
    public void retrieveAllProveedoresOrderedByName() {
        proveedores = proveedorRepository.findAllOrderedByName();
    }
}

