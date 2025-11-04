package colectivo.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a bus stop in the system. Each stop has a code, an address and its
 * location (latitud and longitude), a list of lines and a list of walkable
 * stops.
 */
public class Parada {

	private int codigo;
	private String direccion;
	private List<Linea> lineas;
	private List<Parada> paradaCaminando;
	private double latitud;
	private double longitud;

	public Parada() {
		this.lineas = new ArrayList<Linea>();
		this.paradaCaminando = new ArrayList<Parada>();
	}

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
	 * Adds a line to this stop.
	 * 
	 * @param linea the line to be added.
	 */
	public void agregarLinea(Linea linea) {
		this.lineas.add(linea);
	}

	/**
	 * Adds a stop to the list of stops that the person can walk to.
	 * 
	 * @param parada the stop to be added.
	 */
	public void agregarParadaCaminado(Parada parada) {
		this.paradaCaminando.add(parada);
	}

	public int getCodigo() {
		return codigo;
	}

	public void setCodigo(int codigo) {
		this.codigo = codigo;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public double getLatitud() {
		return latitud;
	}

	public void setLatitud(double latitud) {
		this.latitud = latitud;
	}

	public double getLongitud() {
		return longitud;
	}

	public void setLongitud(double longitud) {
		this.longitud = longitud;
	}

	public List<Linea> getLineas() {
		return lineas;
	}

	public List<Parada> getParadaCaminando() {
		return paradaCaminando;
	}

	@Override
	public String toString() {
		return "Parada " + codigo + ": " + direccion;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + codigo;
		return result;
	}

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