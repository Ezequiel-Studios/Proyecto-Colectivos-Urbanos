package colectivo.logica;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.dao.LineaDAO;
import colectivo.dao.ParadaDAO;
import colectivo.dao.TramoDAO;
import colectivo.modelo.Ciudad;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

/**
 * This service is responsible for orchestrating the loading of all core domain
 * entities (stops, lines, segments) from the persistance layer (DAOs) and
 * assembling them into the central {@code Ciudad} object.
 * 
 * @author Juliana Martin
 * @author Ezequiel Ramos
 * @author Nerea Toledo
 */
public class CiudadLoaderService {

	/** Logger instance for logging events, errors and exceptions. */
	private static final Logger LOGGER = LogManager.getLogger(CiudadLoaderService.class);

	/** The DAO contract for accessing stop data. */
	private final ParadaDAO paradaDAO;

	/** The DAO contract for accessing line data. */
	private final LineaDAO lineaDAO;

	/** The DAO contract for accessing segment data. */
	private final TramoDAO tramoDAO;

	/**
	 * Constructor that injects the required DAO dependencies.
	 * 
	 * @param paradaDAO The DAO contract for accessing stop data.
	 * @param lineaDAO  The DAO contract for accessing line data.
	 * @param tramoDAO  The DAO contract for accessing segment data.
	 */
	public CiudadLoaderService(ParadaDAO paradaDAO, LineaDAO lineaDAO, TramoDAO tramoDAO) {
		this.paradaDAO = paradaDAO;
		this.lineaDAO = lineaDAO;
		this.tramoDAO = tramoDAO;
	}

	/**
	 * Loads all necessary data and assembles the complete {@code Ciudad} domain
	 * model.
	 * 
	 * @return The {@code Ciudad} object containing all system data.
	 */
	public Ciudad cargarCiudad() {
		LOGGER.info("Iniciando carga de datos del sistema...");

		Map<Integer, Parada> paradas = paradaDAO.buscarTodos();
		Map<String, Linea> lineas = lineaDAO.buscarTodos();
		Map<String, Tramo> tramos = tramoDAO.buscarTodos();

		establecerRelaciones(paradas, lineas);

		LOGGER.info("Carga de datos completa. Paradas: {}, Líneas: {}, Tramos: {}", paradas.size(), lineas.size(),
				tramos.size());

		return new Ciudad(paradas, lineas, tramos);
	}

	/**
	 * Establishes bidirectional object relationships between stops and lines.
	 * 
	 * @param paradas The complete map of all stops in the system.
	 * @param lineas  The complete map of all lines
	 */
	private void establecerRelaciones(Map<Integer, Parada> paradas, Map<String, Linea> lineas) {
		LOGGER.debug("Estableciendo relaciones bidireccionales Parada <-> Linea...");

		for (Linea linea : lineas.values()) {
			List<Parada> paradasOriginales = new ArrayList<>(linea.getParadas());
			linea.getParadas().clear();

			for (Parada paradaRef : paradasOriginales) {
				Parada paradaCompleta = paradas.get(paradaRef.getCodigo());
				if (paradaCompleta != null) {
					linea.agregarParada(paradaCompleta);
				} else {
					LOGGER.warn("La parada con código {} definida en la línea {} no existe en el mapa de paradas.",
							paradaRef.getCodigo(), linea.getCodigo());
				}
			}
		}
	}
}