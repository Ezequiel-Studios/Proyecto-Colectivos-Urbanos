package colectivo.logica;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import colectivo.controlador.Constantes;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

/**
 * Concrete Strategy for Bus-Walk-Bus Route Calculation. This strategy searches
 * for routes consisting of three segments: 1. Bus (Line A) 2. Walking (Tramo)
 * 3. Bus (Line C)
 * 
 * @author Juliana Martin
 * @author Ezequiel Ramos
 * @author Nerea Toledo
 */
public class CalculoCaminandoService extends EstrategiaCalculoBase {

	/**
	 * Constructor that injects the system's available lines.
	 * 
	 * @param lineasDelSistema A map of all available {@code Linea} objects.
	 */
	public CalculoCaminandoService(Map<String, Linea> lineasDelSistema) {
		super(lineasDelSistema);
	}

	/**
	 * Searches for all valid three-segment routes involving a walking transfer. The
	 * route is structured as: {@code ParadaOrigen} -> Bus (Line A) -> Walk -> Bus
	 * (Line C) -> {@code ParadaDestino}.
	 * 
	 * @param paradaOrigen       The starting stop of the trip.
	 * @param paradaDestino      The final destination stop.
	 * @param diaSemana          The day of the week for schedule lookup.
	 * @param horaLlegaParada    The initial departure time from the origin.
	 * @param tramos             Map of all available {@code Tramo} objects
	 *                           (including walking segments).
	 * @param todosLosResultados The list where all found routes (List<Recorrido>)
	 *                           are added.
	 * @return true if at least one three-segment route was found, false otherwise.
	 */
	@Override
	public boolean buscar(Parada paradaOrigen, Parada paradaDestino, int diaSemana, LocalTime horaLlegaParada,
			Map<String, Tramo> tramos, List<List<Recorrido>> todosLosResultados) {

		LOGGER.debug("Buscando viajes con conexión caminando de {} a {}.", paradaOrigen.getCodigo(),
				paradaDestino.getCodigo());

		Set<String> combinacionesEncontradas = new HashSet<>();

		for (Linea lineaA : lineasDelSistema.values()) {
			int idxOrigenA = lineaA.getParadas().indexOf(paradaOrigen);

			if (idxOrigenA != -1) {
				buscarConexionesDesdeLineaInicial(lineaA, idxOrigenA, paradaDestino, diaSemana, horaLlegaParada, tramos,
						todosLosResultados, combinacionesEncontradas);
			}
		}
		return !combinacionesEncontradas.isEmpty();
	}

	/**
	 * Iterates over all stops on {@code lineaA} that are after the origin to
	 * identify them as potential drop-off points before the walk segment.
	 * 
	 * @param lineaA                   The starting line.
	 * @param idxOrigenA               The index of the origin stop on Line A.
	 * @param paradaDestino            The final destination stop.
	 * @param diaSemana                The day of the week for schedule lookup.
	 * @param horaLlegaParada          The initial departure time from the origin.
	 * @param tramos                   Map of all available {@code Tramo} objects
	 *                                 (including walking segments).
	 * @param todosLosResultados       The list where all found routes
	 *                                 (List<Recorrido>) are added.
	 * @param combinacionesEncontradas Set used to track unique line combinations (A
	 *                                 -> Walk -> C).
	 */
	private void buscarConexionesDesdeLineaInicial(Linea lineaA, int idxOrigenA, Parada paradaDestino, int diaSemana,
			LocalTime horaLlegaParada, Map<String, Tramo> tramos, List<List<Recorrido>> todosLosResultados,
			Set<String> combinacionesEncontradas) {

		List<Parada> paradasA = lineaA.getParadas();

		for (int i = idxOrigenA + 1; i < paradasA.size(); i++) {
			Parada paradaBajada = paradasA.get(i);

			buscarTramosCaminandoDesdeParada(lineaA, paradaBajada, paradaDestino, diaSemana, horaLlegaParada,
					idxOrigenA, i, tramos, todosLosResultados, combinacionesEncontradas);
		}
	}

