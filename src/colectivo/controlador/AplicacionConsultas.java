package colectivo.controlador;

import java.io.IOException;
import java.util.Map;
import java.util.ArrayList;

import colectivo.conexion.Factory;
import colectivo.dao.*;
import colectivo.interfaz.ControladorInterfaz;
import colectivo.logica.Calculo;
import colectivo.modelo.Linea;
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

		// --- 2. Cargar los Datos (Lógica de negocio) ---
		Map<Integer, Parada> paradas = null;

		try {
			ParadaDAO paradaDAO = (ParadaDAO) Factory.getInstancia("PARADA");
			TramoDAO tramoDAO = (TramoDAO) Factory.getInstancia("TRAMO");
			LineaDAO lineaDAO = (LineaDAO) Factory.getInstancia("LINEA");

			paradas = paradaDAO.buscarTodos();
			Map<String, Tramo> tramos = tramoDAO.buscarTodos();
			Map<String, Linea> lineas = lineaDAO.buscarTodos();

			if (paradas.isEmpty() || tramos.isEmpty() || lineas.isEmpty()) {
				System.err.println("Error crítico: Falló la carga de datos desde DAOs.");
				throw new IOException("Fallo en la carga inicial de datos desde DAOs.");
			}

			Calculo calculoLogic = new Calculo();

			miCoordinador.setCalculo(calculoLogic);
			miCoordinador.setTramos(tramos);
			miCoordinador.setParadas(new ArrayList<>(paradas.values()));
			miCoordinador.setLineas(new ArrayList<>(lineas.values()));

		} catch (IOException e) {
			System.err.println("Error fatal al cargar los datos iniciales. La aplicación se cerrará.");
			e.printStackTrace();
			return;
		} catch (Exception e) {
			System.err.println("Error inesperado durante la carga de datos.");
			e.printStackTrace();
			return;
		}

		// --- 3. Cargar la Vista (FXML) ---
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/colectivo/interfaz/ControladorInterfaz.fxml"));
			Parent root = loader.load();

			// Iniciar el controlador con el coordinador y paradas
			ControladorInterfaz controller = loader.getController();
			controller.init(miCoordinador, new ArrayList<>(paradas.values()));

			Scene scene = new Scene(root, 700, 700);
			// Cargar CSS (por si el FXML no lo toma)
			scene.getStylesheets().add(getClass().getResource("/colectivo/interfaz/estilos.css").toExternalForm());

			primaryStage.setTitle("Sistema de Consultas de Colectivos");
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("No se pudo cargar el FXML de la interfaz.");
		}
	}

}