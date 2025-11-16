package colectivo.logica;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

/**
 * This class implements the highest priority strategy within the
 * {@code Calculo} service, designed to find routes that can be completed
 * entirely on a single bus line. It inherits core schedule calculation methods
 * from {@code EstrategiaCalculoBase}.
 * 
 * @author Juliana Martin
 * @author Ezequiel Ramos
 * @author Nerea Toledo
 */
public class CalculoDirectoService extends EstrategiaCalculoBase {

	/**
	 * Constructor that injects the system's available lines.
	 * 
	 * @param lineasDelSistema A map of all available {@code Linea} objects.
	 */
	public CalculoDirectoService(Map<String, Linea> lineasDelSistema) {
		super(lineasDelSistema);
	}

	/**
	 * Searches for all valid direct routes between the origin and destination
	 * stops. It iterates through every available line and checks if that line
	 * serves both the origin and the destination in the correct sequence.
	 * 
	 * @param paradaOrigen       The starting stop of the trip.
	 * @param paradaDestino      The final destination stop.
	 * @param diaSemana          The day of the week for schedule lookup.
	 * @param horaLlegaParada    The initial time the passenger arrives at the
	 *                           origin stop.
	 * @param tramos             Map of all available {@code Tramo} objects.
	 * @param todosLosResultados The list where all found routes (List<Recorrido>)
	 *                           are added.
	 * @return true if at least one direct route was found, false otherwise.
	 */
	@Override
	public boolean buscar(Parada paradaOrigen, Parada paradaDestino, int diaSemana, LocalTime horaLlegaParada,
			Map<String, Tramo> tramos, List<List<Recorrido>> todosLosResultados) {

		LOGGER.debug("Buscando viajes directos de {} a {}.", paradaOrigen.getCodigo(), paradaDestino.getCodigo());
		boolean seEncontroAlgo = false;

		for (Linea linea : lineasDelSistema.values()) {
			Optional<Recorrido> recorrido = buscarRecorridoEnLinea(linea, paradaOrigen, paradaDestino, diaSemana,
					horaLlegaParada, tramos);

			if (recorrido.isPresent()) {
				todosLosResultados.add(Collections.singletonList(recorrido.get()));
				seEncontroAlgo = true;
			}
		}

		return seEncontroAlgo;
	}

	/**
	 * Attempts to find a valid direct route on a specific line by checking
	 * geographical viability and matching the time schedule.
	 * 
	 * @param linea           The bus line to check.
	 * @param paradaOrigen    The starting stop.
	 * @param paradaDestino   The final destination stop.
	 * @param diaSemana       The day of the week.
	 * @param horaLlegaParada The passenger's arrival time at the origin.
	 * @param tramos          Map of all available {@code Tramo} objects.
	 * @return An {@code Optional} containing the valid {@code Recorrido} if found,
	 *         or an empty {@code Optional} otherwise.
	 */
	private Optional<Recorrido> buscarRecorridoEnLinea(Linea linea, Parada paradaOrigen, Parada paradaDestino,
			int diaSemana, LocalTime horaLlegaParada, Map<String, Tramo> tramos) {

		IndicesParadas indices = obtenerIndicesParadas(linea, paradaOrigen, paradaDestino);

		if (!indices.esValida())
			return Optional.empty();

		int tiempoHastaOrigen = calcularTiempoEntreParadas(linea.getParadas(), 0, indices.getIdxOrigen(), tramos);

		return buscarPrimeraFrecuenciaValida(linea, diaSemana, horaLlegaParada, tiempoHastaOrigen, indices, tramos);
	}

	/**
	 * Fiinds the indexes of the origin and destination stops within the given
	 * line's route.
	 * 
	 * @param linea         The bus line route list.
	 * @param paradaOrigen  The starting stop.
	 * @param paradaDestino The final destination stop.
	 * @return An {@code IndicesParadas} object containing the indexes.
	 */
	private IndicesParadas obtenerIndicesParadas(Linea linea, Parada paradaOrigen, Parada paradaDestino) {
		List<Parada> paradasDeLaLinea = linea.getParadas();
		int idxOrigen = paradasDeLaLinea.indexOf(paradaOrigen);
		int idxDestino = paradasDeLaLinea.indexOf(paradaDestino);

		return new IndicesParadas(idxOrigen, idxDestino);
	}

	/**
	 * Iterates through the line's schedules to find the first frequency that is
	 * available for the given day and departs at or after the passenger's arrival
	 * time.
	 * 
	 * @param linea             The bus line.
	 * @param diaSemana         The required day of the week.
	 * @param horaLlegaParada   The passenger's arrival time at the origin stop.
	 * @param tiempoHastaOrigen The time elapsed from the start of the route to the
	 *                          origin stop.
	 * @param indices           The origin and destination indices.
	 * @param tramos            Map of all available {@code Tramo} objects.
	 * @return An {@code Optional} containing the valid {@code Recorrido}, or empty
	 *         if none is found.
	 */
	private Optional<Recorrido> buscarPrimeraFrecuenciaValida(Linea linea, int diaSemana, LocalTime horaLlegaParada,
			int tiempoHastaOrigen, IndicesParadas indices, Map<String, Tramo> tramos) {

		for (Linea.Frecuencia frecuencia : linea.getFrecuencias()) {
			Optional<Recorrido> recorrido = procesarFrecuencia(linea, frecuencia, diaSemana, horaLlegaParada,
					tiempoHastaOrigen, indices, tramos);

			if (recorrido.isPresent())
				return recorrido;
		}
		return Optional.empty();
	}

