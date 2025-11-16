package colectivo.logica;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

/**
 * This class implements a specific strategy (part of the Strategy Pattern)
 * designed to find routes that require exactly one transfer between two
 * distinct bus lines (Line A -> Line B). It inherits core schedule calculation
 * methods from {@code EstrategiaCalculoBase}.
 * 
 * @author Juliana Martin
 * @author Ezequiel Ramos
 * @author Nerea Toledo
 */
public class CalculoBusBusService extends EstrategiaCalculoBase {

	/**
	 * Constructor that injects the system's available lines.
	 * 
	 * @param lineasDelSistema A map of all available {@code Linea} objects.
	 */
	public CalculoBusBusService(Map<String, Linea> lineasDelSistema) {
		super(lineasDelSistema);
	}

	/**
	 * Searches for all valid two-segment routes involving a bus transfer.
	 * 
	 * @param paradaOrigen       The starting stop of the trip.
	 * @param paradaDestino      The final destination stop.
	 * @param diaSemana          The day of the week for schedule lookup.
	 * @param horaLlegaParada    The initial departure time from the origin.
	 * @param tramos             Map of all available {@code Tramo} objects.
	 * @param todosLosResultados The list where all found routes (List<Recorrido>)
	 *                           are added.
	 * @return true if at least one two-segment route was found, false otherwise.
	 */
	@Override
	public boolean buscar(Parada paradaOrigen, Parada paradaDestino, int diaSemana, LocalTime horaLlegaParada,
			Map<String, Tramo> tramos, List<List<Recorrido>> todosLosResultados) {

		LOGGER.debug("Buscando viajes con conexión de {} a {}.", paradaOrigen.getCodigo(), paradaDestino.getCodigo());
		Set<String> combinacionesEncontradas = new HashSet<>();

		for (Linea lineaA : lineasDelSistema.values()) {
			int idxOrigenA = lineaA.getParadas().indexOf(paradaOrigen);

			if (idxOrigenA != -1) {
				buscarConexionesDesdeLinea(lineaA, idxOrigenA, paradaDestino, diaSemana, horaLlegaParada, tramos,
						todosLosResultados, combinacionesEncontradas);
			}
		}
		return !combinacionesEncontradas.isEmpty();
	}

	/**
	 * Iterates over all stops on {@code lineaA} that are after the origin to
	 * identify them as potential transfer points.
	 * 
	 * @param lineaA                   The starting line.
	 * @param idxOrigenA               The index of the origin stop on Line A.
	 * @param paradaDestino            The final destination stop.
	 * @param diaSemana                The day of the week.
	 * @param horaLlegaParada          The initial departure time.
	 * @param tramos                   Map of all available segments.
	 * @param todosLosResultados       List to accumulate results.
	 * @param combinacionesEncontradas Set to track unique Line A -> Line B
	 *                                 combinations.
	 */
	private void buscarConexionesDesdeLinea(Linea lineaA, int idxOrigenA, Parada paradaDestino, int diaSemana,
			LocalTime horaLlegaParada, Map<String, Tramo> tramos, List<List<Recorrido>> todosLosResultados,
			Set<String> combinacionesEncontradas) {

		List<Parada> paradasA = lineaA.getParadas();

		for (int i = idxOrigenA + 1; i < paradasA.size(); i++) {
			Parada paradaTransbordo = paradasA.get(i);

			buscarSegundaLineaDesdeTransbordo(lineaA, paradaTransbordo, paradaDestino, diaSemana, horaLlegaParada,
					idxOrigenA, i, tramos, todosLosResultados, combinacionesEncontradas);
		}
	}

	/**
	 * Iterates through all other lines (Line B) to find a valid continuation from
	 * the transfer stop to the final destination.
	 * 
	 * @param lineaA                   The first line (used to ensure the two lines
	 *                                 are different).
	 * @param paradaTransbordo         The stop where the transfer occurs.
	 * @param paradaDestino            The final destination stop.
	 * @param diaSemana                The day of the week.
	 * @param horaLlegaParada          The initial departure time.
	 * @param idxOrigenA               The index of the origin stop on Line A.
	 * @param idxTransbordoA           The index of the transfer stop on Line A.
	 * @param tramos                   Map of all available segments.
	 * @param todosLosResultados       List to accumulate results.
	 * @param combinacionesEncontradas Set to track unique combinations.
	 */
	private void buscarSegundaLineaDesdeTransbordo(Linea lineaA, Parada paradaTransbordo, Parada paradaDestino,
			int diaSemana, LocalTime horaLlegaParada, int idxOrigenA, int idxTransbordoA, Map<String, Tramo> tramos,
			List<List<Recorrido>> todosLosResultados, Set<String> combinacionesEncontradas) {

		for (Linea lineaB : lineasDelSistema.values()) {
			if (lineaA.equals(lineaB)) {
				continue;
			}
			ConexionValida conexion = validarConexion(lineaB, paradaTransbordo, paradaDestino);

			if (conexion.esValida()) {
				intentarAgregarConexion(lineaA, lineaB, diaSemana, horaLlegaParada, idxOrigenA, idxTransbordoA,
						conexion, tramos, todosLosResultados, combinacionesEncontradas);
			}
		}
	}

