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

public class InterfazInicializador extends Application {

	private static Coordinador coordinador;
	private static InterfazInicializador instance;
	private Stage primaryStage;
	private static final Logger LOGGER = LogManager.getLogger(InterfazInicializador.class);

	public static void lanzar(Coordinador coord) {
		coordinador = coord;
		launch();
	}

	public static InterfazInicializador getInstance() {
		return instance;
	}

	public void refrescarInterfaz() {
		Platform.runLater(() -> {
			try {
				cargarInterfaz();
			} catch (Exception e) {
				LOGGER.error("Error al recargar la interfaz.", e);
			}
		});
	}

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
	 * 
	 * @param primaryStage the main window of the JavaFX application.
	 */
	@Override
	public void start(Stage stage) throws Exception {
		this.primaryStage = stage;
		coordinador.setInterfazInicializador(this);
		cargarInterfaz();

	}

	/**
	 * Este método es llamado automáticamente por JavaFX cuando se cierra la ventana
	 * principal (al apretar la 'X').
	 */
	@Override
	public void stop() throws Exception {
		LOGGER.info("Aplicación cerrándose. Apagando servicios...");

		if (coordinador != null) {
			coordinador.apagarServicioDeHilos();
		}

		super.stop();
	}
}