package colectivo.logica;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.controlador.Constantes;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

/**
 * Abstract class for route calculation strategies. This class provides common,
 * reusable logic for all route finding strategies (e.g., direct, bus-bus)
 * 
 * @author Juliana Martin
 * @author Ezequiel Ramos
 * @author Nerea Toledo
 */
public abstract class EstrategiaCalculoBase implements CalculoEstrategia {

	/** Map of all available lines in the system. */
	protected final Map<String, Linea> lineasDelSistema;

	/** Logger instance for logging events, errors and exceptions. */
	protected static final Logger LOGGER = LogManager.getLogger(EstrategiaCalculoBase.class);

	/**
	 * Constructor that injects the system's available lines.
	 * 
	 * @param lineasDelSistema A map of all available {@code Linea} objects.
	 */
	public EstrategiaCalculoBase(Map<String, Linea> lineasDelSistema) {
		this.lineasDelSistema = lineasDelSistema;
	}

	/**
	 * Calculates the accumulated travel time (in seconds) between two stop indexes.
	 * It iterates through all intermediate segments and sums up the time, only
	 * considering segments marked as bus segments (type
	 * {@code Constantes.COLECTIVO}).
	 * 
	 * @param paradas   List containing all stops of the current line's route.
	 * @param idxInicio Index of the start stop (inclusive).
	 * @param idxFin    Index of the end stop (exclusive for segment search,
	 *                  inclusive for stops).
	 * @param tramos    Map containing all available segments for time lookup.
	 * @return The total travel time in seconds between the two indexes.
	 */
	protected int calcularTiempoEntreParadas(List<Parada> paradas, int idxInicio, int idxFin,
			Map<String, Tramo> tramos) {

		LOGGER.debug("Calculando tiempo de {} ({}) a {} ({})", paradas.get(idxInicio).getCodigo(), idxInicio,
				paradas.get(idxFin).getCodigo(), idxFin);
		int tiempo = 0;
		for (int i = idxInicio; i < idxFin; i++) {
			if (i + 1 < paradas.size()) {
				String clave = paradas.get(i).getCodigo() + "-" + paradas.get(i + 1).getCodigo();
				Tramo tramo = tramos.get(clave);

				if (tramo != null && tramo.getTipo() == Constantes.COLECTIVO) {
					tiempo += tramo.getTiempo();
					LOGGER.debug(" -> Sumo tramo: {} ({}s). Total: {}s", clave, tramo.getTiempo(), tiempo);
				}
			} else {
				LOGGER.error("Error: Índice fuera de rango en calcularTiempoEntreParadas.");
				break;
			}
		}
		LOGGER.info("Duración final calculada: {} segundos.", tiempo);
		return tiempo;
	}

	/**
	 * Helper method to calculate a single travel segment. Finds the earliest
	 * available bus schedule (frequency) for the given line that departs at or
	 * after the required minimum time.
	 * 
	 * @param linea      The bus line to search within.
	 * @param diaSemana  The day of the week for schedule matching.
	 * @param idxInicio  Index of the segment's origin stop.
	 * @param idxFin     Index of the segment's destination stop.
	 * @param horaMinima The minimum departure time required (passenger's arrival
	 *                   time).
	 * @param tramos     Map containing all available segments for time lookup.
	 * @return The found {@code Recorrido} object if a valid schedule exists, or
	 *         null otherwise.
	 */
	protected Recorrido calcularTramoDeViaje(Linea linea, int diaSemana, int idxInicio, int idxFin,
			LocalTime horaMinima, Map<String, Tramo> tramos) {

		LOGGER.debug("Calculando tramo de viaje con conexión caminando de {} a {}.", idxInicio, idxFin);
		int tiempoHastaInicioTramo = calcularTiempoEntreParadas(linea.getParadas(), 0, idxInicio, tramos);

		Iterator<Linea.Frecuencia> iter = linea.getFrecuencias().iterator();
		Recorrido recorridoEncontrado = null;

		while (iter.hasNext() && recorridoEncontrado == null) {
			Linea.Frecuencia frecuencia = iter.next();
			if (frecuencia.getDiaSemana() == diaSemana) {
				LocalTime horaPasoPorInicio = frecuencia.getHora().plusSeconds(tiempoHastaInicioTramo);
				if (!horaPasoPorInicio.isBefore(horaMinima)) {
					int duracionTramo = calcularTiempoEntreParadas(linea.getParadas(), idxInicio, idxFin, tramos);
					if (idxFin + 1 <= linea.getParadas().size()) {
						List<Parada> paradasTramo = linea.getParadas().subList(idxInicio, idxFin + 1);
						recorridoEncontrado = new Recorrido(linea, new ArrayList<>(paradasTramo), horaPasoPorInicio,
								duracionTramo);
					}
				}
			}
		}

		return recorridoEncontrado;
	}
}