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

public class ParadaDAODatabase implements ParadaDAO {
	
	private static final Logger LOGGER = LogManager.getLogger(ParadaDAODatabase.class);

	@Override
	public void insertar(Parada parada) {
	}

	@Override
	public void actualizar(Parada parada) {
	}

	@Override
	public void borrar(Parada parada) {
	}

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
