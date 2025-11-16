package colectivo.dao.secuencial;

import colectivo.conexion.Factory;
import colectivo.dao.ParadaDAO;
import colectivo.dao.TramoDAO;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;

/**
 * Concrete implementation of {@code TramoDAO} using sequential files. This
 * class implements the {@code TramoDAO} contract, handling persistence
 * operations by reading and processing data from structured text files (as an
 * alternative to a relational database).
 * 
 * @author Juliana Martin
 * @author Ezequiel Ramos
 * @author Nerea Toledo
 */
public class TramoDAOArchivo implements TramoDAO {

	/** Path to the sequential file containing the route segment data. */
	private String rutaArchivo;

	/** Cache map containing all available stops. */
	private Map<Integer, Parada> paradasDisponibles;

	/**
	 * Cache map where loaded {@code Tramo} objects are stored, keyed by a composite
	 * ID.
	 */
	private Map<String, Tramo> tramosMap;

	/**
	 * Flag indicating if the cache needs to be refreshed by reading the files
	 * again.
	 */
	private boolean actualizar;

	/** Logger instance for logging events, errors and exceptions. */
	private static final Logger LOGGER = LogManager.getLogger(TramoDAOArchivo.class);

	/**
	 * Constructor that loads configuration properties, initizalizes stops and
	 * prepares the structure to store segments.
	 */
	public TramoDAOArchivo() {
		Properties prop = new Properties();
		try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {

			if (input == null) {
				LOGGER.error(
						"Error crítico: No se pudo encontrar 'config.properties' en la carpeta src (desde TramoDAO).");
				throw new IOException("Archivo config.properties no encontrado en classpath.");
			}

			prop.load(input);

			this.rutaArchivo = prop.getProperty("tramo");

			if (this.rutaArchivo == null) {
				LOGGER.fatal("Error crítico: la clave 'tramo' no se encontró en config.properties.");
			}

		} catch (IOException ex) {
			LOGGER.error("Error crítico: No se pudo leer el archivo config.properties en TramoDAO.", ex);
		}

		this.paradasDisponibles = cargarParadas();
		this.tramosMap = new LinkedHashMap<>();
		this.actualizar = true;
	}

	/** Method not implemented in the current version. */
	@Override
	public void insertar(Tramo tramo) {

	}

	/** Method not implemented in the current version. */
	@Override
	public void actualizar(Tramo tramo) {

	}

	/** Method not implemented in the current version. */
	@Override
	public void borrar(Tramo tramo) {

	}

	/**
	 * Returns all bus segments ({@code Tramo} objects) currently loaded. If the
	 * data needs to refreshed, reads them from the file first.
	 * 
	 * @return a map containing all loaded segment objects.
	 */
	@Override
	public Map<String, Tramo> buscarTodos() {
		if (this.rutaArchivo == null) {
			LOGGER.warn("Error: No se puede buscar tramos porque la ruta del archivo es nula.");
			return Collections.emptyMap();
		}
		if (actualizar) {
			this.tramosMap = leerDelArchivo(this.rutaArchivo);
			this.actualizar = false;
			LOGGER.info("Carga de tramos finalizada con éxito. Tramos cargados: {}", this.tramosMap.size());
		}
		return this.tramosMap;
	}

	/**
	 * Private method that contains the logic required to read and process the file.
	 * Reads line by line, parses segment attributes (start/end IDs, time, type).
	 * Looks up the start and end IDs in {@code paradasDisponibles} to obtain the
	 * actual {@code Parada} objects, establishing the object reference.
	 * 
	 * @param ruta The path of the file to be read.
	 * @return A map loaded with the segments (Tramo).
	 */
	private Map<String, Tramo> leerDelArchivo(String ruta) {
		Map<String, Tramo> tramos = new LinkedHashMap<>();

		if (paradasDisponibles == null || paradasDisponibles.isEmpty()) {
			LOGGER.error("Error: No se pueden cargar los tramos sin las paradas. El mapa de paradas está vacío.");
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
				} else {
					LOGGER.warn("Tramo omitido: Parada {} o parada {} no encontrada en el sistema.", codigoInicio,
							codigoFin);
				}
			}
		} catch (IOException | NumberFormatException e) {
			LOGGER.error("Error al leer o procesar el archivo de tramos.", e);
			return Collections.emptyMap();
		}
		return tramos;
	}

	/**
	 * Private method responsible for retrieving the dependency (the map of stops)
	 * needed to build the {@code Tramo} objects.
	 * 
	 * @return the map loaded with stops.
	 */
	private Map<Integer, Parada> cargarParadas() {
		try {
			// Usa la Factory para obtener el ParadaDAO
			ParadaDAO paradaDAO = Factory.getInstancia("PARADA", ParadaDAO.class);
			return paradaDAO.buscarTodos();
		} catch (Exception e) {
			LOGGER.error("Error al obtener ParadaDAO desde la Factory en TramoDAO.", e);
			return Collections.emptyMap();
		}
	}

	/**
	 * Returns the configured path to the segment data file.
	 * 
	 * @return The file path string.
	 */
	public String getRutaArchivo() {
		return rutaArchivo;
	}
}