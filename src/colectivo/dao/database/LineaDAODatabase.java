package colectivo.dao.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.conexion.BDConexion;
import colectivo.conexion.Factory;
import colectivo.dao.LineaDAO;
import colectivo.dao.ParadaDAO;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;

/**
 * Concrete implementation of {@code LineaDAO} using a relational database. This
 * class implements the contract defined by {@code LineaDAO} to handle
 * persistence operations for {@code Linea} objects.
 * 
 * @author Juliana Martin
 * @author Ezequiel Ramos
 * @author Nerea Toledo
 */
public class LineaDAODatabase implements LineaDAO {

	/**
	 * Map containing all available stops, loaded before lines to facilitate object
	 * mapping.
	 */
	private final Map<Integer, Parada> paradasDisponibles;

	/** Logger instance for logging events, errors and exceptions. */
	private static final Logger LOGGER = LogManager.getLogger(LineaDAODatabase.class);

	/**
	 * Constructor that initializes the necessary dependencies. Calls
	 * {@code cargarParadas()} to obtain all available stops from the database
	 * through the {@code ParadaDAO}.
	 */
	public LineaDAODatabase() {
		this.paradasDisponibles = cargarParadas();
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
	 * Retrieves all bus lines and their related entities (stops and frequencies) by
	 * loading them from the database.
	 * 
	 * @return A {@code Map} of all loaded {@code Linea} objects, or an empty map if
	 *         an error occurs or no data is found.
	 */
	@Override
	public Map<String, Linea> buscarTodos() {
		return cargarDesdeBD();
	}

	/**
	 * Reads data drom the database tables to fully populate the {@code Linea}
	 * objects.
	 * 
	 * @return A map containing {@code Linea} objects.
	 * @throws SQLException if a database access error occurs.
	 */
	private Map<String, Linea> cargarDesdeBD() {
		Map<String, Linea> lineas = new LinkedHashMap<>();
		Connection conn = null;

		if (this.paradasDisponibles == null || this.paradasDisponibles.isEmpty()) {
			LOGGER.fatal("Error: No se pudieron cargar las paradas necesarias para leer las líneas.");
			return Collections.emptyMap();
		}

		try {
			conn = BDConexion.getConnection();

			String sqlLineas = "SELECT codigo, nombre FROM linea";
			try (Statement stmt = conn.createStatement(); ResultSet rsLineas = stmt.executeQuery(sqlLineas)) {

				while (rsLineas.next()) {
					String codigo = rsLineas.getString("codigo");
					String nombre = rsLineas.getString("nombre");
					Linea linea = new Linea(codigo, nombre);
					lineas.put(codigo, linea);
				}
			}

			if (lineas.isEmpty()) {
				return Collections.emptyMap();
			}

			String sqlParadas = "SELECT linea, parada, secuencia FROM linea_parada ORDER BY linea, secuencia";
			try (Statement stmt = conn.createStatement(); ResultSet rsParadas = stmt.executeQuery(sqlParadas)) {

				while (rsParadas.next()) {
					String codigoLinea = rsParadas.getString("linea");
					int codigoParada = rsParadas.getInt("parada");

					Linea lineaExistente = lineas.get(codigoLinea);
					Parada parada = this.paradasDisponibles.get(codigoParada);

					if (lineaExistente != null && parada != null) {
						lineaExistente.agregarParada(parada);
					}
				}
			}

			String sqlFrecuencias = "SELECT linea, diasemana, hora FROM linea_frecuencia";
			try (Statement stmt = conn.createStatement(); ResultSet rsFrec = stmt.executeQuery(sqlFrecuencias)) {

				while (rsFrec.next()) {
					String codigoLinea = rsFrec.getString("linea");
					int diaSemana = rsFrec.getInt("diasemana");
					LocalTime hora = rsFrec.getTime("hora").toLocalTime();

					Linea lineaExistente = lineas.get(codigoLinea);
					if (lineaExistente != null) {
						lineaExistente.agregarFrecuencia(diaSemana, hora);
					}
				}
			}

		} catch (SQLException e) {
			LOGGER.fatal("Error al cargar datos de líneas desde la BD: ", e);
			return Collections.emptyMap();
		}

		return lineas;
	}

	/**
	 * Loads all available stops by requesting the {@code ParadaDAO} implementation
	 * from the {@code Factory}.
	 * 
	 * @return A map of all stops keyed by their ID, or an empty map on failure.
	 */
	private Map<Integer, Parada> cargarParadas() {
		try {
			ParadaDAO paradaDAO = Factory.getInstancia("PARADA", ParadaDAO.class);
			return paradaDAO.buscarTodos();
		} catch (Exception e) {
			LOGGER.error("Error al obtener ParadaDAO desde la Factory en LineaDAO: ", e);
			return Collections.emptyMap();
		}
	}
}