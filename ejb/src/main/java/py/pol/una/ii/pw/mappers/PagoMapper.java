package py.pol.una.ii.pw.mappers;

import py.pol.una.ii.pw.model.Pago;

import java.util.List;

/**
 * Created by carlitos on 14/04/17.
 */
public interface PagoMapper {
    Pago findById(long id);

    List<Pago> findAllOrderedById();

    void register(Pago pago);
}
