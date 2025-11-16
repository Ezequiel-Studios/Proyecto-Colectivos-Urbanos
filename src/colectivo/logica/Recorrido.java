package colectivo.logica;

import java.time.LocalTime;
import java.util.List;

import colectivo.modelo.Linea;
import colectivo.modelo.Parada;

/**
 * Represents a route in the system. Each route has a line, a duration, a list
 * of stops and a departure time.
 * 
 * @author Juliana Martin
 * @author Ezequiel Ramos
 * @author Nerea Toledo
 */
public class Recorrido {

	/**
	 * The bus line used for this segment, or {@code null} if the segment is a
	 * walking trip.
	 */
	private Linea linea;

	/**
	 * The sequential list of stops (or segment endpoints) covered by this segment.
	 */
	private List<Parada> paradas;

	/** The calculated departure time from the first stop of this segment. */
	private LocalTime horaSalida;

	/** The total duration of this segment in seconds. */
	private int duracion;

	/**
	 * Constructor for creating a new route segment.
	 * 
	 * @param linea      The bus line used, or null for walking segments.
	 * @param paradas    The list of stops/endpoints included in this segment.
	 * @param horaSalida The departure time from the start stop of the segment.
	 * @param duracion   The total duration of the segment in seconds.
	 */
	public Recorrido(Linea linea, List<Parada> paradas, LocalTime horaSalida, int duracion) {
		super();
		this.linea = linea;
		this.paradas = paradas;
		this.horaSalida = horaSalida;
		this.duracion = duracion;
	}

	/** @return The bus line used for this segment, or null. */
	public Linea getLinea() {
		return linea;
	}

	/** @param linea The bus line to set. */
	public void setLinea(Linea linea) {
		this.linea = linea;
	}

	/** @return The list of stops/endpoints covered by this segment. */
	public List<Parada> getParadas() {
		return paradas;
	}

	/** @param paradas The list of stops/endpoints to set. */
	public void setParadas(List<Parada> paradas) {
		this.paradas = paradas;
	}

	/** @return The calculated departure time for this segment. */
	public LocalTime getHoraSalida() {
		return horaSalida;
	}

	/** @param horaSalida The departure time to set. */
	public void setHoraSalida(LocalTime horaSalida) {
		this.horaSalida = horaSalida;
	}

	/** @return The total duration of the segment in seconds. */
	public int getDuracion() {
		return duracion;
	}

	/** @param duracion The total duration of the segment in seconds to set. */
	public void setDuracion(int duracion) {
		this.duracion = duracion;
	}

	/**
	 * Returns a String representation of the {@code Recorrido} object.
	 * 
	 * @return A descriptive string containing the segment details.
	 */
	@Override
	public String toString() {
		return "Recorrido{" + "linea=" + linea + ", paradas=" + paradas + ", horaSalida=" + horaSalida + ", duracion="
				+ duracion + " minutos" + '}';
	}
}