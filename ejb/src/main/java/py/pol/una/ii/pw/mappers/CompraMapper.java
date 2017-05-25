package py.pol.una.ii.pw.mappers;

import py.pol.una.ii.pw.model.Compra;
import py.pol.una.ii.pw.model.ProductoComprado;

import java.util.List;
import java.util.Map;

/**
 * Created by carlitos on 11/04/17.
 */
public interface CompraMapper {
    List<Compra> findAllOrderedByName(Map<String, Object> param);

    Compra findById(long id);

    public int addProducto(Map<String, Object> param);

    public int deleteProducto(Map<String, Object> param);

    public List<ProductoComprado> getProductos(Map<String, Object> param);

    void register(Compra Compra);

    void update(Compra Compra);

    void delete(long id);
}
