package colectivo.dao.secuencial;

import colectivo.dao.ParadaDAO;
import colectivo.modelo.Parada;
import java.util.Map;
import java.util.HashMap;

public class ParadaDAOArchivo implements ParadaDAO {

    private String rutaArchivo;

    public ParadaDAOArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    @Override
    public void insertar(Parada parada) {
        // TODO: Implementar escritura en archivo
    }

    @Override
    public void actualizar(Parada parada) {
        // TODO: Actualizar registro en archivo
    }

    @Override
    public void borrar(Parada parada) {
        // TODO: Borrar línea correspondiente en archivo
    }

    @Override
    public Map<Integer, Parada> buscarTodos() {
        Map<Integer, Parada> paradas = new HashMap<>();
        // TODO: Leer archivo y cargar las paradas al mapa
        return paradas;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }
}
