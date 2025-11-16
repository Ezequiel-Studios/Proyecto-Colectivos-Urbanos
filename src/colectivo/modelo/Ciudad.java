package colectivo.modelo;

import java.util.Map;
import java.util.Objects;

/**
 * This class represents the entire transportation system of the city. The
 * {@code Ciudad} object is typically loaded once at startup and accessed by
 * services like {@code Calculo} and {@code Coordinador}.
 * 
 * @author Juliana Martin
 * @author Ezequiel Ramos
 * @author Nerea Toledo
 */
public class Ciudad {

	/**
	 * Map of all available bus stops in the system, keyed by their unique integer
	 * code.
	 */
	private final Map<Integer, Parada> paradas;

	/**
	 * Map of all available bus lines in the system, keyed by their unique string
	 * code.
	 */
	private final Map<String, Linea> lineas;

	/**
	 * Map of all available route segments (Tramos), keyed by a composite string
	 * (startId-endId).
	 */
	private final Map<String, Tramo> tramos;

	/**
	 * Constructor for creating the city model. It initializes the core collections
	 * using {@code Objects.requireNonNull} to enforce that these components cannot
	 * be null.
	 * 
	 * @param paradas Map of all stops.
	 * @param lineas  Map of all lines.
	 * @param tramos  Map of all segments.
	 */
	public Ciudad(Map<Integer, Parada> paradas, Map<String, Linea> lineas, Map<String, Tramo> tramos) {
		this.paradas = Objects.requireNonNull(paradas);
		this.lineas = Objects.requireNonNull(lineas);
		this.tramos = Objects.requireNonNull(tramos);
	}

	/** @return The map of all available stops. */
	public Map<Integer, Parada> getParadas() {
		return paradas;
	}

	/** @return The map of all available lines. */
	public Map<String, Linea> getLineas() {
		return lineas;
	}

	/** @return The map of all available segments (Tramos). */
	public Map<String, Tramo> getTramos() {
		return tramos;
	}
}