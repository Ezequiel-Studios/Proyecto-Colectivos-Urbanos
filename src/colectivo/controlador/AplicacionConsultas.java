package colectivo.controlador;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main application entry point. This class serves as the initial entry point
 * for launching the application. It is responsible for initializing the
 * necessary components and starting the main control flow, specifically through
 * the {@code Coordinador}.
 * 
 * @author Juliana Martin
 * @author Ezequiel Ramos
 * @author Nerea Toledo
 */
public class AplicacionConsultas {

	/** Logger instance for logging events, errors and exceptions. */
	private static final Logger LOGGER = LogManager.getLogger(AplicacionConsultas.class);

	/**
	 * Launches the application and delegates control. 1. Logs the application start
	 * event. 2. Creates the main controller {@code Coordinador}. 3. Calls
	 * {@code iniciarSistema()} on the coordinator to begin the application's
	 * execution logic.
	 */
	public static void main(String[] args) {
		LOGGER.info("Iniciando la aplicación de consultas de colectivos.");
		Coordinador coordinador = new Coordinador();
		coordinador.iniciarSistema();
	}
}