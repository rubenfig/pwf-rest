package py.pol.una.ii.pw.mappers;

import py.pol.una.ii.pw.model.ProductoComprado;
import py.pol.una.ii.pw.model.Venta;

import java.util.List;
import java.util.Map;

/**
 * Created by carlitos on 11/04/17.
 */
public interface VentaMapper {
    List<Venta> findAllOrderedByName(Map<String, Object> param);

    Venta findById(long id);

    public int addProducto(Map<String, Object> param);

    public int deleteProducto(Map<String, Object> param);

    public List<ProductoComprado> getProductos(Map<String, Object> param);

    void register(Venta Venta);

    void update(Venta Venta);

    void delete(long id);
}