	/**
	 * Checks if Line B starts at the transfer stop and contains the destination
	 * stop *after* the transfer stop in its route sequence.
	 * 
	 * @param lineaB           The second line.
	 * @param paradaTransbordo The required starting stop for Line B.
	 * @param paradaDestino    The required destination stop for Line B.
	 * @return A {@code ConexionValida} object detailing the result and relevant
	 *         indices on Line B.
	 */
	private ConexionValida validarConexion(Linea lineaB, Parada paradaTransbordo, Parada paradaDestino) {
		List<Parada> paradasB = lineaB.getParadas();
		int idxTransbordoB = paradasB.indexOf(paradaTransbordo);
		int idxDestinoB = paradasB.indexOf(paradaDestino);

		boolean esValida = idxTransbordoB != -1 && idxDestinoB != -1 && idxTransbordoB < idxDestinoB;
		return new ConexionValida(esValida, idxTransbordoB, idxDestinoB);
	}

	/**
	 * Calculates the schedule and duration for both segments (Line A and Line B)
	 * and adds the complete, valid route to the results list. * Utilizes
	 * {@code calcularTramoDeViaje} (inherited from base class) to find a valid
	 * departure time for each segment. * Prevents adding duplicate line
	 * combinations (A->B) to keep results clean.
	 * 
	 * @param lineaA                   The starting line.
	 * @param lineaB                   The connecting line.
	 * @param diaSemana                The day of the week.
	 * @param horaLlegaParada          The initial departure time.
	 * @param idxOrigenA               Index of the origin stop on Line A.
	 * @param idxTransbordoA           Index of the transfer stop on Line A.
	 * @param conexion                 The validated connection object containing
	 *                                 Line B indices.
	 * @param tramos                   Map of all available segments.
	 * @param todosLosResultados       List to accumulate results.
	 * @param combinacionesEncontradas Set to track unique combinations.
	 */
	private void intentarAgregarConexion(Linea lineaA, Linea lineaB, int diaSemana, LocalTime horaLlegaParada,
			int idxOrigenA, int idxTransbordoA, ConexionValida conexion, Map<String, Tramo> tramos,
			List<List<Recorrido>> todosLosResultados, Set<String> combinacionesEncontradas) {

		String combinacion = lineaA.getCodigo() + "->" + lineaB.getCodigo();

		if (combinacionesEncontradas.contains(combinacion)) {
			return;
		}

		Recorrido tramo1 = calcularTramoDeViaje(lineaA, diaSemana, idxOrigenA, idxTransbordoA, horaLlegaParada, tramos);

		if (tramo1 == null) {
			return;
		}

		LocalTime horaLlegadaTransbordo = tramo1.getHoraSalida().plusSeconds(tramo1.getDuracion());
		Recorrido tramo2 = calcularTramoDeViaje(lineaB, diaSemana, conexion.idxTransbordo, conexion.idxDestino,
				horaLlegadaTransbordo, tramos);

		if (tramo2 != null) {
			todosLosResultados.add(List.of(tramo1, tramo2));
			combinacionesEncontradas.add(combinacion);
		}
	}

	/**
	 * Inner utility class to encapsulate the validity and indexes of a bus
	 * connection.
	 */
	private static class ConexionValida {

		private final boolean valida;
		private final int idxTransbordo;
		private final int idxDestino;

		/**
		 * Constructor
		 * 
		 * @param valida        True if the connection is geographically valid.
		 * @param idxTransbordo The index of the transfer stop on the second line.
		 * @param idxDestino    The index of the destination stop on the second line.
		 */
		public ConexionValida(boolean valida, int idxTransbordo, int idxDestino) {
			this.valida = valida;
			this.idxTransbordo = idxTransbordo;
			this.idxDestino = idxDestino;
		}

		/** @return True if the connection is valid. */
		public boolean esValida() {
			return valida;
		}
	}
}