	/**
	 * Iterates through all available {@code Tramo} objects to find a valid walking
	 * segment starting at the specified drop-off stop.
	 * 
	 * @param lineaA                   The starting line.
	 * @param paradaBajada             The stop where the passenger gets off Line A.
	 * @param paradaDestino            The final destination stop.
	 * @param diaSemana                The day of the week.
	 * @param horaLlegaParada          The initial departure time.
	 * @param idxOrigenA               Index of the origin stop on Line A.
	 * @param idxBajada                Index of the drop-off stop on Line A.
	 * @param tramos                   Map of all available {@code Tramo} objects.
	 * @param todosLosResultados       List to accumulate results.
	 * @param combinacionesEncontradas Set to track unique line combinations.
	 */
	private void buscarTramosCaminandoDesdeParada(Linea lineaA, Parada paradaBajada, Parada paradaDestino,
			int diaSemana, LocalTime horaLlegaParada, int idxOrigenA, int idxBajada, Map<String, Tramo> tramos,
			List<List<Recorrido>> todosLosResultados, Set<String> combinacionesEncontradas) {

		for (Tramo tramoCaminando : tramos.values()) {
			if (esTramoValidoParaCaminar(tramoCaminando, paradaBajada)) {
				Parada paradaFinCaminata = tramoCaminando.getFin();

				buscarLineaFinalDesdeFinCaminata(lineaA, paradaFinCaminata, paradaDestino, diaSemana, horaLlegaParada,
						idxOrigenA, idxBajada, tramoCaminando, tramos, todosLosResultados, combinacionesEncontradas);
			}
		}
	}

	/**
	 * Checks if the given segment is a valid walking segment starting at the
	 * specified stop.
	 * 
	 * @param tramo        The segment to validate.
	 * @param paradaBajada The required starting stop for the walk.
	 * @return true if the segment starts at {@code paradaBajada} and is of type
	 *         {@code Constantes.CAMINANDO}.
	 */
	private boolean esTramoValidoParaCaminar(Tramo tramo, Parada paradaBajada) {
		return tramo.getInicio().equals(paradaBajada) && tramo.getTipo() == Constantes.CAMINANDO;
	}

	/**
	 * Iterates through all lines in the system to find a final connecting line
	 * (Line C) that serves the end of the walking segment.
	 * 
	 * @param lineaA                   The initial line (Line A).
	 * @param paradaFinCaminata        The stop where the walking segment ends.
	 * @param paradaDestino            The final destination stop.
	 * @param diaSemana                The day of the week.
	 * @param horaLlegaParada          The initial departure time.
	 * @param idxOrigenA               Index of the origin stop on Line A.
	 * @param idxBajada                Index of the drop-off stop on Line A.
	 * @param tramoCaminando           The valid walking segment.
	 * @param tramos                   Map of all available {@code Tramo} objects.
	 * @param todosLosResultados       List to accumulate results.
	 * @param combinacionesEncontradas Set to track unique line combinations.
	 */
	private void buscarLineaFinalDesdeFinCaminata(Linea lineaA, Parada paradaFinCaminata, Parada paradaDestino,
			int diaSemana, LocalTime horaLlegaParada, int idxOrigenA, int idxBajada, Tramo tramoCaminando,
			Map<String, Tramo> tramos, List<List<Recorrido>> todosLosResultados, Set<String> combinacionesEncontradas) {

		for (Linea lineaC : lineasDelSistema.values()) {
			procesarLineaFinal(lineaA, lineaC, paradaFinCaminata, paradaDestino, diaSemana, horaLlegaParada, idxOrigenA,
					idxBajada, tramoCaminando, tramos, todosLosResultados, combinacionesEncontradas);
		}
	}

