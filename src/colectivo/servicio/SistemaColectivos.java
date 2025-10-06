package colectivo.servicio;

import colectivo.dao.secuencial.*;
import colectivo.logica.Calculo;
import colectivo.dao.*;

public class SistemaColectivos {
    private Calculo calculo;
    private ParadaDAOArchivo paradaDAO;

    public SistemaColectivos() {
        paradaDAO = new ParadaDAOArchivo();
        calculo = new Calculo();
    }

    public void iniciar() {
        // TODO: cargar datos, iniciar lógica, etc.
    }
}
