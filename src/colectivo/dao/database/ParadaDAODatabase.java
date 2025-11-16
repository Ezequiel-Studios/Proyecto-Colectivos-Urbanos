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
import colectivo.dao.ParadaDAO;
import colectivo.modelo.Parada;

/**
 * Concrete implementation of {@code ParadaDAO} using a relational database.
 * This class implements the contract defined by {@code ParadaDAO} to handle
 * persistence operations for {@code Parada} objects.
 * 
 * @author Juliana Martin
 * @author Ezequiel Ramos
 * @author Nerea Toledo
 */
public class ParadaDAODatabase implements ParadaDAO {

	/** Logger instance for logging events, errors and exceptions. */
	private static final Logger LOGGER = LogManager.getLogger(ParadaDAODatabase.class);

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
	 * Retrieves all bus stops from the database and maps them to {@code Parada}
	 * objects. It reads basic stop data from the 'parada' table. The results are
	 * stored in a {@code LinkedHashMap} for order preservation.
	 * 
	 * @return A {@code Map} of all loaded {@code Parada} objects, keyed by their
	 *         ID, or an empty map if an error occurs or no data is found.
	 */
	@Override
	public Map<Integer, Parada> buscarTodos() {
		Map<Integer, Parada> paradas = new LinkedHashMap<>();
		String sql = "SELECT codigo, direccion, latitud, longitud FROM parada";
		Connection conn = null;

		try {
			conn = BDConexion.getConnection();

			try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
				while (rs.next()) {
					int codigo = rs.getInt("codigo");
					String direccion = rs.getString("direccion");
					double latitud = rs.getDouble("latitud");
					double longitud = rs.getDouble("longitud");
					Parada parada = new Parada(codigo, direccion, latitud, longitud);
					paradas.put(codigo, parada);
				}
			}
		} catch (SQLException e) {
			LOGGER.fatal("Error al buscar todas las paradas en la BD: ", e);
			return Collections.emptyMap();
		}
		return paradas;
	}
}