package colectivo.interfaz.javafx;

import java.net.URL; // Necesario para initialize
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle; // Necesario para initialize

import colectivo.aplicacion.Coordinador; // Importa tu clase Coordinador
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import javafx.collections.FXCollections; // Para llenar los ComboBox
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable; // Interfaz para el método initialize
import javafx.scene.control.Alert; // Para mostrar errores
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Controlador para la interfaz gráfica definida en VistaPrincipal.fxml. Maneja
 * la interacción del usuario y la lógica de la vista.
 */
public class ControladorVistaPrincipal implements Initializable { // Implementa Initializable

	// --- Componentes de la Interfaz (inyectados desde FXML) ---
	// Los nombres DEBEN coincidir con los fx:id del archivo FXML
	@FXML
	private ComboBox<Parada> comboOrigen;

	@FXML
	private ComboBox<Parada> comboDestino;

	@FXML
	private ComboBox<Integer> comboDia;

	@FXML
	private TextField horaField;

	@FXML
	private Button calcularButton; // Aunque no lo uses directamente, es bueno tenerlo

	@FXML
	private TextArea resultadoArea;

	// --- Lógica de la aplicación ---
	private Coordinador coordinador; // Referencia al coordinador principal

	/**
	 * Método llamado automáticamente por JavaFX después de que se carga el FXML.
	 * Ideal para inicializar los componentes (ej. llenar ComboBoxes).
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Llenar ComboBox de días
		for (int i = 1; i <= 7; i++) {
			comboDia.getItems().add(i);
		}
		comboDia.setValue(1); // Poner Lunes por defecto

		// La inicialización de paradas se mueve a setCoordinador.
		// Aquí solo inicializamos lo que no depende del coordinador.
	}

	/**
	 * Método que se ejecuta cuando se hace clic en el botón "Calcular recorrido".
	 * Corresponde al onAction="#calcularRecorrido" en el FXML.
	 *
	 * @param event El evento de acción (clic del botón).
	 */
	@FXML
	void calcularRecorrido(ActionEvent event) {
		Parada origen = comboOrigen.getValue();
		Parada destino = comboDestino.getValue();
		Integer dia = comboDia.getValue();
		String horaTexto = horaField.getText();

		// Validaciones básicas
		if (origen == null || destino == null || dia == null || horaTexto.isEmpty()) {
			mostrarAlerta("Error", "Por favor, complete todos los campos.");
			return;
		}
		if (origen.equals(destino)) {
			mostrarAlerta("Error", "La parada de origen y destino no pueden ser la misma.");
			return;
		}

		LocalTime hora;
		try {
			// Intentar parsear la hora en formato HH:MM
			String[] partesHora = horaTexto.split(":");
			if (partesHora.length != 2) {
				throw new DateTimeParseException("Formato de hora inválido", horaTexto, 0);
			}
			hora = LocalTime.of(Integer.parseInt(partesHora[0]), Integer.parseInt(partesHora[1]));
		} catch (DateTimeParseException | NumberFormatException e) {
			mostrarAlerta("Error", "Formato de hora inválido. Use HH:MM (ej. 10:35).");
			return;
		}

		// Llamar al coordinador para obtener los resultados
		if (coordinador != null) {
			try {
				List<List<Recorrido>> recorridos = coordinador.calcularRecorrido(origen, destino, dia, hora);
				mostrarResultados(recorridos);
			} catch (Exception e) {
				mostrarAlerta("Error", "Ocurrió un error al calcular el recorrido: " + e.getMessage());
				e.printStackTrace(); // Imprimir el stack trace para debugging
			}
		} else {
			mostrarAlerta("Error", "Error interno: El coordinador no está inicializado.");
		}
	}

	/**
	 * Muestra los resultados calculados en el TextArea. (Similar al método que
	 * tenías en InterfazJavaFX)
	 *
	 * @param listaRecorridos La lista de posibles recorridos.
	 */
	public void mostrarResultados(List<List<Recorrido>> listaRecorridos) {
		if (listaRecorridos == null || listaRecorridos.isEmpty()) {
			resultadoArea.setText("No hay recorridos disponibles.");
			return;
		}
		StringBuilder sb = new StringBuilder("---------RECORRIDOS DISPONIBLES---------\n\n");
		int contador = 1;
		for (List<Recorrido> r : listaRecorridos) {
			sb.append("--- Recorrido ").append(contador).append(" ---\n");
			for (Recorrido recorrido : r) {
				// Formato un poco más legible
				sb.append("  Línea: ")
						.append(recorrido.getLinea() != null ? recorrido.getLinea().getCodigo() : "Caminando")
						.append("\n");
				sb.append("  Desde: ").append(recorrido.getParadas().get(0).getDireccion()).append("\n");
				sb.append("  Hasta: ")
						.append(recorrido.getParadas().get(recorrido.getParadas().size() - 1).getDireccion())
						.append("\n");
				sb.append("  Sale a las: ").append(recorrido.getHoraSalida()).append("\n");
				sb.append("  Duración: ").append(recorrido.getDuracion()).append(" segundos\n\n"); // Asumiendo que
																									// duración está en
																									// segundos
			}
			contador++;
		}
		resultadoArea.setText(sb.toString());
	}

	/**
	 * Método para inyectar la dependencia del Coordinador. Deberás llamarlo desde
	 * tu clase principal (AplicacionConsultas) después de cargar el FXML.
	 *
	 * @param coordinador La instancia del Coordinador.
	 */
	public void setCoordinador(Coordinador coordinador) {
		this.coordinador = coordinador;

		// Llenar ComboBoxes de Paradas
		if (this.coordinador != null) {
			List<Parada> paradasDisponibles = this.coordinador.getParadas(); // Pide las paradas al coordinador

			if (paradasDisponibles != null && !paradasDisponibles.isEmpty()) {
				// Convierte la lista a ObservableList y la asigna a los ComboBox
				comboOrigen.setItems(FXCollections.observableArrayList(paradasDisponibles));
				comboDestino.setItems(FXCollections.observableArrayList(paradasDisponibles));
			} else {
				// Si el coordinador no tenía paradas, mostramos el error.
				System.err.println("Error: El Coordinador no proporcionó la lista de paradas.");
				mostrarAlerta("Error de Carga", "No se pudieron obtener las paradas para mostrar en las listas.");
			}
		} else {
			System.err.println("Error grave: Se intentó configurar el controlador con un Coordinador nulo.");
		}
	}

	/**
	 * Muestra una ventana emergente de alerta.
	 * 
	 * @param titulo  Título de la ventana.
	 * @param mensaje Mensaje a mostrar.
	 */
	private void mostrarAlerta(String titulo, String mensaje) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(titulo);
		alert.setHeaderText(null); // No usamos cabecera
		alert.setContentText(mensaje);
		alert.showAndWait();
	}
}