package py.pol.una.ii.pw.service;

import com.google.gson.GsonBuilder;
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

import java.io.*;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;
import javax.json.Json;

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

    /*private int queryCompraRecordsSize() {
        return em.createNamedQuery( "Compra.queryRecordsSize", Long.class )
                .getSingleResult().intValue();
    }

    private List<Compra> listAllCompraEntities(int recordPosition, int recordsPerRoundTrip ) {
        return em.createNamedQuery( "Compra.listAll" )
                .setFirstResult( recordPosition )
                .setMaxResults( recordsPerRoundTrip )
                .getResultList();
    }*/

    /*public void write( OutputStream os ) throws IOException {
        int recordsPerRoundTrip = 100;                      // Number of records for every round trip to the database
        int recordPosition = 0;                             // Initial record position index
        int recordSize = queryCompraRecordsSize();   // Total records found for the query

        // Start streaming the data
        try ( PrintWriter writer = new PrintWriter( new BufferedWriter( new OutputStreamWriter( os ) ) ) ) {

            writer.print( "{\"result\": [" );

            while ( recordSize > 0 ) {
                // Get the paged data set from the DB
                List<Compra> compras = listAllCompraEntities( recordPosition, recordsPerRoundTrip );

                for ( Compra compra : compras ) {
                    if ( recordPosition > 0 ) {
                        writer.print( "," );
                    }

                    // Stream the data in Json object format
                    writer.print( Json.createObjectBuilder()
                            .add( "id", compra.getId() )
                            .add( "fecha", compra.getFecha() )
                            .add( "proveedor", compra.getProveedor().toString() )
                            .add( "productos", compra.getProductos().toString() )
                            .build().toString() );

                    // Increase the recordPosition for every record streamed
                    recordPosition++;
                }

                // update the recordSize (remaining no. of records)
                recordSize -= recordsPerRoundTrip;
            }

            // Done!
            writer.print( "]}" );
        }
    }*/

}
