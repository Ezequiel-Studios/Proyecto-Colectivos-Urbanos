package colectivo.modelo;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a bus line withing the transporation system. Each line has a code,
 * a name, a list of stops and a list of frequencies (schedules).
 * 
 * @author Juliana Martin
 * @author Ezequiel Ramos
 * @author Nerea Toledo
 */
public class Linea {

	/** The unique code identifying the line (e.g., "L1", "L2"). */
	private String codigo;

	/** The human-readable name of the line. */
	private String nombre;

	/** The ordered list of stops (the route) this line serves. */
	private List<Parada> paradas;

	/** The list of scheduled departure times (frequencies) for this line. */
	private List<Frecuencia> frecuencias;

	/**
	 * Default constructor. Initializes the internal collections (stops and
	 * frequencies).
	 */
	public Linea() {
		this.paradas = new ArrayList<Parada>();
		this.frecuencias = new ArrayList<Frecuencia>();
	}

	/**
	 * Constructor for creating a line with essential identity.
	 * 
	 * @param codigo The unique code for the line.
	 * @param nombre The name of the line.
	 */
	public Linea(String codigo, String nombre) {
		super();
		this.codigo = codigo;
		this.nombre = nombre;
		this.paradas = new ArrayList<Parada>();
		this.frecuencias = new ArrayList<Frecuencia>();
	}

	/**
	 * Adds a stop to this line's route and establishes bidirectional link.
	 * 
	 * @param parada the stop to be added.
	 */
	public void agregarParada(Parada parada) {
		paradas.add(parada);
		parada.agregarLinea(this);
	}

	/**
	 * Adds a frequency for this line. Creates a new {@code Frecuencia} object and
	 * adds it to the list.
	 * 
	 * @param diaSemana Day of the week.
	 * @param hora      The scheduled departure time from the route's starting
	 *                  point.
	 */
	public void agregarFrecuencia(int diaSemana, LocalTime hora) {
		frecuencias.add(new Frecuencia(diaSemana, hora));
	}

	/** @return The unique code of the line. */
	public String getCodigo() {
		return codigo;
	}

	/** @param codigo The unique code to set. */
	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	/** @return The name of the line. */
	public String getNombre() {
		return nombre;
	}

	/** @param nombre The name to set. */
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	/** @return The ordered list of stops served by this line. */
	public List<Parada> getParadas() {
		return paradas;
	}

	/** @return The list of schedule frequencies. */
	public List<Frecuencia> getFrecuencias() {
		return frecuencias;
	}

	/**
	 * Calculates the hash code based on the unique line code.
	 * 
	 * @return The hash code of the object.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((codigo == null) ? 0 : codigo.hashCode());
		return result;
	}

	/**
	 * Compares this line to another object for equality, based solely on the unique
	 * line code.
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
		Linea other = (Linea) obj;
		if (codigo == null) {
			if (other.codigo != null)
				return false;
		} else if (!codigo.equals(other.codigo))
			return false;
		return true;
	}

	/**
	 * Returns a short string representation of the Linea object.
	 * 
	 * @return A descriptive string containing the line's code and name.
	 */
	@Override
	public String toString() {
		return "Linea [codigo=" + codigo + ", nombre=" + nombre + "]";
	}

	/**
	 * Inner class representing a single instance of a schedule departure from the
	 * line's initial stop for a specific say of the week.
	 */
	public class Frecuencia {

		/** The integer code for the day of the week (e.g., 1=Mon, 7=Sun). */
		private int diaSemana;

		/** The scheduled departure time from the starting stop (LocalTime). */
		private LocalTime hora;

		/**
		 * Constructor for a new frequency entry.
		 * 
		 * @param diaSemana Day of the week.
		 * @param hora      Departure time.
		 */
		public Frecuencia(int diaSemana, LocalTime hora) {
			super();
			this.diaSemana = diaSemana;
			this.hora = hora;
		}

		/** @return The integer code for the day of the week. */
		public int getDiaSemana() {
			return diaSemana;
		}

		/** @param diaSemana The day of the week code to set. */
		public void setDiaSemana(int diaSemana) {
			this.diaSemana = diaSemana;
		}

		/** @return The scheduled departure time. */
		public LocalTime getHora() {
			return hora;
		}

		/** @param hora The departure time to set. */
		public void setHora(LocalTime hora) {
			this.hora = hora;
		}

		/**
		 * Returns a String representation of the Frecuencia object.
		 * 
		 * @return A descriptive string containing the day and time.
		 */
		@Override
		public String toString() {
			return "Frecuencia [diaSemana=" + diaSemana + ", hora=" + hora + "]";
		}
	}
}