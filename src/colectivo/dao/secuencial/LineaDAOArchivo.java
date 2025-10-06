package colectivo.dao.secuencial;

import colectivo.dao.LineaDAO;
import colectivo.modelo.Linea;
import java.util.Map;
import java.util.HashMap;

public class LineaDAOArchivo implements LineaDAO {

    private String rutaArchivo;

    public LineaDAOArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    @Override
    public void insertar(Linea linea) {
        // TODO: Implementar escritura en archivo
    }

    @Override
    public void actualizar(Linea linea) {
        // TODO: Actualizar registro en archivo
    }

    @Override
    public void borrar(Linea linea) {
        // TODO: Borrar línea correspondiente en archivo
    }

    @Override
    public Map<String, Linea> buscarTodos() {
        Map<String, Linea> lineas = new HashMap<>();
        // TODO: Leer archivo y cargar las líneas al mapa
        return lineas;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }
}
