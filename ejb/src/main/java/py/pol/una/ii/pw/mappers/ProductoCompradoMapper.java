package py.pol.una.ii.pw.mappers;

import py.pol.una.ii.pw.model.ProductoComprado;

import java.util.List;
import java.util.Map;

/**
 * Created by carlitos on 13/04/17.
 */
public interface ProductoCompradoMapper {
    List<ProductoComprado> findAllOrderedByName();

    ProductoComprado findById(long id_pc);

    void register(ProductoComprado productocomprado);

    void delete(long id);
}
