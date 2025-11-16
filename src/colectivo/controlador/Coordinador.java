package colectivo.controlador;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import colectivo.conexion.Factory;
import colectivo.dao.LineaDAO;
import colectivo.dao.ParadaDAO;
import colectivo.dao.TramoDAO;
import colectivo.interfaz.InterfazInicializador;
import colectivo.logica.Calculo;
import colectivo.logica.CiudadLoaderService;
import colectivo.logica.Recorrido;
import colectivo.modelo.Ciudad;
import colectivo.modelo.Parada;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main application coordinator. This class acts as the central coordinator for
 * the application, implementing the Model-View-Controller design pattern. It is
 * responsible for initializing the application's Model, delegating tasks and
 * managing and concerns like Internationalization (i18n).
 * 
 * @author Juliana Martin
 * @author Ezequiel Ramos
 * @author Nerea Toledo
 */
public class Coordinador {

	/**
	 * Current locale used for internationalization (i18n) throughout the
	 * application.
	 */
	private static Locale localeActual = Locale.of("es");

	/**
	 * Base name for the resource bundles containing localized messages for te UI.
	 */
	private static final String BUNDLE_BASE_NAME = "colectivo.interfaz.messages";

	/** Reference to the UI initializer, the JavaFX application launcher. */
	private InterfazInicializador interfazInicializador;

	/**
	 * Instance of the business logic processor for route and simulation
	 * calculations.
	 */
	private Calculo calculo;

	/**
	 * The core model object representing the entire city's network (stops, lines,
	 * segments)
	 */
	private Ciudad ciudad;

	/** Logger instance for logging events, errors and exceptions. */
	private static final Logger LOGGER = LogManager.getLogger(Coordinador.class);

	/**
	 * Constructor that initializes the Coordinador and loads application model.
	 * Loads the complete city structure and initializes the {@code Calculo} with
	 * this data.
	 */
	public Coordinador() {
		try {
			ParadaDAO paradaDAO = Factory.getInstancia("PARADA", ParadaDAO.class);
			TramoDAO tramoDAO = Factory.getInstancia("TRAMO", TramoDAO.class);
			LineaDAO lineaDAO = Factory.getInstancia("LINEA", LineaDAO.class);

			CiudadLoaderService loader = new CiudadLoaderService(paradaDAO, lineaDAO, tramoDAO);
			this.ciudad = loader.cargarCiudad();

			this.calculo = new Calculo(this.ciudad.getLineas());

		} catch (RuntimeException e) {
			LOGGER.fatal("Error crítico: Falló la inicialización del Modelo (DAO/Factory).", e);
		}
	}

	/**
	 * Sets the {@code Calculo} service instance.
	 * 
	 * @param calculo The calculation service to be used by the coordinator.
	 */
	public void setCalculo(Calculo calculo) {
		this.calculo = calculo;
	}

	/**
	 * Sets the reference to the object responsible for initializing the user
	 * interface.
	 * 
	 * @param interfazInicializador The UI initializer instance.
	 */
	public void setInterfazInicializador(InterfazInicializador interfazInicializador) {
		this.interfazInicializador = interfazInicializador;
	}

	/**
	 * Calls the method responsible for calculating possible routes between two bus
	 * stops given the day and time.
	 * 
	 * @param origen    The origin bus stop
	 * @param destino   The destination bus stop
	 * @param diaSemana The day of the week
	 * @param hora      The departure time
	 * @return a list of all the possible routes that match the needs of the user,
	 *         represented as lists of {@code Recorrido} segments.
	 */
	public List<List<Recorrido>> calcularRecorrido(Parada origen, Parada destino, int diaSemana, LocalTime hora) {
		return calculo.calcularRecorrido(origen, destino, diaSemana, hora, this.ciudad.getTramos());
	}

	/**
	 * Switches the application's current locale (language). If the new locale is
	 * different from the current one, it updates the static locale and triggers a
	 * refresh of the user interface.
	 * 
	 * @param codigoIdioma The two-letter ISO 639-1 code for the new language (e.g.,
	 *                     "en", "es").
	 */
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
	 * Returns the current application locale.
	 * 
	 * @return the current {@code Locale}.
	 */
	public static Locale getLocaleActual() {
		return localeActual;
	}

	/**
	 * Retrieves the resource bundle corresponding to the current application
	 * locale.
	 * 
	 * @return The {@code ResourceBundle} containing the messages for the current
	 *         locale.
	 */
	public ResourceBundle getResourceBundle() {
		return ResourceBundle.getBundle(BUNDLE_BASE_NAME, localeActual);
	}

	/**
	 * Returns the list of all bus stops loaded.
	 * 
	 * @return a list with all the stops.
	 */
	public List<Parada> getParadas() {
		return new ArrayList<>(this.ciudad.getParadas().values());
	}

	/**
	 * Returns the main {@code Ciudad} model object.
	 * 
	 * @return The main city data model.
	 */
	public Ciudad getCiudad() {
		return ciudad;
	}

	/**
	 * Initiates the graphical user interface (GUI) of the application. This method
	 * delegates the startup process to the {@code InterfazInicializador}.
	 */
	public void iniciarSistema() {
		InterfazInicializador.lanzar(this);
	}
}