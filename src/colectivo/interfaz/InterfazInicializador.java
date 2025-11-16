package colectivo.interfaz;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import colectivo.controlador.Coordinador;
import colectivo.modelo.Parada;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * JavaFX application initializer and lifecycle manager. This class extends
 * {@code javafx.application.Application}, serving as the official entry point
 * for the JavaFX GUI framework.
 * 
 * @author Juliana Martin
 * @author Ezequiel Ramos
 * @author Nerea Toledo
 */
public class InterfazInicializador extends Application {

	/** Static reference to the application's central controller. */
	private static Coordinador coordinador;

	/** Static reference to the running instance of the JavaFX Application class. */
	private static InterfazInicializador instance;

	/** The primary window container provided by the JavaFX runtime. */
	private Stage primaryStage;

	/** Logger instance for logging events, errors and exceptions. */
	private static final Logger LOGGER = LogManager.getLogger(InterfazInicializador.class);

	/**
	 * Static entry method to launch the JavaFX runtime. This method links the main
	 * application object graph (the {@code Coordinador}) to the JavaFX framework.
	 * 
	 * @param coord The initialized central coordinator instance.
	 */
	public static void lanzar(Coordinador coord) {
		coordinador = coord;
		launch();
	}

	/**
	 * Returns the single running instance of this class. Useful for allowing other
	 * parts of the application (e.g., the Coordinator) to access the JavaFX
	 * lifecycle methods.
	 * 
	 * @return The singleton instance of {@code InterfazInicializador}.
	 */
	public static InterfazInicializador getInstance() {
		return instance;
	}

	/**
	 * Safely reloads the entire user interface on the JavaFX Application Thread.
	 * Used primarily for dynamically applying language changes (i18n) by reloading
	 * the FXML with a new {@code ResourceBundle}.
	 */
	public void refrescarInterfaz() {
		Platform.runLater(() -> {
			try {
				cargarInterfaz();
			} catch (Exception e) {
				LOGGER.error("Error al recargar la interfaz.", e);
			}
		});
	}

	/**
	 * Core method for loading the FXML file and initializing the view.
	 * 
	 * @throws Exception If FXML loading or dependency injection fails.
	 */
	public void cargarInterfaz() throws Exception {
		try {
			Locale locale = Coordinador.getLocaleActual();
			ResourceBundle resources = ResourceBundle.getBundle("colectivo.interfaz.messages", locale);

			FXMLLoader loader = new FXMLLoader(getClass().getResource("/colectivo/interfaz/ControladorInterfaz.fxml"),
					resources);
			List<Parada> paradasDisponibles = coordinador.getParadas();
			Parent root = loader.load();

			Scene scene = primaryStage.getScene();
			if (scene == null) {
				scene = new Scene(root, 1200, 800);
				primaryStage.setScene(scene);
			} else {
				scene.setRoot(root);
			}

			scene.getStylesheets().add(getClass().getResource("/colectivo/interfaz/estilos.css").toExternalForm());

			ControladorInterfaz controller = loader.getController();
			controller.init(coordinador, paradasDisponibles, resources, primaryStage);

			primaryStage.setMaximized(true);
			primaryStage.show();

		} catch (Exception e) {
			LOGGER.error("No se pudo cargar el FXML de la interfaz. La aplicación no es funcional.", e);
		}
	}

	/**
	 * Starts the JavaFX application and initializes the necessary components.
	 * Called automatically after {@code launch()}. It sets up the primary stage and
	 * delegates control to the coordinator.
	 * 
	 * @param primaryStage The main window of the JavaFX application.
	 */
	@Override
	public void start(Stage stage) throws Exception {
		this.primaryStage = stage;
		coordinador.setInterfazInicializador(this);
		cargarInterfaz();

	}

	/**
	 * Called automatically by JavaFX when the main window is closed. Used to
	 * perform final cleanup tasks before the application exits.
	 */
	@Override
	public void stop() throws Exception {
		LOGGER.info("Aplicación cerrándose. Apagando servicios...");
		super.stop();
	}
}