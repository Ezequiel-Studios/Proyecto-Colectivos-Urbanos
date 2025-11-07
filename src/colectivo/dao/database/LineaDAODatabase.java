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

public class LineaDAODatabase implements LineaDAO {

	private final Map<Integer, Parada> paradasDisponibles;
	private static final Logger LOGGER = LogManager.getLogger(LineaDAODatabase.class);

	public LineaDAODatabase() {
		this.paradasDisponibles = cargarParadas();
	}

	@Override
	public void insertar(Linea linea) {
	}

	@Override
	public void actualizar(Linea linea) {
	}

	@Override
	public void borrar(Linea linea) {
	}

	@Override
	public Map<String, Linea> buscarTodos() {
		return cargarDesdeBD();
	}

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

	private Map<Integer, Parada> cargarParadas() {
		try {
			ParadaDAO paradaDAO = (ParadaDAO) Factory.getInstancia("PARADA");
			return paradaDAO.buscarTodos();
		} catch (Exception e) {
			LOGGER.error("Error al obtener ParadaDAO desde la Factory en LineaDAO: ", e);
			return Collections.emptyMap();
		}
	}
}