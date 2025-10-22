package colectivo.interfaz;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import colectivo.controlador.Coordinador;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import javafx.collections.FXCollections; 
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent; 
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class InterfazJavaFX {

	private Coordinador coordinador;
	
	private ComboBox<Parada> comboOrigen;
	private ComboBox<Parada> comboDestino;
	private ComboBox<String> comboDia;
	private TextField horaField;
	private TextArea resultadoArea;
	private Map<String, Integer> diasMap;
	
	private VBox rootLayout;
	
	/**
	 * Constructor that builds the interface.
	 * Replaces the start() method.
	 * @param coordinador coordinador instance object
	 * @param paradasDisponibles the list of stops loaded by AplicacionConsultas.
	 */
	public InterfazJavaFX(Coordinador coordinador, List<Parada> paradasDisponibles) {
		this.coordinador = coordinador;
		
		comboOrigen = new ComboBox<>();
		comboDestino = new ComboBox<>();
		comboDia = new ComboBox<>();
		horaField = new TextField();
	    resultadoArea = new TextArea();
	    resultadoArea.setEditable(false);
	    
	    configurarEstilosComponentes();
	    
	    comboOrigen.setItems(FXCollections.observableArrayList(paradasDisponibles));
	    comboDestino.setItems(FXCollections.observableArrayList(paradasDisponibles));
	    
	    /*
	    for(int i = 1; i <= 7; i++)
	    	comboDia.getItems().add(i);
	    comboDia.setValue(1); 
	    */
	    
	    comboDia.getItems().addAll("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo");
	    
	    diasMap = new HashMap<>();
	    diasMap.put("Lunes", 1);
	    diasMap.put("Martes", 2);
	    diasMap.put("Miércoles", 3);
	    diasMap.put("Jueves", 4);
	    diasMap.put("Viernes", 5);
	    diasMap.put("Sábado", 6);
	    diasMap.put("Domingo", 7);
	    
	    Label titulo = new Label("Consulta de Recorridos");
	    titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
	    titulo.setStyle("-fx-text-fill: #2c3e50;");
	    
	    Label lblOrigen = crearLabel("Parada de Origen:");
	    Label lblDestino = crearLabel("Parada de Destino:");
	    Label lblDia = crearLabel("Día de la Semana:");
	    Label lblHora = crearLabel("Hora de Llegada:");
	    Label lblResultados = crearLabel("Resultados:");
	    
	    Button calcularButton = new Button("🔍 Calcular Recorrido");
	    calcularButton.setOnAction(e -> calcularRecorrido());
	    calcularButton.setMaxWidth(Double.MAX_VALUE);
	    
	    calcularButton.setStyle(
	        "-fx-background-color: #3498db;" +
	        "-fx-text-fill: white;" +
	        "-fx-font-size: 16px;" +
	        "-fx-font-weight: bold;" +
	        "-fx-padding: 12px;" +
	        "-fx-background-radius: 8px;" +
	        "-fx-cursor: hand;"
	    );
	    
	    calcularButton.setOnMouseEntered(e -> 
	        calcularButton.setStyle(
	            "-fx-background-color: #2980b9;" +
	            "-fx-text-fill: white;" +
	            "-fx-font-size: 16px;" +
	            "-fx-font-weight: bold;" +
	            "-fx-padding: 12px;" +
	            "-fx-background-radius: 8px;" +
	            "-fx-cursor: hand;"
	        )
	    );
	    calcularButton.setOnMouseExited(e -> 
	        calcularButton.setStyle(
	            "-fx-background-color: #3498db;" +
	            "-fx-text-fill: white;" +
	            "-fx-font-size: 16px;" +
	            "-fx-font-weight: bold;" +
	            "-fx-padding: 12px;" +
	            "-fx-background-radius: 8px;" +
	            "-fx-cursor: hand;"
	        )
	    );
	    
	    
	    rootLayout = new VBox(15);
	    rootLayout.setPadding(new Insets(25));
	    rootLayout.setStyle("-fx-background-color: linear-gradient(to bottom, #ecf0f1, #bdc3c7);");
	    rootLayout.setAlignment(Pos.TOP_CENTER);
	    
	    VBox formPanel = new VBox(12);
	    formPanel.setPadding(new Insets(20));
	    formPanel.setStyle(
	        "-fx-background-color: white;" +
	        "-fx-background-radius: 10px;" +
	        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);"
	    );
	    
	    formPanel.getChildren().addAll(
	        lblOrigen, comboOrigen,
	        lblDestino, comboDestino,
	        lblDia, comboDia,
	        lblHora, horaField,
	        calcularButton
	    );
	    
	    VBox resultadosPanel = new VBox(10);
	    resultadosPanel.setPadding(new Insets(20));
	    resultadosPanel.setStyle(
	        "-fx-background-color: white;" +
	        "-fx-background-radius: 10px;" +
	        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);"
	    );
	    
	    resultadoArea.setPrefRowCount(10);
	    resultadoArea.setStyle(
	        "-fx-control-inner-background: #f8f9fa;" +
	        "-fx-font-family: 'Consolas', 'Courier New';" +
	        "-fx-font-size: 12px;" +
	        "-fx-border-color: #dcdde1;" +
	        "-fx-border-width: 1px;" +
	        "-fx-border-radius: 5px;"
	    );
	    
	    resultadosPanel.getChildren().addAll(lblResultados, resultadoArea);
	    
	    rootLayout.getChildren().addAll(titulo, formPanel, resultadosPanel);
	 
	}
	
	/**
	 * Method that allows AplicacionConsultas to retrieve the main UI
	 * layout.
	 * @return the root VBox panel containing the entire interface.
	 */
	public Parent getRoot() {
		return rootLayout;
	}
	
	/**
	 * Private method that applies consistent styles to ComboBoxes and 
	 * TextFields used in the interface.
	 * */
	private void configurarEstilosComponentes() {
		String comboStyle = 
	        "-fx-background-color: white;" +
	        "-fx-border-color: #bdc3c7;" +
	        "-fx-border-width: 2px;" +
	        "-fx-border-radius: 5px;" +
	        "-fx-background-radius: 5px;" +
	        "-fx-font-size: 13px;" +
	        "-fx-padding: 8px;";
	    
	    comboOrigen.setStyle(comboStyle);
	    comboDestino.setStyle(comboStyle);
	    comboDia.setStyle(comboStyle);
	    
	    comboOrigen.setMaxWidth(Double.MAX_VALUE);
	    comboDestino.setMaxWidth(Double.MAX_VALUE);
	    comboDia.setMaxWidth(Double.MAX_VALUE);
	    
	    horaField.setStyle(
	        "-fx-background-color: white;" +
	        "-fx-border-color: #bdc3c7;" +
	        "-fx-border-width: 2px;" +
	        "-fx-border-radius: 5px;" +
	        "-fx-background-radius: 5px;" +
	        "-fx-font-size: 13px;" +
	        "-fx-padding: 8px;"
	    );
	    horaField.setPromptText("HH:MM (Ejemplo: 14:30)");
	}
	
	/**
	 * Private method that creates a styled Label for the interface.
	 * Sets font, weight, and text color.
	 * @param texto the text to display on the label.
	 * @return a styled Label instance.
	 * */
	private Label crearLabel(String texto) {
		Label label = new Label(texto);
	    label.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
	    label.setStyle("-fx-text-fill: #34495e;");
	    return label;
	}
	
	/**
	 * Calls the method responsible for calculating routes based
	 * on user input.
	 * */
	public void calcularRecorrido() {
		try {
			Parada origen = comboOrigen.getValue();
			Parada destino = comboDestino.getValue();
			String dia = comboDia.getValue();
			Integer numeroDia = diasMap.get(dia);
			
			if(origen == null || destino == null || dia == null || horaField.getText().isEmpty()) {
				resultadoArea.setText("⚠️ Por favor complete todos los campos."); 
				resultadoArea.setStyle(
					"-fx-control-inner-background: #fff3cd;" +
					"-fx-text-fill: #856404;" +
					"-fx-font-family: 'Arial';" +
					"-fx-font-size: 13px;" +
					"-fx-border-color: #ffc107;" +
					"-fx-border-width: 2px;" +
					"-fx-border-radius: 5px;"
				);
				return;
			}
			
            if (origen.equals(destino)) {
                resultadoArea.setText("⚠️ La parada de origen y destino no pueden ser la misma.");
                resultadoArea.setStyle(
					"-fx-control-inner-background: #fff3cd;" +
					"-fx-text-fill: #856404;" +
					"-fx-font-family: 'Arial';" +
					"-fx-font-size: 13px;" +
					"-fx-border-color: #ffc107;" +
					"-fx-border-width: 2px;" +
					"-fx-border-radius: 5px;"
				);
                return;
            }
			
			String partesHora[] = horaField.getText().split(":");
			LocalTime hora = LocalTime.of(Integer.parseInt(partesHora[0]), Integer.parseInt(partesHora[1]));
			
			List<List<Recorrido>> recorridos = coordinador.calcularRecorrido(origen, destino, numeroDia, hora);
			mostrarResultados(recorridos);
			
		} catch (Exception e) {
			resultadoArea.setText("❌ Error: Verifique el formato de la hora (HH:MM)");
			resultadoArea.setStyle(
				"-fx-control-inner-background: #f8d7da;" +
				"-fx-text-fill: #721c24;" +
				"-fx-font-family: 'Arial';" +
				"-fx-font-size: 13px;" +
				"-fx-border-color: #dc3545;" +
				"-fx-border-width: 2px;" +
				"-fx-border-radius: 5px;"
			);
		}
	}
	
	/**
	 * Displays the calculated results in the textArea.
	 * @param listaRecorridos the list of possible routes, each route 
	 * represented as a list of Recorrido objects.
	 * */
	public void mostrarResultados(List<List<Recorrido>> listaRecorridos) {
		if(listaRecorridos == null || listaRecorridos.isEmpty()) {
			resultadoArea.setText("ℹ️ No hay recorridos disponibles para la búsqueda realizada.");
			resultadoArea.setStyle(
				"-fx-control-inner-background: #d1ecf1;" +
				"-fx-text-fill: #0c5460;" +
				"-fx-font-family: 'Arial';" +
				"-fx-font-size: 13px;" +
				"-fx-border-color: #17a2b8;" +
				"-fx-border-width: 2px;" +
				"-fx-border-radius: 5px;"
			);
			return;
		}
		
		resultadoArea.setStyle(
			"-fx-control-inner-background: #f8f9fa;" +
			"-fx-font-family: 'Consolas', 'Courier New';" +
			"-fx-font-size: 12px;" +
			"-fx-border-color: #28a745;" +
			"-fx-border-width: 2px;" +
			"-fx-border-radius: 5px;"
		);
		
		StringBuilder sb = new StringBuilder();
		sb.append("═══════════════════════════════════════════\n");
		sb.append("          RECORRIDOS DISPONIBLES\n");
		sb.append("═══════════════════════════════════════════\n\n");
		
		int contador = 1;
		for(List<Recorrido> r : listaRecorridos) {
			sb.append("🚏 Opción ").append(contador).append(":\n");
			sb.append("───────────────────────────────────────────\n");
			
			for(Recorrido recorrido : r) {
				
				if (recorrido.getLinea() != null) {
					sb.append("  🚍 Línea: ").append(recorrido.getLinea().getNombre())
					  .append(" (").append(recorrido.getLinea().getCodigo()).append(")\n");
				} else {
					sb.append("  🚶 Tramo Caminando\n"); 
				}

				List<Parada> paradasDelRecorrido = recorrido.getParadas();
				if (!paradasDelRecorrido.isEmpty()) {
					sb.append("     Desde: ").append(paradasDelRecorrido.get(0).getDireccion()).append("\n");
					sb.append("     Hasta: ").append(paradasDelRecorrido.get(paradasDelRecorrido.size() - 1).getDireccion()).append("\n");
				}

				sb.append("     Sale:  ").append(recorrido.getHoraSalida()).append("\n");
				
				int duracionTotalSegundos = recorrido.getDuracion();
				int minutos = duracionTotalSegundos / 60;
				int segundos = duracionTotalSegundos % 60;
				sb.append("     Duración: ").append(minutos).append(" min ").append(segundos).append(" seg\n\n");
			}
			contador++;
		}
		resultadoArea.setText(sb.toString());
	}
	
}