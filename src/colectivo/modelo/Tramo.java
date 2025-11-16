package colectivo.modelo;

import colectivo.controlador.Constantes;

/**
 * Represents connection segment between two stops, defining it travel time and
 * type (bus or walking).
 * 
 * @author Juliana Martin
 * @author Ezequiel Ramos
 * @author Nerea Toledo
 */
public class Tramo {

	/** The starting stop of the segment. */
	private Parada inicio;

	/** The ending stop of the segment. */
	private Parada fin;

	/** The duration of the segment in seconds. */
	private int tiempo;

	/**
	 * The type of the segment (e.g., {@code Constantes.COLECTIVO} or
	 * {@code Constantes.CAMINANDO}).
	 */
	private int tipo;

	/**
	 * Default constructor.
	 */
	public Tramo() {

	}

	/**
	 * Constructor for creating a route segment.
	 * 
	 * @param inicio The starting stop of the segment.
	 * @param fin    The ending stop of the segment.
	 * @param tiempo The duration of the segment in seconds.
	 * @param tipo   The type of the segment (e.g., {@code Constantes.COLECTIVO} or
	 *               {@code Constantes.CAMINANDO}).
	 */
	public Tramo(Parada inicio, Parada fin, int tiempo, int tipo) {
		this.inicio = inicio;
		this.fin = fin;
		this.tiempo = tiempo;
		this.tipo = tipo;
		if (tipo == Constantes.CAMINANDO) {
			inicio.agregarParadaCaminado(fin);
			fin.agregarParadaCaminado(inicio);
		}
	}

	/** @return The starting stop of the segment. */
	public Parada getInicio() {
		return inicio;
	}

	/** @param inicio The starting stop to set. */
	public void setInicio(Parada inicio) {
		this.inicio = inicio;
	}

	/** @return The ending stop of the segment. */
	public Parada getFin() {
		return fin;
	}

	/** @param fin The ending stop to set. */
	public void setFin(Parada fin) {
		this.fin = fin;
	}

	/** @return The duration of the segment in seconds. */
	public int getTiempo() {
		return tiempo;
	}

	/** @param tiempo The duration in seconds to set. */
	public void setTiempo(int tiempo) {
		this.tiempo = tiempo;
	}

	/** @return The type of the segment (Bus or Walking constant). */
	public int getTipo() {
		return tipo;
	}

	/** @param tipo The type of the segment to set. */
	public void setTipo(int tipo) {
		this.tipo = tipo;
	}

	/**
	 * Calculates the hash code based on the unique combination of the start and end
	 * stops.
	 * 
	 * @return The hash code of the object.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fin == null) ? 0 : fin.hashCode());
		result = prime * result + ((inicio == null) ? 0 : inicio.hashCode());
		return result;
	}

	/**
	 * Compares this segment to another object for equality, based solely on the
	 * start stop and the end stop.
	 * 
	 * @param obj The object to compare with.
	 * @return true if both the start and end stops are equal, false otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tramo other = (Tramo) obj;
		if (fin == null) {
			if (other.fin != null)
				return false;
		} else if (!fin.equals(other.fin))
			return false;
		if (inicio == null) {
			if (other.inicio != null)
				return false;
		} else if (!inicio.equals(other.inicio))
			return false;
		return true;
	}

	/**
	 * Returns a string representation of the segment.
	 * 
	 * @return A descriptive string containing the start stop, end stop, duration,
	 *         and type.
	 */
	@Override
	public String toString() {
		return "Tramo [inicio=" + inicio + ", fin=" + fin + ", tiempo=" + tiempo + ", tipo=" + tipo + "]";
	}
}