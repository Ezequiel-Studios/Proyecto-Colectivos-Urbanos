package colectivo.controlador;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import colectivo.conexion.Factory;
import colectivo.dao.LineaDAO;
import colectivo.dao.ParadaDAO;
import colectivo.dao.TramoDAO;
import colectivo.interfaz.ControladorInterfaz;
import colectivo.interfaz.InterfazInicializador;
import colectivo.logica.Calculo;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.modelo.Tramo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Coordinador {

	private static Locale localeActual = Locale.of("es");
	private static final String BUNDLE_BASE_NAME = "colectivo.interfaz.messages";
	private InterfazInicializador interfazInicializador;
	private Calculo calculo;
	private ControladorInterfaz ControladorInterfaz;
	private List<Parada> paradas;
	private List<Linea> lineas;
	private List<Recorrido> recorridos;
	private Map<String, Tramo> tramos;
	private static final Logger LOGGER = LogManager.getLogger(Coordinador.class);

	public Coordinador() {
		this.calculo = new Calculo();

		try {
			ParadaDAO paradaDAO = (ParadaDAO) Factory.getInstancia("PARADA");
			TramoDAO tramoDAO = (TramoDAO) Factory.getInstancia("TRAMO");
			LineaDAO lineaDAO = (LineaDAO) Factory.getInstancia("LINEA");

			Map<Integer, Parada> paradasMap = paradaDAO.buscarTodos();
			this.tramos = tramoDAO.buscarTodos();
			Map<String, Linea> lineasMap = lineaDAO.buscarTodos();

			if (paradasMap.isEmpty() || tramos.isEmpty() || lineasMap.isEmpty()) {
				LOGGER.fatal("Error crítico: Falló la carga de datos. Una o más colecciones están vacías.");
				throw new IOException("Fallo en la carga inicial de datos desde DAOs.");
			}
			this.paradas = new ArrayList<>(paradasMap.values());
			this.lineas = new ArrayList<>(lineasMap.values());

			LOGGER.info("Paradas cargadas y convertidas a List<Parada>: {} elementos.", this.paradas.size());
		} catch (RuntimeException e) {
			LOGGER.fatal("Error crítico: Falló la inicialización del Modelo (DAO/Factory).", e);
		} catch (IOException e) {
			LOGGER.fatal("Error de infraestructura: Los datos críticos no se pudieron cargar o están vacíos.", e);
			// e.printStackTrace();
		}
	}

	public void setCalculo(Calculo calculo) {
		this.calculo = calculo;
	}

	public void setInterfaz(ControladorInterfaz ControladorInterfaz) {
		this.ControladorInterfaz = ControladorInterfaz;
	}

	public void setParadas(List<Parada> paradas) {
		this.paradas = paradas;
	}

	public void setLineas(List<Linea> lineas) {
		this.lineas = lineas;
	}

	public void setRecorridos(List<Recorrido> recorridos) {
		this.recorridos = recorridos;
	}

	public void setTramos(Map<String, Tramo> tramos) {
		this.tramos = tramos;
	}

	public void setInterfazInicializador(InterfazInicializador interfazInicializador) {
		this.interfazInicializador = interfazInicializador;
	}

	/**
	 * Calls the method responsible for calculating possible routes between two bus
	 * stops given the day and time.
	 * 
	 * @param origen    the origin bus stop
	 * @param destino   the destination bus stop
	 * @param diaSemana the day of the week
	 * @param hora      the departure time
	 * @return a list of all the possible routes that match the needs of the user,
	 *         represented as lists.
	 */
	public List<List<Recorrido>> calcularRecorrido(Parada origen, Parada destino, int diaSemana, LocalTime hora) {
		return calculo.calcularRecorrido(origen, destino, diaSemana, hora, tramos);
	}

	/***/
	public void cambiarIdioma(String codigoIdioma) {
		Locale nuevoLocale = Locale.of(codigoIdioma);

		if (nuevoLocale.equals(localeActual)) {
			LOGGER.info("El idioma {} ya fue seleccionado.", codigoIdioma);
			return;
		}
		localeActual = nuevoLocale;
		LOGGER.info("Cambiando locale a: {}. La interfaz se reiniciará.", codigoIdioma);
		if (interfazInicializador != null)
			interfazInicializador.refrescarInterfaz();
	}

	/**
	 * Returns the current locale.
	 * 
	 * @return the current locale.
	 */
	public static Locale getLocaleActual() {
		return localeActual;
	}

	/***/
	public ResourceBundle getResourceBundle() {
		return ResourceBundle.getBundle(BUNDLE_BASE_NAME, localeActual);
	}

	/**
	 * Returns the list of all bus lines loaded.
	 * 
	 * @return a list with all the lines.
	 */
	public List<Linea> getLineas() {
		return lineas;
	}

	/**
	 * Returns the list of all bus stops loaded.
	 * 
	 * @return a list with all the stops.
	 */
	public List<Parada> getParadas() {
		return paradas;
	}

	/**
	 * Returns the map with all route segments loaded.
	 * 
	 * @return a map with the route segments.
	 */
	public Map<String, Tramo> getTramos() {
		return tramos;
	}

	public void iniciarSistema() {
		InterfazInicializador.lanzar(this);
	}
}