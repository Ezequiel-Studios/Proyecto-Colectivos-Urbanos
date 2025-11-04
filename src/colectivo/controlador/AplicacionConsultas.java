package colectivo.controlador;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AplicacionConsultas {

	private static final Logger LOGGER = LogManager.getLogger(AplicacionConsultas.class);

	/**
	 * Launches the application.
	 */
	public static void main(String[] args) {
		LOGGER.info("Iniciando la aplicación de consultas de colectivos.");
		Coordinador coordinador = new Coordinador();
		coordinador.iniciarSistema();
	}
}