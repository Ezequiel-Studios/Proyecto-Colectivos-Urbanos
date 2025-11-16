package colectivo.dao.secuencial;

import colectivo.dao.ParadaDAO;
import colectivo.modelo.Parada;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;

/**
 * Concrete implementation of {@code ParadaDAO} using sequential files. This
 * class implements the {@code ParadaDAO} contract, handling persistence
 * operations by reading and processing data from structured text files (as an
 * alternative to a relational database).
 * 
 * @author Juliana Martin
 * @author Ezequiel Ramos
 * @author Nerea Toledo
 */
public class ParadaDAOArchivo implements ParadaDAO {

	/** Path to the sequential file containing the bus stop data. */
	private String rutaArchivo;

	/**
	 * Cache map where loaded {@code Parada} objects are stored, keyed by stop ID.
	 */
	private Map<Integer, Parada> paradasMap;

	/**
	 * Flag indicating if the cache needs to be refreshed by reading the files
	 * again.
	 */
	private boolean actualizar;

	/** Logger instance for logging events, errors and exceptions. */
	private static final Logger LOGGER = LogManager.getLogger(ParadaDAOArchivo.class);

	/**
	 * Constructor that loads configuration properties, initizalizes stops and
	 * prepares the structure to store lines.
	 */
	public ParadaDAOArchivo() {
		Properties prop = new Properties();
		try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {

			if (input == null) {
				LOGGER.fatal("Error crítico: No se pudo encontrar 'config.properties' en la carpeta src.");
				throw new IOException("Archivo config.properties no encontrado en classpath.");
			}

			prop.load(input);

			this.rutaArchivo = prop.getProperty("parada");

			if (this.rutaArchivo == null) {
				LOGGER.fatal("Error crítico: La clave 'parada' no se encontró en config.properties.");
			}

		} catch (IOException ex) {
			LOGGER.fatal("Error crítico: No se pudo leer el archivo config.properties en ParadaDAO.", ex);
		}

		this.paradasMap = new LinkedHashMap<>();
		this.actualizar = true;
	}

	/** Method not implemented in the current version. */
	@Override
	public void insertar(Parada parada) {

	}

	/** Method not implemented in the current version. */
	@Override
	public void actualizar(Parada parada) {

	}

	/** Method not implemented in the current version. */
	@Override
	public void borrar(Parada parada) {

	}

	/**
	 * Returns all bus stops currently loaded. On the first call (when
	 * {@code actualizar} is true), it calls {@code leerDelArchivo} to read and
	 * process the data. Subsequent calls return the cached map ({@code paradasMap})
	 * directly.
	 * 
	 * @return a map containing all loaded stop objects.
	 */
	@Override
	public Map<Integer, Parada> buscarTodos() {
		if (actualizar) {
			this.paradasMap = leerDelArchivo(this.rutaArchivo);
			this.actualizar = false;
		}
		return this.paradasMap;
	}

	/**
	 * Private method that contains the logic required to read and process the file.
	 * Reads line by line, splits the data by the semicolon delimiter, and parses
	 * the components (ID, address, latitude, longitude). Creates a new
	 * {@code Parada} object and stores it in the result map.
	 * 
	 * @param ruta the route of the file to be read.
	 * @return a map loaded with the stops, or an empty map on failure.
	 */
	private Map<Integer, Parada> leerDelArchivo(String ruta) {
		LOGGER.info("Comenzando la lectura del archivo de paradas: {}", ruta);
		Map<Integer, Parada> paradas = new LinkedHashMap<>();
		try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
			String linea;
			while ((linea = br.readLine()) != null) {
				if (linea.trim().isEmpty()) {
					continue;
				}
				String[] partes = linea.split(";");
				int codigo = Integer.parseInt(partes[0].trim());
				String direccion = partes[1].trim();
				double latitud = Double.parseDouble(partes[2].trim());
				double longitud = Double.parseDouble(partes[3].trim());
				Parada parada = new Parada(codigo, direccion, latitud, longitud);
				paradas.put(codigo, parada);
			}
		} catch (IOException | NumberFormatException e) {
			LOGGER.error("Error al leer o procesar el archivo de paradas: {}", ruta, e);
			return Collections.emptyMap();
		}
		return paradas;
	}

	/**
	 * Returns the configured path to the stop data file.
	 * 
	 * @return The file path string.
	 */
	public String getRutaArchivo() {
		return rutaArchivo;
	}
}