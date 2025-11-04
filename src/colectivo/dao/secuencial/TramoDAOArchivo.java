package colectivo.dao.secuencial;

import colectivo.conexion.Factory;
import colectivo.dao.ParadaDAO;
import colectivo.dao.TramoDAO;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;
import java.util.Map;
import java.util.Properties;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;

public class TramoDAOArchivo implements TramoDAO {

	private String rutaArchivo;
	private Map<Integer, Parada> paradasDisponibles;
	private Map<String, Tramo> tramosMap;
	private boolean actualizar;

	/**
	 * Constructor that loads configuration properties, initizalizes stops and
	 * prepares the structure to store segments.
	 */
	public TramoDAOArchivo() {
		Properties prop = new Properties();
		try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {

			if (input == null) {
				System.err.println(
						"Error crítico: No se pudo encontrar 'config.properties' en la carpeta src (desde TramoDAO).");
				throw new IOException("Archivo config.properties no encontrado en classpath.");
			}

			prop.load(input);

			this.rutaArchivo = prop.getProperty("tramo");

			if (this.rutaArchivo == null) {
				System.err.println("Error crítico: La clave 'tramo' no se encontró en config.properties.");
			}

		} catch (IOException ex) {
			System.err.println("Error crítico: No se pudo leer el archivo config.properties en TramoDAO.");
			ex.printStackTrace();
		}

		this.paradasDisponibles = cargarParadas();
		this.tramosMap = new LinkedHashMap<>();
		this.actualizar = true;
	}

	@Override
	public void insertar(Tramo tramo) {

	}

	@Override
	public void actualizar(Tramo tramo) {

	}

	@Override
	public void borrar(Tramo tramo) {

	}

	/**
	 * Returns all bus segments (Tramo) currently loaded. If the data needs to
	 * refreshed, reads them from the file first.
	 * 
	 * @return a map containing all loaded segment objects.
	 */
	@Override
	public Map<String, Tramo> buscarTodos() {
		if (this.rutaArchivo == null) {
			System.err.println("Error: No se puede buscar tramos porque la ruta del archivo es nula.");
			return Collections.emptyMap();
		}
		if (actualizar) {
			this.tramosMap = leerDelArchivo(this.rutaArchivo);
			this.actualizar = false;
		}
		return this.tramosMap;
	}

	/**
	 * Private method that contains the logic required to read and process the file.
	 * 
	 * @param ruta the route of the file to be read.
	 * @return a map loaded with the segments (Tramo).
	 */
	private Map<String, Tramo> leerDelArchivo(String ruta) {
		Map<String, Tramo> tramos = new LinkedHashMap<>();

		if (paradasDisponibles == null || paradasDisponibles.isEmpty()) {
			System.err.println("Error: No se pueden cargar los tramos sin las paradas. El mapa de paradas está vacío.");
			return Collections.emptyMap();
		}

		try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
			String linea;
			while ((linea = br.readLine()) != null) {
				if (linea.trim().isEmpty())
					continue;

				String[] partes = linea.split(";");
				int codigoInicio = Integer.parseInt(partes[0].trim());
				int codigoFin = Integer.parseInt(partes[1].trim());
				int tiempo = Integer.parseInt(partes[2].trim());
				int tipo = Integer.parseInt(partes[3].trim());

				Parada paradaInicio = this.paradasDisponibles.get(codigoInicio);
				Parada paradaFin = this.paradasDisponibles.get(codigoFin);

				if (paradaInicio != null && paradaFin != null) {
					Tramo tramo = new Tramo(paradaInicio, paradaFin, tiempo, tipo);
					String clave = codigoInicio + "-" + codigoFin;
					tramos.put(clave, tramo);
				}
			}
		} catch (IOException | NumberFormatException e) {
			e.printStackTrace();
			return Collections.emptyMap();
		}
		return tramos;
	}

	/**
	 * Private method responsible for retrieving the dependency (the map of stops)
	 * 
	 * @return the map loaded with stops.
	 */
	private Map<Integer, Parada> cargarParadas() {
		try {
			// Usa la Factory para obtener el ParadaDAO
			ParadaDAO paradaDAO = (ParadaDAO) Factory.getInstancia("PARADA");
			return paradaDAO.buscarTodos();
		} catch (Exception e) {
			System.err.println("Error al obtener ParadaDAO desde la Factory en TramoDAO.");
			e.printStackTrace();
			return Collections.emptyMap();
		}
	}

	public String getRutaArchivo() {
		return rutaArchivo;
	}
}