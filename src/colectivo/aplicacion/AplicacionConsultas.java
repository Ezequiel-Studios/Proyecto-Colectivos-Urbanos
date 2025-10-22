package colectivo.aplicacion;

import java.io.IOException;
import java.util.Map;
import java.util.ArrayList;

import colectivo.conexion.Factory;
import colectivo.dao.*;
import colectivo.interfaz.InterfazJavaFX;
import colectivo.logica.Calculo;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;
import javafx.application.Application;
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

			paradas = paradaDAO.buscarTodos(); // Guardamos las paradas
			Map<String, Tramo> tramos = tramoDAO.buscarTodos();
			Map<String, Linea> lineas = lineaDAO.buscarTodos(); // Cargamos líneas aquí también

			// Verificar si la carga falló (DAOs devuelven mapas vacíos o hubo errores)
			if (paradas.isEmpty() || tramos.isEmpty() || lineas.isEmpty()) {
				System.err.println(
						"Error crítico: Uno o más DAOs no pudieron cargar datos. Verifique los archivos .txt y config.properties.");
				throw new IOException("Fallo en la carga inicial de datos desde DAOs."); // Lanzar excepción para ir al
																							// catch
			}

			Calculo calculoLogic = new Calculo();

			miCoordinador.setCalculo(calculoLogic); // Darle el cerebro
			miCoordinador.setTramos(tramos); // Darle los tramos (Calculo los necesita)
			miCoordinador.setParadas(new ArrayList<>(paradas.values())); // Darle las paradas (la Vista las necesita)

		} catch (IOException e) {
			System.err.println("Error fatal al cargar los datos iniciales. La aplicación se cerrará.");
			e.printStackTrace();
			return;
		} catch (Exception e) {
			System.err.println("Error inesperado durante la carga de datos.");
			e.printStackTrace();
			return;
		}

		// --- 3. Cargar la Vista (Interfaz) ---
		// ¡Aquí está el cambio!
		// Creamos nuestra interfaz manual y le pasamos el coordinador y las paradas
		InterfazJavaFX vista = new InterfazJavaFX(miCoordinador, new ArrayList<>(paradas.values()));

		// Obtenemos el panel raíz (VBox) que la clase InterfazJavaFX construyó
		Parent root = vista.getRoot();

		// --- 4. Mostrar la Escena (Esto es estándar de JavaFX) ---
		primaryStage.setTitle("Sistema de Consultas de Colectivos");
		primaryStage.setScene(new Scene(root, 500, 700)); // Usamos el 'root' de nuestra interfaz manual
		primaryStage.show();

	}
}