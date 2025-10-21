package colectivo.aplicacion; // Asegúrate de que el paquete sea el correcto

import java.io.IOException;
import java.util.Map; // Para cargar datos

import colectivo.datos.CargarDatos; // Importa tus clases de carga
import colectivo.datos.CargarParametros;
import colectivo.interfaz.javafx.ControladorVistaPrincipal; // Importa tu nuevo controlador
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
            CargarParametros.parametros(); // Carga nombres de archivos desde config.properties

            Map<Integer, Parada> paradas = CargarDatos.cargarParadas(CargarParametros.getArchivoParada());
            Map<String, Linea> lineas = CargarDatos.cargarLineas(CargarParametros.getArchivoLinea(),
                    CargarParametros.getArchivoFrecuencia(), paradas);
            Map<String, Tramo> tramos = CargarDatos.cargarTramos(CargarParametros.getArchivoTramo(), paradas);

            // Guardar los datos cargados en CargarDatos para que el Controlador los pueda acceder
            // Opcionalmente, pasarlos al Coordinador si prefieres
             CargarDatos.setParadasCargadas(paradas); // <- Necesitas este método en CargarDatos
             CargarDatos.setLineasCargadas(lineas); // <- Necesitas este método en CargarDatos

            // (Opcional) Pasa los datos cargados al Coordinador si los va a manejar él
             //miCoordinador.setParadas(new ArrayList<>(paradas.values()));
             //miCoordinador.setLineas(new ArrayList<>(lineas.values()));
             //miCoordinador.setTramos(tramos);


        } catch (IOException e) {
            System.err.println("Error fatal al cargar los datos iniciales. La aplicación se cerrará.");
            e.printStackTrace();
            // Aquí podrías mostrar una alerta JavaFX antes de salir
            return; // Salir si no se pueden cargar los datos
        } catch (Exception e) {
            System.err.println("Error inesperado durante la carga de datos.");
             e.printStackTrace();
            return;
        }


        // --- 3. Cargar la Vista desde FXML ---
        try {
            // Ruta RELATIVA al archivo FXML desde la carpeta 'src'
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/colectivo/interfaz/javafx/VistaPrincipal.fxml"));
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