	/**
	 * Checks the geographical validity of the final bus segment (Line C) and
	 * attempts to calculate the full route schedule if valid.
	 * 
	 * @param lineaA                   The initial line.
	 * @param lineaC                   The final line.
	 * @param paradaFinCaminata        The start stop of Line C (end of walk).
	 * @param paradaDestino            The final destination stop.
	 * @param diaSemana                The day of the week.
	 * @param horaLlegaParada          The initial departure time.
	 * @param idxOrigenA               Index of the origin stop on Line A.
	 * @param idxBajada                Index of the drop-off stop on Line A.
	 * @param tramoCaminando           The valid walking segment.
	 * @param tramos                   Map of all available {@code Tramo} objects.
	 * @param todosLosResultados       List to accumulate results.
	 * @param combinacionesEncontradas Set to track unique line combinations.
	 */
	private void procesarLineaFinal(Linea lineaA, Linea lineaC, Parada paradaFinCaminata, Parada paradaDestino,
			int diaSemana, LocalTime horaLlegaParada, int idxOrigenA, int idxBajada, Tramo tramoCaminando,
			Map<String, Tramo> tramos, List<List<Recorrido>> todosLosResultados, Set<String> combinacionesEncontradas) {

		IndicesLineaFinal indices = obtenerIndicesLineaFinal(lineaC, paradaFinCaminata, paradaDestino);

		if (indices.esValida()) {
			intentarAgregarConexionCaminando(lineaA, lineaC, diaSemana, horaLlegaParada, idxOrigenA, idxBajada, indices,
					tramoCaminando, tramos, todosLosResultados, combinacionesEncontradas);
		}
	}

	/**
	 * Finds the indices of the start and end stops of the final bus segment (Line
	 * C).
	 * 
	 * @param lineaC            The final bus line.
	 * @param paradaFinCaminata The stop where the walk ends (start of Line C).
	 * @param paradaDestino     The final destination stop.
	 * @return A {@code IndicesLineaFinal} object detailing the result and relevant
	 *         indices on Line C.
	 */
	private IndicesLineaFinal obtenerIndicesLineaFinal(Linea lineaC, Parada paradaFinCaminata, Parada paradaDestino) {
		List<Parada> paradasC = lineaC.getParadas();
		int idxOrigenC = paradasC.indexOf(paradaFinCaminata);
		int idxDestinoC = paradasC.indexOf(paradaDestino);

		return new IndicesLineaFinal(idxOrigenC, idxDestinoC);
	}

	/**
	 * Calculates schedules for the three segments (Bus A, Walk, Bus C) and, if
	 * valid, adds the complete route to the results list.
	 * 
	 * @param lineaA                   The initial line.
	 * @param lineaC                   The final line.
	 * @param diaSemana                The day of the week.
	 * @param horaLlegaParada          The initial departure time.
	 * @param idxOrigenA               Index of the origin stop on Line A.
	 * @param idxBajada                Index of the drop-off stop on Line A.
	 * @param indices                  The indices for the final segment (Line C).
	 * @param tramoCaminando           The valid walking segment.
	 * @param tramos                   Map of all available {@code Tramo} objects.
	 * @param todosLosResultados       List to accumulate results.
	 * @param combinacionesEncontradas Set to track unique line combinations.
	 */
	private void intentarAgregarConexionCaminando(Linea lineaA, Linea lineaC, int diaSemana, LocalTime horaLlegaParada,
			int idxOrigenA, int idxBajada, IndicesLineaFinal indices, Tramo tramoCaminando, Map<String, Tramo> tramos,
			List<List<Recorrido>> todosLosResultados, Set<String> combinacionesEncontradas) {

		String combinacion = lineaA.getCodigo() + "->CAMINANDO->" + lineaC.getCodigo();

		if (!combinacionesEncontradas.contains(combinacion)) {
			Recorrido tramo1 = calcularTramoDeViaje(lineaA, diaSemana, idxOrigenA, idxBajada, horaLlegaParada, tramos);

			if (tramo1 != null) {
				construirYAgregarRecorridoCompleto(lineaC, diaSemana, indices, tramo1, tramoCaminando, tramos,
						todosLosResultados, combinacionesEncontradas, combinacion);
			}
		}
	}

