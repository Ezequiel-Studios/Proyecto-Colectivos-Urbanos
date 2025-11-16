package colectivo.logica;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

/**
 * This class is the core service for finding optimal routes between two bus
 * stops. It implements the Strategy Pattern by holding a prioritized list of
 * calculation strategies (e.g., Direct Bus, Bus-Bus transfer, Walking).
 * 
 * @author Juliana Martin
 * @author Ezequiel Ramos
 * @author Nerea Toledo
 */
public class Calculo {

	/** Logger instance for logging events, errors and exceptions. */
	private static final Logger LOGGER = LogManager.getLogger(Calculo.class);

	/**
	 * A prioritized list of route calculation strategies (Direct, Transfer,
	 * Walking).
	 */
	private final List<CalculoEstrategia> estrategias = new ArrayList<>();

	/**
	 * Constructor that initializes the route calculation service. It populates the
	 * internal list of calculation strategies, injecting the system's available
	 * lines into each strategy.
	 * 
	 * @param lineasDelSistema A map of all available {@code Linea} objects in the
	 *                         system, keyed by their code.
	 * @throws IllegalArgumentException if the provided map of lines is null or
	 *                                  empty.
	 */
	public Calculo(Map<String, Linea> lineasDelSistema) {
		if (lineasDelSistema == null || lineasDelSistema.isEmpty())
			throw new IllegalArgumentException("El mapa de líneas del sistema no puede ser nulo o estar vacío.");

		LOGGER.info("Mapa de líneas actualizado con {} líneas.", lineasDelSistema);

		this.estrategias.add(new CalculoDirectoService(lineasDelSistema));
		this.estrategias.add(new CalculoBusBusService(lineasDelSistema));
		this.estrategias.add(new CalculoCaminandoService(lineasDelSistema));
	}

	/**
	 * Calculates all possible routes between two stops based on a prioritized
	 * strategy. The search order follows business priority: 1. Direct Bus, 2.
	 * Bus-Bus Transfer, 3. Walking. The process stops immediately once the first
	 * successful strategy finds results.
	 * 
	 * @param paradaOrigen    The starting bus stop.
	 * @param paradaDestino   The destination bus stop.
	 * @param diaSemana       The day of the week (integer code) for scheduling
	 *                        lookup.
	 * @param horaLlegaParada The time of arrival at the origin stop (departure time
	 *                        for the route).
	 * @param tramos          A map of all available {@code Tramo} objects, keyed by
	 *                        their composite code.
	 * @return A list of lists, where each inner list represents a complete route
	 *         composed of {@code Recorrido} segments. Returns an empty list if no
	 *         route is found.
	 */
	public List<List<Recorrido>> calcularRecorrido(Parada paradaOrigen, Parada paradaDestino, int diaSemana,
			LocalTime horaLlegaParada, Map<String, Tramo> tramos) {
		List<List<Recorrido>> todosLosResultados = new ArrayList<>();

		for (CalculoEstrategia estrategia : estrategias) {
			boolean encontrado = estrategia.buscar(paradaOrigen, paradaDestino, diaSemana, horaLlegaParada, tramos,
					todosLosResultados);

			if (encontrado) {
				LOGGER.info("Cálculo finalizado. Estrategia '{}' encontró {} resultados.",
						estrategia.getClass().getSimpleName(), todosLosResultados.size());

				if (estrategia instanceof CalculoDirectoService) {
					Collections.sort(todosLosResultados,
							Comparator.comparing(viaje -> viaje.get(0).getLinea().getCodigo()));
				} else {

					Collections.sort(todosLosResultados, Comparator.comparing(viaje -> viaje.get(viaje.size() - 1)
							.getHoraSalida().plusSeconds(viaje.get(viaje.size() - 1).getDuracion())));
				}
				return todosLosResultados;
			}
		}
		return todosLosResultados;
	}
}