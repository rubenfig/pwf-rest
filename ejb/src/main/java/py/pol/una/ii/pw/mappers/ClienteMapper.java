package py.pol.una.ii.pw.mappers;


import py.pol.una.ii.pw.model.Cliente;

import java.util.List;
import java.util.Map;

/**
 * Created by ruben on 11/04/17.
 */
public interface ClienteMapper {
    public List<Cliente> findAllOrderedByName(Map<String, Object> param);

    public Cliente findById (long id);

    public void register (Cliente cliente);

    public void update (Cliente cliente);

    public void updateCuenta(Cliente cliente);

    public void delete (long id);
}
