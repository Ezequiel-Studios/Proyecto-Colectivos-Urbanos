package colectivo.interfaz;

import java.time.LocalTime;
import java.util.List;

import colectivo.controlador.Coordinador;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import javafx.collections.FXCollections; // Importante
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent; // Importamos Parent
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

// --- YA NO ES "extends Application" ---
public class InterfazJavaFX {

	private Coordinador coordinador;

	private ComboBox<Parada> comboOrigen;
	private ComboBox<Parada> comboDestino;
	private ComboBox<Integer> comboDia;
	private TextField horaField;
	private TextArea resultadoArea;

	private VBox rootLayout; // --- AÑADIDO: Guardamos el panel raíz aquí

	/**
	 * Constructor que construye la interfaz. Reemplaza al método start().
	 * 
	 * @param coordinador        La instancia del coordinador.
	 * @param paradasDisponibles La lista de paradas cargadas por
	 *                           AplicacionConsultas.
	 */
	public InterfazJavaFX(Coordinador coordinador, List<Parada> paradasDisponibles) {
		this.coordinador = coordinador;
		// this.coordinador.setInterfaz(this); // Opcional, si el coordinador necesita
		// devolver algo

		// --- CREAR COMPONENTES ---
		comboOrigen = new ComboBox<>();
		comboDestino = new ComboBox<>();
		comboDia = new ComboBox<>();
		horaField = new TextField();
		resultadoArea = new TextArea();
		resultadoArea.setEditable(false);

		configurarEstilosComponentes();

		// --- POBLAR LOS COMBOBOX ---
		comboOrigen.setItems(FXCollections.observableArrayList(paradasDisponibles));
		comboDestino.setItems(FXCollections.observableArrayList(paradasDisponibles));

		for (int i = 1; i <= 7; i++)
			comboDia.getItems().add(i);
		comboDia.setValue(1); // Valor por defecto

		// --- CONFIGURAR INTERFAZ ---
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
		// ... (todos los estilos del botón quedan igual)
		calcularButton.setStyle("-fx-background-color: #3498db;" + "-fx-text-fill: white;" + "-fx-font-size: 16px;"
				+ "-fx-font-weight: bold;" + "-fx-padding: 12px;" + "-fx-background-radius: 8px;"
				+ "-fx-cursor: hand;");

		calcularButton.setOnMouseEntered(e -> calcularButton.setStyle("-fx-background-color: #2980b9;"
				+ "-fx-text-fill: white;" + "-fx-font-size: 16px;" + "-fx-font-weight: bold;" + "-fx-padding: 12px;"
				+ "-fx-background-radius: 8px;" + "-fx-cursor: hand;"));
		calcularButton.setOnMouseExited(e -> calcularButton.setStyle("-fx-background-color: #3498db;"
				+ "-fx-text-fill: white;" + "-fx-font-size: 16px;" + "-fx-font-weight: bold;" + "-fx-padding: 12px;"
				+ "-fx-background-radius: 8px;" + "-fx-cursor: hand;"));

		// --- CONSTRUIR EL LAYOUT ---
		// Usamos la variable de clase 'rootLayout'
		rootLayout = new VBox(15);
		rootLayout.setPadding(new Insets(25));
		rootLayout.setStyle("-fx-background-color: linear-gradient(to bottom, #ecf0f1, #bdc3c7);");
		rootLayout.setAlignment(Pos.TOP_CENTER);

		VBox formPanel = new VBox(12);
		formPanel.setPadding(new Insets(20));
		formPanel.setStyle("-fx-background-color: white;" + "-fx-background-radius: 10px;"
				+ "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");

		formPanel.getChildren().addAll(lblOrigen, comboOrigen, lblDestino, comboDestino, lblDia, comboDia, lblHora,
				horaField, calcularButton);

		VBox resultadosPanel = new VBox(10);
		resultadosPanel.setPadding(new Insets(20));
		resultadosPanel.setStyle("-fx-background-color: white;" + "-fx-background-radius: 10px;"
				+ "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");

		resultadoArea.setPrefRowCount(10);
		resultadoArea.setStyle("-fx-control-inner-background: #f8f9fa;" + "-fx-font-family: 'Consolas', 'Courier New';"
				+ "-fx-font-size: 12px;" + "-fx-border-color: #dcdde1;" + "-fx-border-width: 1px;"
				+ "-fx-border-radius: 5px;");

		resultadosPanel.getChildren().addAll(lblResultados, resultadoArea);

		rootLayout.getChildren().addAll(titulo, formPanel, resultadosPanel);

		// --- QUITAMOS TODA LA LÓGICA DE Scene y Stage ---
		// Scene scene = new Scene(layout, 500, 700);
		// stage.setScene(scene);
		// stage.setTitle("Sistema de Transporte Público");
		// stage.show();
	}

