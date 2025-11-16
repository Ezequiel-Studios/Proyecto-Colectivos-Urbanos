package colectivo.dao.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.conexion.BDConexion;
import colectivo.conexion.Factory;
import colectivo.dao.ParadaDAO;
import colectivo.dao.TramoDAO;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

/**
 * Concrete implementation of {@code TramoDAO} using a relational database. This
 * class implements the contract defined by {@code TramoDAO} to handle
 * persistence operations for {@code Tramo} objects.
 * 
 * @author Juliana Martin
 * @author Ezequiel Ramos
 * @author Nerea Toledo
 */
public class TramoDAODatabase implements TramoDAO {

	/** Map containing all available stops. */
	private final Map<Integer, Parada> paradasDisponibles;

	/** Logger instance for logging events, errors and exceptions. */
	private static final Logger LOGGER = LogManager.getLogger(TramoDAODatabase.class);

	/**
	 * Constructor that initializes dependencies. Calls {@code cargarParadas()} to
	 * obtain all available stops from the database through the {@code ParadaDAO}
	 */
	public TramoDAODatabase() {
		this.paradasDisponibles = cargarParadas();
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
	 * Retrieves all route segments from the database and maps them to {@code Tramo}
	 * objects.
	 * 
	 * @return A {@code Map} of all loaded {@code Tramo} objects, keyed by a
	 *         composite string identifier, or an empty map on failure.
	 */
	@Override
	public Map<String, Tramo> buscarTodos() {
		return cargarDesdeBD();
	}

	/**
	 * Reads segment data from the 'tramo' table. Converts the integer stop IDs
	 * ('codigoInicio', 'codigoFin') into their corresponding {@code Parada} objects
	 * using the cached {@code paradasDisponibles} map.
	 * 
	 * @return A map containing {@code Tramo} objects.
	 */
	private Map<String, Tramo> cargarDesdeBD() {
		Map<String, Tramo> tramos = new LinkedHashMap<>();
		String sql = "SELECT inicio, fin, tiempo, tipo FROM tramo";
		Connection conn = null;

		if (this.paradasDisponibles == null || this.paradasDisponibles.isEmpty()) {
			LOGGER.fatal("Error: No se pudieron cargar las paradas necesarias para leer los tramos.");
			return Collections.emptyMap();
		}

		try {
			conn = BDConexion.getConnection();

			try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
				while (rs.next()) {
					int codigoInicio = rs.getInt("inicio");
					int codigoFin = rs.getInt("fin");
					int tiempo = rs.getInt("tiempo");
					int tipo = rs.getInt("tipo");

					Parada paradaInicio = this.paradasDisponibles.get(codigoInicio);
					Parada paradaFin = this.paradasDisponibles.get(codigoFin);

					if (paradaInicio != null && paradaFin != null) {
						Tramo tramo = new Tramo(paradaInicio, paradaFin, tiempo, tipo);
						String clave = codigoInicio + "-" + codigoFin;
						tramos.put(clave, tramo);
					} else {
						LOGGER.warn(
								"Advertencia: Tramo con paradas huérfanas omitido: " + codigoInicio + "->" + codigoFin);
					}
				}
			}
		} catch (SQLException e) {
			LOGGER.fatal("Error al buscar todos los tramos en la BD: ", e);
			return Collections.emptyMap();
		}
		return tramos;
	}

	/**
	 * Loads all available stops by requesting the {@code ParadaDAO} implementation
	 * from the {@code Factory}.
	 * 
	 * @return A map of all stops keyed by their ID, or an empty map on fatal
	 *         failure.
	 */
	private Map<Integer, Parada> cargarParadas() {
		try {
			ParadaDAO paradaDAO = Factory.getInstancia("PARADA", ParadaDAO.class);
			return paradaDAO.buscarTodos();
		} catch (Exception e) {
			LOGGER.fatal("Error al obtener ParadaDAO desde la Factory en TramoDAO: ", e);
			return Collections.emptyMap();
		}
	}

}