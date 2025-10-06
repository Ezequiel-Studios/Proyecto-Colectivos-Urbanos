package colectivo.dao.secuencial;

import colectivo.dao.TramoDAO;
import colectivo.modelo.Tramo;
import java.util.Map;
import java.util.HashMap;

public class TramoDAOArchivo implements TramoDAO {

    private String rutaArchivo;

    public TramoDAOArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    @Override
    public void insertar(Tramo tramo) {
        // TODO: Implementar escritura en archivo
    }

    @Override
    public void actualizar(Tramo tramo) {
        // TODO: Actualizar registro en archivo
    }

    @Override
    public void borrar(Tramo tramo) {
        // TODO: Borrar línea correspondiente en archivo
    }

    @Override
    public Map<String, Tramo> buscarTodos() {
        Map<String, Tramo> tramos = new HashMap<>();
        // TODO: Leer archivo y cargar los tramos al mapa
        return tramos;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }
}
