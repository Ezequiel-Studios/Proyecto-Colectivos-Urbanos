package colectivo.dao;

import colectivo.modelo.Tramo;
import java.util.Map;

/**
 * Interface that defines the basic operations for managing
 * route segments. It allows inserting, updating, deleting and 
 * retrieving all the stored segments.
 * */
public interface TramoDAO {
	
	void insertar(Tramo tramo);

	void actualizar(Tramo tramo);

	void borrar(Tramo tramo);

	Map<String, Tramo> buscarTodos();
}