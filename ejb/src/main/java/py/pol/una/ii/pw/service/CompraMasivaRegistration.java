package py.pol.una.ii.pw.service;

import com.google.gson.JsonParseException;
import com.sun.media.sound.DLSModulator;
import py.pol.una.ii.pw.model.Compra;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Logger;

// The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class CompraMasivaRegistration {

    @Inject
    private Logger log;

    @Inject
    private EntityManager em;

    @Inject
    private Event<Compra> CompraMasivaEventSrc;

    @Resource
    private EJBContext context;

    private UserTransaction tx;

    public void register(Compra CompraMasiva) throws Exception {
        log.info("Se va a registrar la nueva compra");
        em.persist(CompraMasiva);
        CompraMasivaEventSrc.fire(CompraMasiva);
    }

    public void registerComprasMasivas(String path) throws IOException {
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
                Compra Compra = gs.fromJson(line, Compra.class);
                register(Compra);
            }
            tx.commit();
            log.info("La compra masiva se realizó con exito");
            // note that Scanner suppresses exceptions
            if (sc.ioException() != null) {
                throw sc.ioException();
            }
        } catch (JsonParseException e){
            e.printStackTrace();
            log.info("Error en parsear. No se registraron las compras");
            cancelarCompras();
        } catch (Exception e){
            e.printStackTrace();
            log.info("Se produjo un error inesperado");
            cancelarCompras();
        } finally {
            if (inputStream != null){
                inputStream.close();
            }
            if (sc != null) {
                sc.close();
            }
        }
    }

    public void cancelarCompras(){
        try{
            tx.rollback();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