	/**
	 * Checks a single frequency for validity and constructs the {@code Recorrido}
	 * if valid.
	 * 
	 * @param linea             The bus line.
	 * @param frecuencia        The specific schedule frequency to check.
	 * @param diaSemana         The required day of the week.
	 * @param horaLlegaParada   The passenger's arrival time.
	 * @param tiempoHastaOrigen The time from the start of the route to the origin
	 *                          stop.
	 * @param indices           The origin and destination indices.
	 * @param tramos            Map of all available {@code Tramo} objects.
	 * @return An {@code Optional} containing the valid {@code Recorrido}, or empty
	 *         otherwise.
	 */
	private Optional<Recorrido> procesarFrecuencia(Linea linea, Linea.Frecuencia frecuencia, int diaSemana,
			LocalTime horaLlegaParada, int tiempoHastaOrigen, IndicesParadas indices, Map<String, Tramo> tramos) {

		if (!esFrecuenciaValida(frecuencia, diaSemana, horaLlegaParada, tiempoHastaOrigen))
			return Optional.empty();

		return crearRecorrido(linea, frecuencia, tiempoHastaOrigen, indices, tramos);
	}

	/**
	 * Determines if a specific schedule frequency is valid for the requested day
	 * and time. Validity requires the schedule day to match the request day, and
	 * the estimated bus arrival time at the origin must be at or after the
	 * passenger's arrival time.
	 * 
	 * @param frecuencia        The schedule frequency being checked.
	 * @param diaSemana         The required day of the week.
	 * @param horaLlegaParada   The passenger's arrival time.
	 * @param tiempoHastaOrigen The time from the start of the route to the origin
	 *                          stop.
	 * @return true if the frequency is available and on time for the passenger,
	 *         false otherwise.
	 */
	private boolean esFrecuenciaValida(Linea.Frecuencia frecuencia, int diaSemana, LocalTime horaLlegaParada,
			int tiempoHastaOrigen) {

		boolean mismoDia = frecuencia.getDiaSemana() == diaSemana;
		LocalTime horaPasoPorOrigen = frecuencia.getHora().plusSeconds(tiempoHastaOrigen);
		boolean horaValida = !horaPasoPorOrigen.isBefore(horaLlegaParada);

		return mismoDia && horaValida;
	}

	/**
	 * Constructs the final {@code Recorrido} object from a valid frequency.
	 * Calculates the segment duration between the origin and destination stops.
	 * 
	 * @param linea             The bus line.
	 * @param frecuencia        The valid schedule frequency.
	 * @param tiempoHastaOrigen The time from the start of the route to the origin
	 *                          stop.
	 * @param indices           The origin and destination indices.
	 * @param tramos            Map of all available {@code Tramo} objects.
	 * @return An {@code Optional} containing the constructed {@code Recorrido}
	 *         object.
	 */
	private Optional<Recorrido> crearRecorrido(Linea linea, Linea.Frecuencia frecuencia, int tiempoHastaOrigen,
			IndicesParadas indices, Map<String, Tramo> tramos) {

		List<Parada> paradasDeLaLinea = linea.getParadas();

		if (indices.getIdxDestino() + 1 > paradasDeLaLinea.size()) {
			LOGGER.error("Error: Índice fuera de rango (Directo) para línea {}", linea.getCodigo());
			return Optional.empty();
		}

		List<Parada> paradasDelRecorrido = paradasDeLaLinea.subList(indices.getIdxOrigen(),
				indices.getIdxDestino() + 1);

		int duracionTrayecto = calcularTiempoEntreParadas(paradasDeLaLinea, indices.getIdxOrigen(),
				indices.getIdxDestino(), tramos);

		LocalTime horaPasoPorOrigen = frecuencia.getHora().plusSeconds(tiempoHastaOrigen);

		Recorrido recorrido = new Recorrido(linea, new ArrayList<>(paradasDelRecorrido), horaPasoPorOrigen,
				duracionTrayecto);

		return Optional.of(recorrido);
	}

	/**
	 * Inner utility class to encapsulate the indexes and validity of the stops on
	 * the line.
	 */
	private static class IndicesParadas {

		/** The index of the origin stop. */
		private final int idxOrigen;

		/** The index of the destination stop. */
		private final int idxDestino;

		/**
		 * Constructor.
		 * 
		 * @param idxOrigen  The index of the origin stop.
		 * @param idxDestino The index of the destination stop.
		 */
		public IndicesParadas(int idxOrigen, int idxDestino) {
			this.idxOrigen = idxOrigen;
			this.idxDestino = idxDestino;
		}

		/**
		 * @return true if both stops exist on the line (index != -1) and the origin
		 *         precedes the destination (idxOrigen < idxDestino), meaning the trip
		 *         is possible.
		 */
		public boolean esValida() {
			return idxOrigen != -1 && idxDestino != -1 && idxOrigen < idxDestino;
		}

		/** @return The index of the origin stop. */
		public int getIdxOrigen() {
			return idxOrigen;
		}

		/** @return The index of the destination stop. */
		public int getIdxDestino() {
			return idxDestino;
		}
	}
}