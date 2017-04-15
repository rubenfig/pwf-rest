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

import org.apache.ibatis.session.SqlSession;
import py.pol.una.ii.pw.data.ClienteRepository;
import py.pol.una.ii.pw.data.ProductoRepository;
import py.pol.una.ii.pw.mappers.ClienteMapper;
import py.pol.una.ii.pw.mappers.ProductoCompradoMapper;
import py.pol.una.ii.pw.mappers.VentaMapper;
import py.pol.una.ii.pw.model.Cliente;
import py.pol.una.ii.pw.model.Producto;
import py.pol.una.ii.pw.model.ProductoComprado;
import py.pol.una.ii.pw.model.Venta;
import py.pol.una.ii.pw.util.Factory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

// The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateful
@StatefulTimeout(value = 15, unit = TimeUnit.MINUTES)
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

    private VentaMapper Mapper;

    private ProductoCompradoMapper mapperProducto;

    private SqlSession sqlSession;


    @PostConstruct
    public void initializateBean(){
        venta_actual = new Venta();
    }

    public void register(Venta venta) throws Exception {
        //Se ingresa la fecha en un formato lindo
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        Date today = Calendar.getInstance().getTime();
        String reportDate = df.format(today);
        venta.setFecha(reportDate);

        log.info("Registrando venta de:" + venta.getCliente());
        venta_actual=venta;
        sqlSession = Factory.getSqlSessionFactory().openSession();
        Mapper = sqlSession.getMapper(VentaMapper.class);
        mapperProducto = sqlSession.getMapper(ProductoCompradoMapper.class);
        Mapper.register(venta_actual);
        int n=venta_actual.getProductos().size();
        for(int i=0;i<n;i++)
            this.agregarCarrito(venta_actual.getProductos().get(i));
    }

    public void agregarCarrito (ProductoComprado pc) throws Exception{
        mapperProducto.register(pc);
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("id_venta", venta_actual.getId());
        param.put("id_productocomprado", pc.getId());
        Mapper.addProducto(param);
        venta_actual.getProductos().add(pc);
    }

    public void removeItem(Producto p) throws Exception{
        boolean bandera = false;
        Map<String, Object> param = new HashMap<String, Object>();
        int n=venta_actual.getProductos().size();
        for(int i=0;i<n;i++){
            ProductoComprado pc = venta_actual.getProductos().get(i);
            if(p.getId().equals(pc.getProducto().getId())){
                venta_actual.getProductos().remove(i);
                param.put("id_venta", venta_actual.getId());
                param.put("id_productocomprado", pc.getId());
                Mapper.deleteProducto(param);
                bandera = true;
                n--;
            }
        }
        if(!bandera)
            log.info("No existe el item que se desea eliminar");
    }

    @Remove
    public void completarVenta()  {
        try {
            cliente = repoCliente.findById(venta_actual.getCliente().getId());
            //Agregar cuenta de cliente
            Float cuenta = cliente.getCuenta();
            for (ProductoComprado pc : venta_actual.getProductos()) {
                Producto p = repoProducto.findById(pc.getProducto().getId());
                cuenta = cuenta + (p.getPrecio() * pc.getCantidad());
            }
            cliente.setCuenta(cuenta);
            ClienteMapper mapperCliente = sqlSession.getMapper(ClienteMapper.class);
            mapperCliente.updateCuenta(cliente);
            sqlSession.commit();

        } catch (Exception e){
            System.out.println("Fallo el commit");
        } finally {
            sqlSession.close();
        }

    }

    @Remove
    public void cancelarVenta() throws Exception {
        try {
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
    }



}
