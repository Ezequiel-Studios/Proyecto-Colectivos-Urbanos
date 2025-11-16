package colectivo.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a bus stop in the system. It encapsulates the stop's unique code,
 * its address and geographical location (latitude and longitude), and lists the
 * lines that pass through it and other stops reachable by walking.
 * 
 * @author Juliana Martin
 * @author Ezequiel Ramos
 * @author Nerea Toledo
 */
public class Parada {

	/** The unique integer code identifying the stop. */
	private int codigo;

	/** The physical address or name of the stop's location. */
	private String direccion;

	/** The list of bus lines that pass through this stop. */
	private List<Linea> lineas;

	/**
	 * The list of other stops that can be reached directly from here by walking.
	 */
	private List<Parada> paradaCaminando;

	/** The geographical latitude of the stop. */
	private double latitud;

	/** The geographical longitude of the stop. */
	private double longitud;

	/**
	 * Default constructor. Initializes the internal relationship collections (lines
	 * and walk stops).
	 */
	public Parada() {
		this.lineas = new ArrayList<Linea>();
		this.paradaCaminando = new ArrayList<Parada>();
	}

	/**
	 * Constructor for creating a stop with its essential identity and location.
	 * 
	 * @param codigo    The unique integer code of the stop.
	 * @param direccion The address or name of the stop.
	 * @param latitud   The geographical latitude.
	 * @param longitud  The geographical longitude.
	 */
	public Parada(int codigo, String direccion, double latitud, double longitud) {
		super();
		this.codigo = codigo;
		this.direccion = direccion;
		this.latitud = latitud;
		this.longitud = longitud;
		this.lineas = new ArrayList<Linea>();
		this.paradaCaminando = new ArrayList<Parada>();
	}

	/**
	 * Adds a line to the list of lines that serve this stop.
	 * 
	 * @param linea The line object to be added.
	 */
	public void agregarLinea(Linea linea) {
		this.lineas.add(linea);
	}

	/**
	 * Adds a stop to the list of stops that can be reached from here by walking.
	 * 
	 * @param parada The destination stop reachable by walking.
	 */
	public void agregarParadaCaminado(Parada parada) {
		this.paradaCaminando.add(parada);
	}

	/** @return The unique integer code of the stop. */
	public int getCodigo() {
		return codigo;
	}

	/** @param codigo The unique integer code to set. */
	public void setCodigo(int codigo) {
		this.codigo = codigo;
	}

	/** @return The address or name of the stop. */
	public String getDireccion() {
		return direccion;
	}

	/** @param direccion The address or name to set. */
	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	/** @return The geographical latitude. */
	public double getLatitud() {
		return latitud;
	}

	/** @param latitud The geographical latitude to set. */
	public void setLatitud(double latitud) {
		this.latitud = latitud;
	}

	/** @return The geographical longitude. */
	public double getLongitud() {
		return longitud;
	}

	/** @param longitud The geographical longitude to set. */
	public void setLongitud(double longitud) {
		this.longitud = longitud;
	}

	/** @return The list of lines that serve this stop. */
	public List<Linea> getLineas() {
		return lineas;
	}

	/** @return The list of stops directly reachable by walking. */
	public List<Parada> getParadaCaminando() {
		return paradaCaminando;
	}

	/**
	 * Returns a short string representation of the Parada object.
	 * 
	 * @return A descriptive string containing the stop code and address.
	 */
	@Override
	public String toString() {
		return "Parada " + codigo + ": " + direccion;
	}

	/**
	 * Calculates the hash code based on the unique stop code.
	 * 
	 * @return The hash code of the object.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + codigo;
		return result;
	}

	/**
	 * Compares this stop to another object for equality, based solely on the unique
	 * stop code.
	 * 
	 * @param obj The object to compare with.
	 * @return true if the codes are equal, false otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Parada other = (Parada) obj;
		if (codigo != other.codigo)
			return false;
		return true;
	}
}