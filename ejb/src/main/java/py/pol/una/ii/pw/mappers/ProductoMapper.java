package py.pol.una.ii.pw.mappers;

import py.pol.una.ii.pw.model.Producto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by carlitos on 11/04/17.
 */
public interface ProductoMapper {
    List<Producto> findAllOrderedByName(Map<String, Object> param);

    Producto findById(long id);

    Producto findByDescripcion(String descripcion);

    void register(Producto Producto);

    void update(Producto Producto);

    void delete(long id);

    Producto findByNameAndDescripcion(Producto p);
}
