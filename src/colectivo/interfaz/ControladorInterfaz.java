package colectivo.interfaz;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import colectivo.controlador.Coordinador;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Accordion;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import javafx.util.StringConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ControladorInterfaz {
	@FXML
	private ComboBox<Parada> comboOrigen;
	@FXML
	private ComboBox<Parada> comboDestino;
	@FXML
	private ComboBox<String> comboDia;
	@FXML
	private ComboBox<Integer> comboHora;
	@FXML
	private ComboBox<Integer> comboMinuto;
	@FXML
	private Button btnCalcular;
	@FXML
	private Button btnLimpiar;
	@FXML
	private Accordion accordionResultados;
	@FXML
	private MenuButton menuIdioma;
	@FXML
	private CheckBox checkFeriado;
	@FXML
	private WebView webViewMapa;
	@FXML
	private WebEngine webEngine;
	@FXML
	private ImageView animacionCarga;

	private Coordinador coordinador;
	private ResourceBundle resources;
	private Stage stage;
	private final Map<String, Integer> diasMap = new HashMap<>();
	private final Map<String, String> idiomasDisponibles = new HashMap<>();
	private static final Logger LOGGER = LogManager.getLogger(ControladorInterfaz.class);

	/**
	 * Initializes the controller with available stops and sets up ComboBoxes and
	 * default selections.
	 */
	public void init(Coordinador coordinador, List<Parada> paradasDisponibles, ResourceBundle resources, Stage stage) {
		this.coordinador = coordinador;
		this.resources = resources;
		this.stage = stage;

		// Stops
		comboOrigen.getItems().setAll(paradasDisponibles);
		comboDestino.getItems().setAll(paradasDisponibles);

		comboOrigen.setOnAction(e -> handleSeleccionParada(comboOrigen.getValue(), "origen"));
		comboDestino.setOnAction(e -> handleSeleccionParada(comboDestino.getValue(), "destino"));

		// Days of the week
		String clavesDias[] = { "diaLunes", "diaMartes", "diaMiercoles", "diaJueves", "diaViernes", "diaSabado",
				"diaDomingo" };
		List<String> diasTraducidos = new ArrayList<>();

		for (int i = 0; i < clavesDias.length; i++) {
			String clave = clavesDias[i];
			String nombreTraducido = resources.getString(clave).trim();
			String claveNormalizada = nombreTraducido.toLowerCase();
			int indiceDia = i + 1;
			diasMap.put(claveNormalizada, indiceDia);
			diasTraducidos.add(nombreTraducido);
		}
		comboDia.getItems().setAll(diasTraducidos);

		comboDia.getSelectionModel().select(resources.getString("diaLunes").trim());

		comboDia.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null) {
				String claveDomingo = resources.getString("diaDomingo").trim().toLowerCase();
				boolean esDomingo = newVal.trim().toLowerCase().equals(claveDomingo);

				checkFeriado.setDisable(esDomingo);
				if (esDomingo)
					checkFeriado.setSelected(false);
			}
		});

		// Hour and minute (two digits)
		comboHora.getItems().setAll(rango(0, 23));
		comboMinuto.getItems().setAll(rango(0, 59));
		comboHora.setConverter(dosDigitos());
		comboMinuto.setConverter(dosDigitos());

		// Default selection
		comboHora.getSelectionModel().select(Integer.valueOf(10));
		comboMinuto.getSelectionModel().select(Integer.valueOf(0));

		// Languages
		// Languages (para el MenuButton)
		// Nota: El Map 'idiomasDisponibles' ya debe estar definido como campo de la
		// clase.
		String nombreEs = resources.getString("nombreIdiomaEs");
		String nombreEn = resources.getString("nombreIdiomaEn");
		String nombrePt = resources.getString("nombreIdiomaPt");
		String nombreFr = resources.getString("nombreIdiomaFr");

		if (idiomasDisponibles.isEmpty()) {
			idiomasDisponibles.put(nombreEs, "es");
			idiomasDisponibles.put(nombreEn, "en");
			idiomasDisponibles.put(nombrePt, "pt");
			idiomasDisponibles.put(nombreFr, "fr");
		}

		MenuItem itemEs = new MenuItem(nombreEs);
		itemEs.setOnAction(e -> cambiarIdiomaSiEsNecesario(nombreEs));

		MenuItem itemEn = new MenuItem(nombreEn);
		itemEn.setOnAction(e -> cambiarIdiomaSiEsNecesario(nombreEn));

		MenuItem itemPt = new MenuItem(nombrePt);
		itemPt.setOnAction(e -> cambiarIdiomaSiEsNecesario(nombrePt));

		MenuItem itemFr = new MenuItem(nombreFr);
		itemFr.setOnAction(e -> cambiarIdiomaSiEsNecesario(nombreFr));

		menuIdioma.getItems().setAll(itemEs, itemEn, itemPt, itemFr);

		// map
		this.webEngine = webViewMapa.getEngine();
		String mapaHtmlUrl = getClass().getResource("mapa.html").toExternalForm();
		webEngine.load(mapaHtmlUrl);
	}

	/**
	 * Private method called when the Calcular button is pressed. Validates input
	 * and requests available routes from the coordinator.
	 */
	@FXML
	private void onCalcular() {
		final Parada origen = comboOrigen.getValue();
		final Parada destino = comboDestino.getValue();
		String diaTxt = comboDia.getValue();
		Integer hh = comboHora.getValue();
		Integer mm = comboMinuto.getValue();

		if (origen == null || destino == null || diaTxt == null || hh == null || mm == null) {
			pintarAdvertencia(resources.getString("advertenciaCompletaCampos"));
			return;
		}
		if (origen.equals(destino)) {
			pintarAdvertencia(resources.getString("advertenciaOrigenDestinoIguales"));
			return;
		}

		String claveDiaLimpia = diaTxt.trim().toLowerCase();

		int diaInicial = diasMap.get(claveDiaLimpia);

		String claveDomingo = resources.getString("diaDomingo").trim().toLowerCase();
		if (checkFeriado.isSelected() && !claveDiaLimpia.equals(claveDomingo)) {
			diaInicial = 7;
			LOGGER.info("Día marcado como feriado. Usando código de día: {}", diaInicial);
		}

		final int dia = diaInicial;
		final LocalTime hora = LocalTime.of(hh, mm);

		Task<List<List<Recorrido>>> task = new Task<>() {
			@Override
			protected List<List<Recorrido>> call() throws Exception {
				LOGGER.info("Task de cálculo iniciado en hilo de fondo...");

				Thread.sleep(2000); //agregado para ver como funciona
				return coordinador.calcularRecorrido(origen, destino, dia, hora);
			}
		};

		btnCalcular.setDisable(true);
		animacionCarga.setVisible(true);
		accordionResultados.getPanes().clear();

		task.setOnSucceeded(event -> {
			javafx.application.Platform.runLater(() -> {
				animacionCarga.setVisible(false);
				mostrarResultados(task.getValue());
				btnCalcular.setDisable(false);
			});
		});

		task.setOnFailed(event -> {
			javafx.application.Platform.runLater(() -> {
				animacionCarga.setVisible(false);
				Throwable ex = task.getException();
				LOGGER.error("Task de cálculo falló.", ex);
				pintarError("Error: " + ex.getMessage());
				btnCalcular.setDisable(false);
			});
		});

		coordinador.ejecutarCalculo(task);
	}

	/**
	 * Se llama cuando el usuario presiona "Limpiar". Resetea los campos de entrada
	 * y los resultados.
	 */
	@FXML
	private void onLimpiar() {
		LOGGER.info("Limpiando selecciones y resultados.");

		comboOrigen.setValue(null);
		comboDestino.setValue(null);

		comboDia.getSelectionModel().select(resources.getString("diaLunes").trim());
		comboHora.getSelectionModel().select(Integer.valueOf(10));
		comboMinuto.getSelectionModel().select(Integer.valueOf(0));
		checkFeriado.setSelected(false);

		accordionResultados.getPanes().clear();

		if (webEngine != null) {
			webEngine.executeScript("limpiarRecorrido()");
		}
	}

	/**
	 * Displays the list of available routes in the results area.
	 */
	/**
	 * Displays the list of available routes in the results Accordion.
	 */
	private void mostrarResultados(List<List<Recorrido>> listaRecorridos) {
		accordionResultados.getPanes().clear();

		if (webEngine != null)
			webEngine.executeScript("limpiarRecorrido()");

		if (listaRecorridos == null || listaRecorridos.isEmpty()) {
			LOGGER.info("No se encontraron resultados disponibles.");

			Label labelInfo = new Label(resources.getString("resultadoNoDisponible"));
			TitledPane panelInfo = new TitledPane("Información", labelInfo);
			panelInfo.setStyle("-fx-control-inner-background: #d1ecf1; -fx-text-fill: #0c5460;");

			accordionResultados.getPanes().add(panelInfo);
			accordionResultados.setExpandedPane(panelInfo);
			return;
		}

		List<TitledPane> nuevosPaneles = new ArrayList<>();
		int i = 1;
		for (var opcion : listaRecorridos) {

			String tituloPanel = resources.getString("opcion") + " " + i++;
			if (!opcion.isEmpty()) {
				Recorrido primerTramo = opcion.get(0);
				if (primerTramo.getLinea() != null) {
					tituloPanel += ": (" + resources.getString("linea") + " " + primerTramo.getLinea().getNombre()
							+ ")";
				} else {
					tituloPanel += ": (" + resources.getString("tramoCaminando") + ")";
				}
			}

			StringBuilder sbContenido = new StringBuilder();
			for (var r : opcion) {
				if (r.getLinea() != null) {
					sbContenido.append(resources.getString("linea")).append(" ").append(r.getLinea().getNombre())
							.append(" (").append(r.getLinea().getCodigo()).append(")\n");
				} else {
					sbContenido.append(resources.getString("tramoCaminando")).append("\n");
				}
				var ps = r.getParadas();
				if (!ps.isEmpty()) {
					sbContenido.append("     ").append(resources.getString("desde")).append(" ")
							.append(ps.get(0).getDireccion()).append("\n");
					sbContenido.append("     ").append(resources.getString("hasta")).append(" ")
							.append(ps.get(ps.size() - 1).getDireccion()).append("\n");
				}
				sbContenido.append("     ").append(resources.getObject("sale")).append("  ").append(r.getHoraSalida())
						.append("\n");

				int totalSeg = r.getDuracion();
				int min = totalSeg / 60;
				int seg = totalSeg % 60;

				sbContenido.append("     ").append(resources.getString("duracion")).append(" ").append(min).append(" ")
						.append(resources.getString("minutos"));
				if (seg != 0) {
					sbContenido.append(" ").append(seg).append(" ").append(resources.getString("segundos"));
				}
				sbContenido.append("\n\n");
			}

			Label contenidoLabel = new Label(sbContenido.toString());
			contenidoLabel.setWrapText(true);
			contenidoLabel.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12; -fx-padding: 5;"); 

			ScrollPane scrollPane = new ScrollPane();
			scrollPane.setContent(contenidoLabel);

			scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

			scrollPane.setFitToWidth(true);

			scrollPane.setMaxHeight(250);

			TitledPane panelOpcion = new TitledPane(tituloPanel, scrollPane);
			nuevosPaneles.add(panelOpcion);
		}

		accordionResultados.getPanes().setAll(nuevosPaneles);

		if (!nuevosPaneles.isEmpty()) {
			accordionResultados.setExpandedPane(nuevosPaneles.get(0));
		}

		if (webEngine != null && !listaRecorridos.isEmpty()) {
			List<Recorrido> opcion = listaRecorridos.get(0);

			List<Parada> paradasMapa = new ArrayList<>();
			for (Recorrido r : opcion) {
				if (paradasMapa.isEmpty() || !paradasMapa.get(paradasMapa.size() - 1).equals(r.getParadas().get(0)))
					paradasMapa.addAll(r.getParadas());
				else
					paradasMapa.addAll(r.getParadas().subList(1, r.getParadas().size()));
			}

			StringBuilder jsonBuilder = new StringBuilder("[");
			for (int j = 0; j < paradasMapa.size(); j++) {
				Parada p = paradasMapa.get(j);
				jsonBuilder.append(
						String.format(Locale.ROOT, "{\"lat\":%f, \"lon\":%f}", p.getLatitud(), p.getLongitud()));
				if (j < paradasMapa.size() - 1)
					jsonBuilder.append(",");
			}
			jsonBuilder.append("]");
			String jsDraw = "dibujarRecorrido(" + jsonBuilder.toString() + ");";
			webEngine.executeScript(jsDraw);
		}
	}

	/**
	 * Llama al coordinador para cambiar el idioma, solo si el idioma seleccionado
	 * es diferente al actual.
	 * 
	 * @param nombreIdiomaElige El nombre (String) del idioma elegido.
	 */
	private void cambiarIdiomaSiEsNecesario(String nombreIdiomaElegido) {
		String codigoIdioma = idiomasDisponibles.get(nombreIdiomaElegido);
		if (codigoIdioma != null && !codigoIdioma.equals(Coordinador.getLocaleActual().getLanguage())) {
			coordinador.cambiarIdioma(codigoIdioma);
		} else {
			LOGGER.debug("Idioma seleccionado ({}) ya es el actual.", codigoIdioma);
		}
	}

	/***/
	public void handleSeleccionParada(Parada parada, String tipo) {
		if (parada != null && webEngine != null) {
			String jsCall = String.format(Locale.ROOT, "marcarParada(%f, %f, '%s', '%s');", parada.getLatitud(),
					parada.getLongitud(), parada.getDireccion(), tipo);

			webEngine.executeScript(jsCall);
		}
	}

	/**
	 * Generates a list of integers within a given range. Used to populate
	 * ComboBoxes for hours and minutes.
	 * 
	 * @param desde Starting integer value.
	 * @param hasta Ending integer value.
	 * @return a list containing all integers in the specified range.
	 */
	private List<Integer> rango(int desde, int hasta) {
		List<Integer> l = new ArrayList<>();
		for (int i = desde; i <= hasta; i++)
			l.add(i);
		return l;
	}

	/**
	 * Used for displaying hours and minutes consistently.
	 * 
	 * @return a StringConverter for formatting integer values with two digits.
	 */
	private StringConverter<Integer> dosDigitos() {
		return new StringConverter<Integer>() {
			@Override
			public String toString(Integer value) {
				if (value == null)
					return "";
				return String.format("%02d", value);
			}

			@Override
			public Integer fromString(String s) {
				return (s == null || s.isEmpty()) ? null : Integer.valueOf(s);
			}
		};
	}

	/**
	 * Displays a warning message in the results Accordion. * @param msg The warning
	 * message to be shown.
	 */
	private void pintarAdvertencia(String msg) {
		accordionResultados.getPanes().clear();

		Label labelAdvertencia = new Label(msg);
		labelAdvertencia.setWrapText(true);

		TitledPane panelAdvertencia = new TitledPane("Advertencia", labelAdvertencia);
		panelAdvertencia.setStyle("-fx-control-inner-background: #fff3cd; -fx-text-fill: #856404;");

		accordionResultados.getPanes().add(panelAdvertencia);
		accordionResultados.setExpandedPane(panelAdvertencia);
	}

	/**
	 * Displays an error message in the results Accordion.
	 ** 
	 * @param msg The error message to be shown.
	 */
	private void pintarError(String msg) {
		accordionResultados.getPanes().clear();

		Label labelError = new Label(msg);
		labelError.setWrapText(true);

		TitledPane panelError = new TitledPane("Error", labelError);
		panelError.setStyle("-fx-control-inner-background: #f8d7da; -fx-text-fill: #721c24;");

		accordionResultados.getPanes().add(panelError);
		accordionResultados.setExpandedPane(panelError);
	}
}