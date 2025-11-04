package colectivo.dao.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import colectivo.conexion.BDConexion;
import colectivo.conexion.Factory;
import colectivo.dao.ParadaDAO;
import colectivo.dao.TramoDAO;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

public class TramoDAODatabase implements TramoDAO {
	private final Map<Integer, Parada> paradasDisponibles;

	public TramoDAODatabase() {
		this.paradasDisponibles = cargarParadas();
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

	@Override
	public Map<String, Tramo> buscarTodos() {
		return cargarDesdeBD();
	}

	private Map<String, Tramo> cargarDesdeBD() {
		Map<String, Tramo> tramos = new LinkedHashMap<>();
		String sql = "SELECT inicio, fin, tiempo, tipo FROM tramo";
		Connection conn = null;

		if (this.paradasDisponibles == null || this.paradasDisponibles.isEmpty()) {
			System.err.println("Error: No se pudieron cargar las paradas necesarias para leer los tramos.");
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
						System.err.println(
								"Advertencia: Tramo con paradas huérfanas omitido: " + codigoInicio + "->" + codigoFin);
					}
				}
			}
		} catch (SQLException e) {
			System.err.println("Error al buscar todos los tramos en la BD:");
			e.printStackTrace();
			return Collections.emptyMap();
		}
		return tramos;
	}

	private Map<Integer, Parada> cargarParadas() {
		try {
			ParadaDAO paradaDAO = (ParadaDAO) Factory.getInstancia("PARADA");
			return paradaDAO.buscarTodos();
		} catch (Exception e) {
			System.err.println("Error al obtener ParadaDAO desde la Factory en TramoDAO:");
			e.printStackTrace();
			return Collections.emptyMap();
		}
	}

}
