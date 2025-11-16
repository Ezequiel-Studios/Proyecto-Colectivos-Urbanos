package colectivo.logica;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

/**
 * This interface defines the contract for all route calculation algorithms.
 * Each concrete class implementing this interface (e.g., {@code CalculoDirectoService},
 * {@code CalculoBusBusService}) represents a different strategy for finding a route.
 * @author Juliana Martin
 * @author Ezequiel Ramos
 * @author Nerea Toledo
 * */
public interface CalculoEstrategia {
	
	/**
	 * Attempts to find all possible routes between two stops based on this
	 * strategy. Implementations must add any found routes to the
	 * {@code todosLosResultados} list.
	 * 
	 * @param paradaOrigen       The starting stop of the trip.
	 * @param paradaDestino      The final destination stop.
	 * @param diaSemana          The day of the week (integer code) for schedule
	 *                           lookup.
	 * @param horaLlegaParada    The time of arrival at the origin stop (departure
	 *                           time for the route).
	 * @param tramos             Map of all available {@code Tramo} objects
	 *                           (including walking segments).
	 * @param todosLosResultados The accumulated list where all found routes
	 *                           (List<Recorrido>) are added.
	 * @return true if at least one route was found by this specific strategy, false
	 *         otherwise.
	 */
	boolean buscar(Parada paradaOrigen, Parada paradaDestino, int diaSemana, LocalTime horaLlegaParada,
			Map<String, Tramo> tramos, List<List<Recorrido>> todosLosResultados);
}