package colectivo.dao;

import colectivo.modelo.Linea;
import java.util.Map;

/**
 * Interface that defines the basic operations for managing
 * bus lines. It allows inserting, updating, deleting and 
 * retrieving all the stored lines.
 * */
public interface LineaDAO {

	void insertar(Linea linea);

	void actualizar(Linea linea);

	void borrar(Linea linea);

	Map<String, Linea> buscarTodos();
}