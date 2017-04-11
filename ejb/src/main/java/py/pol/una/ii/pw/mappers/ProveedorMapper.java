package py.pol.una.ii.pw.mappers;


import py.pol.una.ii.pw.model.Proveedor;

import java.util.List;
import java.util.Map;

/**
 * Created by ruben on 11/04/17.
 */
public interface ProveedorMapper {
    public List<Proveedor> findAllOrderedByName(Map<String, Object> param);

    public Proveedor findById(long id);

    public void register(Proveedor proveedor);

    public void update(Proveedor proveedor);

    public void delete(long id);
}
