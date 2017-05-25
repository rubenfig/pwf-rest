package py.pol.una.ii.pw.mappers;

import py.pol.una.ii.pw.model.Compra;

import java.util.List;
import java.util.Map;

/**
 * Created by carlitos on 14/04/17.
 */
public interface CompraMasivaMapper {
    int compraRecordSize();

    List<Compra> listAll(Map<String, Object> param);

}
