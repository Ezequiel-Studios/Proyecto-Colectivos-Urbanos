package colectivo.interfaz;

import javafx.application.Application;
import javafx.stage.Stage;

public class VentanaPrincipal extends Application {
    @Override
    public void start(Stage stage) {
        stage.setTitle("Consultas de Colectivos Urbanos");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
