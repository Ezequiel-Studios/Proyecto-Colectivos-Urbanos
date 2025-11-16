package colectivo.dao;

import colectivo.modelo.Parada;
import java.util.Map;

/**
 * Interface that defines the basic operations for managing bus stops. It allows
 * inserting, updating, deleting and retrieving all the stored stops.
 * 
 * @author Juliana Martin
 * @author Ezequiel Ramos
 * @author Nerea Toledo
 */
public interface ParadaDAO {

	/**
	 * Creates a new bus stop record in the persistance layer.
	 * 
	 * @param parada The {@code Parada} object to be persisted.
	 */
	void insertar(Parada parada);

	/**
	 * Updates an existing bus stop record in the persistance layer.
	 * 
	 * @param parada The {@code Parada} object containing the updated data.
	 */
	void actualizar(Parada parada);

	/**
	 * Deletes a bus stop record from the persistance layer.
	 * 
	 * @param parada The {@code Parada} object to be deleted.
	 */
	void borrar(Parada parada);

	/**
	 * Retrieves all bus stops stored in the persistance layer.
	 * 
	 * @return A {@code Map} where the key is the unique identifier of the stop and
	 *         the value is the corresponding {@code Parada} object.
	 */
	Map<Integer, Parada> buscarTodos();
}