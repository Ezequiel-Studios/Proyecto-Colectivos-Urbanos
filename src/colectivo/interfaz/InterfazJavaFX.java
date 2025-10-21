package colectivo.interfaz;

import java.time.LocalTime;
import java.util.List;

import colectivo.aplicacion.Coordinador;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class InterfazJavaFX extends Application{

	private Coordinador coordinador;
	
	private ComboBox<Parada> comboOrigen;
	private ComboBox<Parada> comboDestino;
	private ComboBox<Integer> comboDia;
	private TextField horaField;
	private TextArea resultadoArea;
	
	
	@Override
	public void start(Stage stage) throws Exception {
		coordinador = new Coordinador();
		coordinador.setInterfaz(this);
		
		comboOrigen = new ComboBox<>();
		comboDestino = new ComboBox<>();
		comboDia = new ComboBox<>();
		horaField = new TextField();
	    resultadoArea = new TextArea();
	    resultadoArea.setEditable(false);
	    
	    for(int i = 1; i <= 7; i++)
	    	comboDia.getItems().add(i);
	    
	    Button calcularButton = new Button("Calcular recorrido");
	    calcularButton.setOnAction(e -> calcularRecorrido());
	    
	    VBox layout = new VBox(10, new Label("Parada origen:"), comboOrigen,
	    							new Label("Parada destino:"), comboDestino,
	    							new Label("Dia de la semana (1-7):"), comboDia,
	    							new Label("Hora de llegada (HH:MM):"), horaField,
	    							calcularButton,
	    							new Label("Resultados:"), resultadoArea);
	    
	    layout.setPadding(new javafx.geometry.Insets(15));
	    Scene scene = new Scene(layout, 400, 500);
	    stage.setScene(scene);
	    stage.setTitle("Consulta de recorridos de colectivo");
	    stage.show();
	}
	
	public void calcularRecorrido() {
		Parada origen = comboOrigen.getValue();
		Parada destino = comboDestino.getValue();
		Integer dia = comboDia.getValue();
		String partesHora[] = horaField.getText().split(":");
		LocalTime hora = LocalTime.of(Integer.parseInt(partesHora[0]), Integer.parseInt(partesHora[1]));
		
		List<List<Recorrido>> recorridos = coordinador.calcularRecorrido(origen, destino, dia, hora);
		mostrarResultados(recorridos);
		
	}
	
	public void mostrarResultados(List<List<Recorrido>> listaRecorridos) {
		if(listaRecorridos.isEmpty()) {
			resultadoArea.setText("No hay recorridos disponibles.");
			return;
		}
		StringBuilder sb = new StringBuilder("---------RECORRIDOS DISPONIBLES---------\n");
		int contador = 1;
		for(List<Recorrido> r : listaRecorridos) {
			sb.append("Recorrido ").append(contador).append(":\n");
			for(Recorrido recorrido : r) 
				sb.append(" ").append(recorrido).append("\n");
			contador++;
		}
		resultadoArea.setText(sb.toString());
	}
	
	public static void main(String[] args) {
		Application.launch(args);
	}
	
	
}