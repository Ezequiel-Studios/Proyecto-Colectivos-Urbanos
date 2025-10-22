package colectivo.aplicacion; // Asegúrate de que el paquete sea el correcto

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map; // Para cargar datos

import colectivo.conexion.Factory;
import colectivo.dao.*;
import colectivo.interfaz.javafx.ControladorVistaPrincipal; // Importa tu nuevo controlador
import colectivo.logica.Calculo;
import colectivo.modelo.Linea; // Importa tus modelos
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AplicacionConsultas extends Application {

	private Coordinador miCoordinador;

	public static void main(String[] args) {
		launch(args);
	}

	@Override	
	
	public void start(Stage primaryStage) {

		// --- 1. Crear el Coordinador ---
		miCoordinador = new Coordinador();

		// --- 2. Cargar los Datos (¡IMPORTANTE!) ---
		// Necesitamos cargar los datos ANTES de inicializar la interfaz
		// para que el controlador pueda llenar los ComboBoxes.
		try {
			// Obtenemos los DAOs usando la Factory
			ParadaDAO paradaDAO = (ParadaDAO) Factory.getInstancia("PARADA");
			TramoDAO tramoDAO = (TramoDAO) Factory.getInstancia("TRAMO");
			LineaDAO lineaDAO = (LineaDAO) Factory.getInstancia("LINEA");

			// Usamos los DAOs para obtener los datos (activando sus cachés)
			Map<Integer, Parada> paradas = paradaDAO.buscarTodos();
			Map<String, Tramo> tramos = tramoDAO.buscarTodos();
			Map<String, Linea> lineas = lineaDAO.buscarTodos();

			Calculo calculoLogic = new Calculo();

			miCoordinador.setCalculo(calculoLogic); // <-- Le pasamos la instancia vacía
			miCoordinador.setTramos(tramos); // Calculo necesita los tramos a través del Coordinador
			miCoordinador.setParadas(new ArrayList<>(paradas.values())); // Darle las paradas (la Vista las necesita
																			// para los ComboBox)
		} catch (Exception e) {
			System.err.println("Error fatal al cargar los datos iniciales. La aplicación se cerrará.");
			e.printStackTrace();
			// Mostrar alerta JavaFX si es posible
			javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
					javafx.scene.control.Alert.AlertType.ERROR);
			alert.setTitle("Error Crítico");
			alert.setHeaderText("No se pudieron cargar los datos necesarios.");
			alert.setContentText("La aplicación no puede continuar.\nDetalle: " + e.getMessage());
			alert.showAndWait();
			javafx.application.Platform.exit(); // Cierra la app
			return; // Salir del método start()
		}

		// --- 3. Cargar la Vista desde FXML ---
		try {
			// Ruta RELATIVA al archivo FXML desde la carpeta 'src'
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/colectivo/interfaz/javafx/VistaPrincipal.fxml"));
			Parent root = loader.load();

			// --- 4. Conectar Vista con Coordinador ---
			// Obtener la instancia del controlador que FXMLLoader creó
			ControladorVistaPrincipal controladorVista = loader.getController();

			// Pasar la instancia del Coordinador al controlador de la vista
			controladorVista.setCoordinador(miCoordinador); // Usa el método que creamos

			// --- 5. Mostrar la Escena ---
			primaryStage.setTitle("Sistema de Consultas de Colectivos");
			primaryStage.setScene(new Scene(root)); // Usar el 'root' cargado del FXML
			primaryStage.show();

		} catch (IOException e) {
			System.err.println("Error al cargar el archivo FXML.");
			e.printStackTrace();
			// Manejar error de carga de FXML (mostrar alerta, etc.)
		} catch (Exception e) {
			System.err.println("Error inesperado al iniciar la interfaz.");
			e.printStackTrace();
		}
	}
}