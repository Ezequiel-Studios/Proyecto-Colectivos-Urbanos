package colectivo.aplicacion; 

import java.io.IOException;
import java.util.Map;
import java.util.ArrayList; 

import colectivo.datos.CargarDatos;
import colectivo.datos.CargarParametros;
import colectivo.interfaz.InterfazJavaFX; 
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
        Map<Integer, Parada> paradas;
        try {
            CargarParametros.parametros();

            paradas = CargarDatos.cargarParadas(CargarParametros.getArchivoParada());
            Map<String, Linea> lineas = CargarDatos.cargarLineas(CargarParametros.getArchivoLinea(),
                    CargarParametros.getArchivoFrecuencia(), paradas);
            Map<String, Tramo> tramos = CargarDatos.cargarTramos(CargarParametros.getArchivoTramo(), paradas);

            // Guardar datos estáticos para que la clase Calculo los vea
             CargarDatos.setParadasCargadas(paradas);
             CargarDatos.setLineasCargadas(lineas);

            // Pasar los datos de tramos al Coordinador para el cálculo
             miCoordinador.setTramos(tramos);


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