package py.pol.una.ii.pw.mappers;

import py.pol.una.ii.pw.model.Venta;

import java.util.List;
import java.util.Map;

/**
 * Created by carlitos on 15/04/17.
 */
public interface VentaMasivaMapper {
    int ventaRecordSize();

    List<Venta> listAll(Map<String, Object> param);
}
