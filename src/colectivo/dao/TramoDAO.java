package colectivo.dao;

import colectivo.modelo.Tramo;
import java.util.Map;

/**
 * Interface that defines the basic operations for managing route segments. It
 * allows inserting, updating, deleting and retrieving all the stored segments.
 * 
 * @author Juliana Martin
 * @author Ezequiel Ramos
 * @author Nerea Toledo
 */
public interface TramoDAO {

	/**
	 * Creates a new route segment record in the persistance layer.
	 * 
	 * @param tramo The {@code Tramo} object to be persisted.
	 */
	void insertar(Tramo tramo);

	/**
	 * Updates an existing route segment record in the persistance layer.
	 * 
	 * @param tramo The {@code Tramo} object containing the updated data.
	 */
	void actualizar(Tramo tramo);

	/**
	 * Deletes a route segment record from the persistance layer.
	 * 
	 * @param tramo The {@code Tramo} object to be deleted.
	 */
	void borrar(Tramo tramo);

	/**
	 * Retrieves all route segments stored in the persistance layer.
	 * 
	 * @return A {@code Map} where the key is the unique identifier of the segment
	 *         and the value is the corresponding {@code Tramo} object.
	 */
	Map<String, Tramo> buscarTodos();
}