/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package py.pol.una.ii.pw.service;

import py.pol.una.ii.pw.data.ClienteRepository;
import py.pol.una.ii.pw.data.ProductoRepository;
import py.pol.una.ii.pw.model.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.util.logging.Logger;

// The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateful
@TransactionManagement(TransactionManagementType.BEAN)
public class VentaRegistration {

    @Inject
    private Logger log;

    @Inject
    private EntityManager em;

    @Inject
    private Event<Venta> ventaEventSrc;

    @Inject
    private ClienteRepository repoCliente;

    @Inject
    private ClienteRegistration regCliente;

    @Inject
    private ProductoRepository repoProducto;

    private Cliente cliente;

    @Resource
    private EJBContext context;

    private UserTransaction tx;

    private Venta venta_actual;

    @PostConstruct
    public void initializateBean(){
        venta_actual = new Venta();
    }

    public void register(Venta venta) throws Exception {
        log.info("Registrando venta de:" + venta.getCliente());
        tx=context.getUserTransaction();
        venta_actual=venta;
        tx.begin();
        em.persist(venta_actual);
        ventaEventSrc.fire(venta);
    }

    public void agregarCarrito (ProductoComprado pc) throws Exception{
        venta_actual.getProductos().add(pc);
        em.persist(venta_actual);
    }

    @Remove
    public void completarVenta()  {
        try {
            tx.commit();
            //Agregar cuenta de cliente
            Float cuenta = cliente.getCuenta();
            for (ProductoComprado pc : venta_actual.getProductos()) {
                Producto p = repoProducto.findById(pc.getProducto().getId());
                cuenta = cuenta + (p.getPrecio() * pc.getCantidad());
            }
            cliente.setCuenta(cuenta);
            regCliente.update(cliente);

        } catch (Exception e){
            System.out.println("Fallo el commit");
        }

    }

    @Remove
    public void cancelarVenta() throws Exception {
        tx.rollback();
    }



}
