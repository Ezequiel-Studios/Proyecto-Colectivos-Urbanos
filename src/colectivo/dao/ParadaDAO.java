package colectivo.dao;

import colectivo.modelo.Parada;
import java.util.Map;

/**
 * Interface that defines the basic operations for managing
 * bus stops. It allows inserting, updating, deleting and 
 * retrieving all the stored stops.
 * */
public interface ParadaDAO {
	
	void insertar(Parada parada);

	void actualizar(Parada parada);

	void borrar(Parada parada);

	Map<Integer, Parada> buscarTodos();
}