	/**
	 * --- AÑADIDO: Método para que AplicacionConsultas obtenga la UI ---
	 * 
	 * @return El panel VBox raíz con toda la interfaz.
	 */
	public Parent getRoot() {
		return rootLayout;
	}

	private void configurarEstilosComponentes() {
		// ... (Este método queda exactamente igual)
		String comboStyle = "-fx-background-color: white;" + "-fx-border-color: #bdc3c7;" + "-fx-border-width: 2px;"
				+ "-fx-border-radius: 5px;" + "-fx-background-radius: 5px;" + "-fx-font-size: 13px;"
				+ "-fx-padding: 8px;";

		comboOrigen.setStyle(comboStyle);
		comboDestino.setStyle(comboStyle);
		comboDia.setStyle(comboStyle);

		comboOrigen.setMaxWidth(Double.MAX_VALUE);
		comboDestino.setMaxWidth(Double.MAX_VALUE);
		comboDia.setMaxWidth(Double.MAX_VALUE);

		horaField.setStyle("-fx-background-color: white;" + "-fx-border-color: #bdc3c7;" + "-fx-border-width: 2px;"
				+ "-fx-border-radius: 5px;" + "-fx-background-radius: 5px;" + "-fx-font-size: 13px;"
				+ "-fx-padding: 8px;");
		horaField.setPromptText("HH:MM (Ejemplo: 14:30)");
	}

	private Label crearLabel(String texto) {
		// ... (Este método queda exactamente igual)
		Label label = new Label(texto);
		label.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
		label.setStyle("-fx-text-fill: #34495e;");
		return label;
	}

	public void calcularRecorrido() {
		// ... (Este método queda exactamente igual)
		try {
			Parada origen = comboOrigen.getValue();
			Parada destino = comboDestino.getValue();
			Integer dia = comboDia.getValue();

			if (origen == null || destino == null || dia == null || horaField.getText().isEmpty()) {
				resultadoArea.setText("⚠ Por favor complete todos los campos."); // Corregido "campos"
				resultadoArea.setStyle("-fx-control-inner-background: #fff3cd;" + "-fx-text-fill: #856404;"
						+ "-fx-font-family: 'Arial';" + "-fx-font-size: 13px;" + "-fx-border-color: #ffc107;"
						+ "-fx-border-width: 2px;" + "-fx-border-radius: 5px;");
				return;
			}

			if (origen.equals(destino)) {
				resultadoArea.setText("⚠ La parada de origen y destino no pueden ser la misma.");
				resultadoArea.setStyle("-fx-control-inner-background: #fff3cd;" + "-fx-text-fill: #856404;"
						+ "-fx-font-family: 'Arial';" + "-fx-font-size: 13px;" + "-fx-border-color: #ffc107;"
						+ "-fx-border-width: 2px;" + "-fx-border-radius: 5px;");
				return;
			}

			String partesHora[] = horaField.getText().split(":");
			LocalTime hora = LocalTime.of(Integer.parseInt(partesHora[0]), Integer.parseInt(partesHora[1]));

			List<List<Recorrido>> recorridos = coordinador.calcularRecorrido(origen, destino, dia, hora);
			mostrarResultados(recorridos);

		} catch (Exception e) {
			resultadoArea.setText("❌ Error: Verifique el formato de la hora (HH:MM)");
			resultadoArea.setStyle("-fx-control-inner-background: #f8d7da;" + "-fx-text-fill: #721c24;"
					+ "-fx-font-family: 'Arial';" + "-fx-font-size: 13px;" + "-fx-border-color: #dc3545;"
					+ "-fx-border-width: 2px;" + "-fx-border-radius: 5px;");
		}
	}

