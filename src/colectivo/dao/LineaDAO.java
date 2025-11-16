package colectivo.dao;

import colectivo.modelo.Linea;
import java.util.Map;

/**
 * Interface that defines the basic operations for managing bus lines. It allows
 * inserting, updating, deleting and retrieving all the stored lines.
 * 
 * @author Juliana Martin
 * @author Ezequiel Ramos
 * @author Nerea Toledo
 */
public interface LineaDAO {

	/**
	 * Creates a new bus line record in the persistance layer.
	 * 
	 * @param linea The {@code Linea} object to be persisted.
	 */
	void insertar(Linea linea);

	/**
	 * Updates an existing bus line record in the persistance layer.
	 * 
	 * @param linea The {@code Linea} object containing the updated data.
	 */
	void actualizar(Linea linea);

	/**
	 * Deletes a bus line record from the persistance layer.
	 * 
	 * @param linea The {@code Linea} object to be deleted.
	 */
	void borrar(Linea linea);

	/**
	 * Retrieves all bus lines stored in the persistance layer.
	 * 
	 * @return A {@code Map} where the key is the unique identifier of the line and
	 *         the value is the corresponding {@code Linea} object.
	 */
	Map<String, Linea> buscarTodos();
}