	/**
	 * Finalizes the route construction, calculates the second and third segment
	 * schedules, and records the result.
	 * 
	 * @param lineaC                   The final bus line.
	 * @param diaSemana                The day of the week.
	 * @param indices                  The indices for the final segment (Line C).
	 * @param tramo1                   The first segment (Bus A).
	 * @param tramoCaminando           The walking segment geometry and duration.
	 * @param tramos                   Map of all available {@code Tramo} objects.
	 * @param todosLosResultados       List to accumulate results.
	 * @param combinacionesEncontradas Set to track unique line combinations.
	 * @param combinacion              The string representation of the route
	 *                                 combination (A->Walk->C).
	 */
	private void construirYAgregarRecorridoCompleto(Linea lineaC, int diaSemana, IndicesLineaFinal indices,
			Recorrido tramo1, Tramo tramoCaminando, Map<String, Tramo> tramos, List<List<Recorrido>> todosLosResultados,
			Set<String> combinacionesEncontradas, String combinacion) {

		LocalTime horaLlegadaBajada = tramo1.getHoraSalida().plusSeconds(tramo1.getDuracion());

		Recorrido tramoCaminata = crearRecorridoCaminata(tramoCaminando, horaLlegadaBajada);
		LocalTime horaFinCaminata = horaLlegadaBajada.plusSeconds(tramoCaminando.getTiempo());

		Recorrido tramo3 = calcularTramoDeViaje(lineaC, diaSemana, indices.getIdxOrigen(), indices.getIdxDestino(),
				horaFinCaminata, tramos);

		if (tramo3 != null) {
			todosLosResultados.add(List.of(tramo1, tramoCaminata, tramo3));
			combinacionesEncontradas.add(combinacion);
		}
	}

	/**
	 * Creates a {@code Recorrido} object specifically for the walking segment. *
	 * Since walking does not have a line, the {@code Linea} field in the
	 * {@code Recorrido} object is set to null.
	 * 
	 * @param tramoCaminando The {@code Tramo} object defining the walk's geometry
	 *                       and duration.
	 * @param horaInicio     The time the passenger starts walking.
	 * @return The completed {@code Recorrido} object for the walk.
	 */
	private Recorrido crearRecorridoCaminata(Tramo tramoCaminando, LocalTime horaInicio) {
		return new Recorrido(null, List.of(tramoCaminando.getInicio(), tramoCaminando.getFin()), horaInicio,
				tramoCaminando.getTiempo());
	}

	/**
	 * Inner utility class to encapsulate the validity and indexes of the final bus
	 * connection.
	 */
	private static class IndicesLineaFinal {

		private final int idxOrigen;
		private final int idxDestino;

		/**
		 * Constructor.
		 * 
		 * @param idxOrigen  The index of the start stop (end of walk) on the final line
		 *                   (C).
		 * @param idxDestino The index of the final destination stop on Line C.
		 */
		public IndicesLineaFinal(int idxOrigen, int idxDestino) {
			this.idxOrigen = idxOrigen;
			this.idxDestino = idxDestino;
		}

		/**
		 * @return True if both stops exist on the line and the origin index precedes
		 *         the destination index, meaning the trip is forward.
		 */
		public boolean esValida() {
			return idxOrigen != -1 && idxDestino != -1 && idxOrigen < idxDestino;
		}

		/** @return The index of the start stop on the final line. */
		public int getIdxOrigen() {
			return idxOrigen;
		}

		/** @return The index of the destination stop on the final line. */
		public int getIdxDestino() {
			return idxDestino;
		}
	}
}