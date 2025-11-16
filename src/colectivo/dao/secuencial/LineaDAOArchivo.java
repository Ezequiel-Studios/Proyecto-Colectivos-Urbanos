package colectivo.dao.secuencial;

import colectivo.conexion.Factory;
import colectivo.dao.LineaDAO;
import colectivo.dao.ParadaDAO;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;

import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalTime;
import java.util.Collections;
import java.util.LinkedHashMap;

/**
 * Concrete implementation of {@code LineaDAO} using sequential files. This
 * class implements the {@code LineaDAO} contract, handling persistence
 * operations by reading and processing data from structured text files (as an
 * alternative to a relational database).
 * 
 * @author Juliana Martin
 * @author Ezequiel Ramos
 * @author Nerea Toledo
 */
public class LineaDAOArchivo implements LineaDAO {

	/** Path to the main file containing line codes, names, and stop sequences. */
	private String rutaArchivo;

	/** Path to the file containing line frequencies/schedules. */
	private String rutaArchivoFrecuencias;

	/**
	 * Map containing all available stops, obtained via {@code ParadaDAO} to resolve
	 * stop IDs.
	 */
	private final Map<Integer, Parada> paradasDisponibles;

	/**
	 * Cache map where loaded {@code Linea} objects are stored after file
	 * processing.
	 */
	private Map<String, Linea> lineasMap;

	/**
	 * Flag indicating if the cache needs to be refreshed by reading the files
	 * again.
	 */
	private boolean actualizar;

	/** Logger instance for logging events, errors and exceptions. */
	private static final Logger LOGGER = LogManager.getLogger(LineaDAOArchivo.class);

	/**
	 * Constructor that loads configuration properties, initizalizes stops and
	 * prepares the structure to store lines.
	 */
	public LineaDAOArchivo() {
		LOGGER.info("Iniciando LineaDAOArchivo: carga de configuración.");
		Properties prop = new Properties();
		try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {

			if (input == null) {
				LOGGER.fatal(
						"Error crítico: No se pudo encontrar 'config.properties' en la carpeta src (desde LineaDAO).");
				throw new IOException("Archivo config.properties no encontrado en classpath.");
			}

			prop.load(input);

			this.rutaArchivo = prop.getProperty("linea");
			this.rutaArchivoFrecuencias = prop.getProperty("frecuencia");

			if (this.rutaArchivo == null || this.rutaArchivoFrecuencias == null) {
				LOGGER.fatal("Error crítico: Claves 'linea' o 'frecuencia' no encontradas en config.properties.");
			}
		} catch (IOException ex) {
			LOGGER.fatal("Error crítico: No se pudo leer config.properties en LineaDAO.", ex);
		}

		this.paradasDisponibles = cargarParadas();
		this.lineasMap = new LinkedHashMap<>();
		this.actualizar = true;
	}

	/** Method not implemented in the current version. */
	@Override
	public void insertar(Linea linea) {

	}

	/** Method not implemented in the current version. */
	@Override
	public void actualizar(Linea linea) {

	}

	/** Method not implemented in the current version. */
	@Override
	public void borrar(Linea linea) {

	}

	/**
	 * Returns all bus lines currently loaded. If the data needs to refreshed, reads
	 * them from the file first.
	 * 
	 * @return a map containing all loaded {@code Linea} objects.
	 */
	@Override
	public Map<String, Linea> buscarTodos() {
		if (this.rutaArchivo == null || this.rutaArchivoFrecuencias == null) {
			LOGGER.warn("Error: No se puede buscar líneas porque las rutas de los archivos son nulas.");
			return Collections.emptyMap();
		}
		if (actualizar) {
			this.lineasMap = leerDelArchivo();
			this.actualizar = false;
			LOGGER.info("Carga de líneas finalizada con éxito. Líneas cargadas: {}", this.lineasMap.size());
		}
		return this.lineasMap;
	}

	/**
	 * Private method responsible for reading the lines, stops and frequencies from
	 * their files.
	 * 
	 * @return a map containing all {@code Linea} objects with their associated
	 *         stops and frequencies.
	 */
	private Map<String, Linea> leerDelArchivo() {
		Map<String, Linea> lineas = new LinkedHashMap<>();

		if (this.paradasDisponibles == null || this.paradasDisponibles.isEmpty()) {
			LOGGER.error("Error: No se pudieron cargar las paradas necesarias para leer las líneas.");
			return Collections.emptyMap();
		}

		try (BufferedReader br = new BufferedReader(new FileReader(this.rutaArchivo))) {
			String lineaTexto;
			while ((lineaTexto = br.readLine()) != null) {
				if (lineaTexto.trim().isEmpty())
					continue;

				String[] partes = lineaTexto.split(";");
				String codigo = partes[0].trim();
				String nombre = partes[1].trim();
				Linea linea = new Linea(codigo, nombre);

				for (int i = 2; i < partes.length; i++) {
					int codigoParada = Integer.parseInt(partes[i].trim());
					Parada parada = this.paradasDisponibles.get(codigoParada);
					if (parada != null) {
						linea.agregarParada(parada);
					}
				}
				lineas.put(codigo, linea);
			}
		} catch (IOException | NumberFormatException e) {
			LOGGER.error("Error al leer o procesar el archivo de líneas: {}.", this.rutaArchivo, e);
		}

		try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivoFrecuencias))) {
			String lineaTexto;
			while ((lineaTexto = br.readLine()) != null) {
				if (lineaTexto.trim().isEmpty())
					continue;

				String[] partes = lineaTexto.split(";");
				String codigoLinea = partes[0].trim();
				int diaSemana = Integer.parseInt(partes[1].trim());
				LocalTime hora = LocalTime.parse(partes[2].trim());

				Linea lineaExistente = lineas.get(codigoLinea);
				if (lineaExistente != null) {
					lineaExistente.agregarFrecuencia(diaSemana, hora);
				}
			}
		} catch (IOException | RuntimeException e) {
			LOGGER.error("Error al leer o procesar el archivo de frecuencias: {}.", this.rutaArchivoFrecuencias, e);
		}

		return lineas;
	}

	/**
	 * Private method that loads the map of bus stops by requesting the
	 * {@code ParadaDAO} implementation from the {@code Factory}.
	 * 
	 * @return the loaded map of stops.
	 */
	private Map<Integer, Parada> cargarParadas() {
		try {
			ParadaDAO paradaDAO = (ParadaDAO) Factory.getInstancia("PARADA", ParadaDAO.class);
			return paradaDAO.buscarTodos();
		} catch (Exception e) {
			LOGGER.error("Error al obtener ParadaDAO desde la Factory en LineaDAO.", e);
			return Collections.emptyMap();
		}
	}

	/**
	 * Returns the configured path to the main line data file.
	 * 
	 * @return The file path string.
	 */
	public String getRutaArchivo() {
		return rutaArchivo;
	}
}