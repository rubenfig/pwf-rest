package py.pol.una.ii.pw.service;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import py.pol.una.ii.pw.data.ClienteRepository;
import py.pol.una.ii.pw.data.ProductoRepository;
import py.pol.una.ii.pw.model.Cliente;
import py.pol.una.ii.pw.model.Producto;
import py.pol.una.ii.pw.model.ProductoComprado;
import py.pol.una.ii.pw.model.Venta;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

// The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class VentaMasivaRegistration {

    @Inject
    private Logger log;

    @Inject
    private EntityManager em;

    @Inject
    private Event<Venta> VentaMasivaEventSrc;

    @Inject
    private ClienteRepository repoCliente;

    @Inject
    private ClienteRegistration regCliente;

    @Inject
    private ProductoRepository repoProducto;

    @Resource
    private EJBContext context;

    private UserTransaction tx;

    public void register(Venta VentaMasiva) throws Exception {
        log.info("Se va a registrar la nueva venta");
        Cliente cliente = repoCliente.findById(VentaMasiva.getCliente().getId());
        //Agregar cuenta de cliente
        Float cuenta = cliente.getCuenta();
        for (ProductoComprado pc : VentaMasiva.getProductos()) {
            Producto p = repoProducto.findById(pc.getProducto().getId());
            cuenta = cuenta + (p.getPrecio() * pc.getCantidad());
        }
        cliente.setCuenta(cuenta);
        regCliente.update(cliente);
        em.persist(VentaMasiva);
        VentaMasivaEventSrc.fire(VentaMasiva);
    }

    public String registerVentasMasivas(String path) throws IOException {
        FileInputStream inputStream = null;
        Scanner sc = null;
        try {
            inputStream = new FileInputStream(path);
            sc = new Scanner(inputStream, "UTF-8");
            tx=context.getUserTransaction();
            tx.begin();
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                Gson gs = new Gson();
                Venta Venta = gs.fromJson(line, Venta.class);
                register(Venta);
            }
            tx.commit();
            log.info("La venta masiva se realizó con exito");
            // note that Scanner suppresses exceptions
            if (sc.ioException() != null) {
                throw sc.ioException();
            }
            return "";
        } catch (JsonParseException e){
            e.printStackTrace();
            cancelarVentas();
            return "Error en parsear. No se registraron las ventas";
        } catch (Exception e){
            e.printStackTrace();
            cancelarVentas();
            return "Se produjo un error inesperado";
        } finally {
            if (inputStream != null){
                inputStream.close();
            }
            if (sc != null) {
                sc.close();
            }
        }

    }

    public void cancelarVentas(){
        try{
            tx.rollback();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public int queryVentaRecordsSize() {
        return em.createNamedQuery( "Venta.queryRecordsSize", Long.class )
                .getSingleResult().intValue();
    }

    public List<Venta> listAllVentaEntities(int recordPosition, int recordsPerRoundTrip ) {
        return em.createNamedQuery( "Venta.listAll" )
                .setFirstResult( recordPosition )
                .setMaxResults( recordsPerRoundTrip )
                .getResultList();
    }

}