	// --- ¡¡MÉTODO MODIFICADO!! ---
	public void mostrarResultados(List<List<Recorrido>> listaRecorridos) {
		if (listaRecorridos == null || listaRecorridos.isEmpty()) {
			resultadoArea.setText("ℹ No hay recorridos disponibles para la búsqueda realizada.");
			resultadoArea.setStyle("-fx-control-inner-background: #d1ecf1;" + "-fx-text-fill: #0c5460;"
					+ "-fx-font-family: 'Arial';" + "-fx-font-size: 13px;" + "-fx-border-color: #17a2b8;"
					+ "-fx-border-width: 2px;" + "-fx-border-radius: 5px;");
			return;
		}

		resultadoArea.setStyle("-fx-control-inner-background: #f8f9fa;" + "-fx-font-family: 'Consolas', 'Courier New';"
				+ "-fx-font-size: 12px;" + "-fx-border-color: #28a745;" + "-fx-border-width: 2px;"
				+ "-fx-border-radius: 5px;");

		StringBuilder sb = new StringBuilder();
		sb.append("═══════════════════════════════════════════\n");
		sb.append("          RECORRIDOS DISPONIBLES\n");
		sb.append("═══════════════════════════════════════════\n\n");

		int contador = 1;
		for (List<Recorrido> r : listaRecorridos) {
			sb.append("🚏 Opción ").append(contador).append(":\n");
			sb.append("───────────────────────────────────────────\n");

			// --- BUCLE INTERNO MODIFICADO ---
			// En lugar de llamar a recorrido.toString(), formateamos la salida.
			for (Recorrido recorrido : r) {

				// 1. Mostrar la línea (si la hay)
				if (recorrido.getLinea() != null) {
					sb.append("  🚍 Línea: ").append(recorrido.getLinea().getNombre()).append(" (")
							.append(recorrido.getLinea().getCodigo()).append(")\n");
				} else {
					// En el futuro, esto podría ser un tramo caminando
					sb.append("  🚶 Tramo Caminando\n");
				}

				// 2. Mostrar parada de inicio y fin del tramo
				List<Parada> paradasDelRecorrido = recorrido.getParadas();
				if (!paradasDelRecorrido.isEmpty()) {
					sb.append("     Desde: ").append(paradasDelRecorrido.get(0).getDireccion()).append("\n");
					sb.append("     Hasta: ")
							.append(paradasDelRecorrido.get(paradasDelRecorrido.size() - 1).getDireccion())
							.append("\n");
				}

				// 3. Mostrar hora de salida
				sb.append("     Sale:  ").append(recorrido.getHoraSalida()).append("\n");

				// 4. Formatear la duración de segundos a "minutos" y "segundos"
				int duracionTotalSegundos = recorrido.getDuracion();
				int minutos = duracionTotalSegundos / 60;
				int segundos = duracionTotalSegundos % 60;
				sb.append("     Duración: ").append(minutos).append(" min ").append(segundos).append(" seg\n\n");
			}
			// sb.append("\n"); // El \n\n de arriba reemplaza este
			contador++;
		}
		resultadoArea.setText(sb.toString());
	}

	// --- QUITAMOS el método main() ---
	// public static void main(String[] args) {
	// Application.launch(args);
	//  }
}