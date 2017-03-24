package py.pol.una.ii.pw.service;

import com.sun.media.sound.DLSModulator;
import py.pol.una.ii.pw.model.Compra;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Logger;

// The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
public class CompraMasivaRegistration {

    @Inject
    private Logger log;

    @Inject
    private EntityManager em;

    @Inject
    private Event<Compra> CompraMasivaEventSrc;

    public void register(Compra CompraMasiva) throws Exception {
        log.info("Se va a registrar la nueva compra");
        em.persist(CompraMasiva);
        CompraMasivaEventSrc.fire(CompraMasiva);
    }

    public void update(Compra CompraMasiva) throws Exception {
        em.merge(CompraMasiva);
        em.flush();
        CompraMasivaEventSrc.fire(CompraMasiva);
    }

    public void remove(Compra CompraMasiva) throws Exception {
        CompraMasiva = em.merge(CompraMasiva);
        em.remove(CompraMasiva);
        em.flush();
    }

    public void registerComprasMasivas(String path) throws IOException {
        FileInputStream inputStream = null;
        Scanner sc = null;
        try {
            inputStream = new FileInputStream(path);
            sc = new Scanner(inputStream, "UTF-8");
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                Gson gs = new Gson();
                Compra Compra = gs.fromJson(line, Compra.class);
                register(Compra);
            }
            // note that Scanner suppresses exceptions
            if (sc.ioException() != null) {
                throw sc.ioException();
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (inputStream != null){
                inputStream.close();
            }
            if (sc != null) {
                sc.close();
            }
        }
    }
}
