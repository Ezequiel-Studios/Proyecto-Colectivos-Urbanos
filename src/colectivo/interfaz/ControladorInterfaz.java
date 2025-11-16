package colectivo.interfaz;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Supplier;

import colectivo.controlador.Coordinador;
import colectivo.logica.Recorrido;
import colectivo.modelo.Parada;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Accordion;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * JavaFX view controller. This class acts as the dedicated controller for the
 * main user interface FXML file. It manages all user interactions, handles
 * input validation, orchestrates UI updates, and delegates business logic
 * requests to the {@code Coordinador}.
 * 
 * @author Juliana Martin
 * @author Ezequiel Ramos
 * @author Nerea Toledo
 */
public class ControladorInterfaz {

	/** FXML UI components (injected by JavaFX. */
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

	/**
	 * Reference to the application's central controller for business logic
	 * delegation.
	 */
	private Coordinador coordinador;

	/** Resource bundle for localized messages (i18n). */
	private ResourceBundle resources;

	/**
	 * Utility class for UI specific tasks (creating cells, panels, converters,
	 * etc.).
	 */
	private UtilidadInterfaz utilidad;

	/**
	 * Internal map to quickly convert localized day names (String) to their numeric
	 * code (Integer).
	 */
	private final Map<String, Integer> diasMap = new HashMap<>();

	/**
	 * Map to convert localized language names to their ISO code for language
	 * switching.
	 */
	private final Map<String, String> idiomasDisponibles = new HashMap<>();

	/** Logger instance for logging events, errors and exceptions. */
	private static final Logger LOGGER = LogManager.getLogger(ControladorInterfaz.class);

	/** Service for executing heavy tasks asynchronously off the UI thread. */
	private final AsyncService asyncService = new AsyncService();

	/**
	 * Initializes the controller with available stops and sets up UI components.
	 * 
	 * @param coordinador        The central coordinator of the application.
	 * @param paradasDisponibles The list of all available stops to populate the
	 *                           combo boxes.
	 * @param resources          The resource bundle for initial localization.
	 * @param stage              The primary stage of the JavaFX application.
	 */
	public void init(Coordinador coordinador, List<Parada> paradasDisponibles, ResourceBundle resources, Stage stage) {
		this.coordinador = coordinador;
		this.resources = resources;
		this.utilidad = new UtilidadInterfaz(resources);

		// Stops
		comboOrigen.getItems().setAll(paradasDisponibles);
		comboDestino.getItems().setAll(paradasDisponibles);

		comboOrigen.setButtonCell(utilidad.createParadaListCell());
		comboDestino.setButtonCell(utilidad.createParadaListCell());

		comboOrigen.setCellFactory(lv -> utilidad.createParadaListCell());
		comboDestino.setCellFactory(lv -> utilidad.createParadaListCell());

		comboOrigen.setOnAction(e -> handleSeleccionParada(comboOrigen.getValue(), "origen"));
		comboDestino.setOnAction(e -> handleSeleccionParada(comboDestino.getValue(), "destino"));

		// Days of the week (i18n setup)
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

		// Hour and minute (two-digit formatting)
		comboHora.getItems().setAll(utilidad.rango(0, 23));
		comboMinuto.getItems().setAll(utilidad.rango(0, 59));
		comboHora.setConverter(utilidad.dosDigitos());
		comboMinuto.setConverter(utilidad.dosDigitos());

		// Default selection
		comboHora.getSelectionModel().select(Integer.valueOf(10));
		comboMinuto.getSelectionModel().select(Integer.valueOf(0));

		// Languages (i18n menu setup)
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

		// Map (WebView setup)
		this.webEngine = webViewMapa.getEngine();
		String mapaHtmlUrl = getClass().getResource("mapa.html").toExternalForm();
		webEngine.load(mapaHtmlUrl);

		// Shutdown hook for Async service
		stage.setOnCloseRequest(event -> {
			LOGGER.info("Ventana cerrada detectada. Apagando AsyncService...");
			asyncService.shutdown();
		});
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
			String tipo = resources.getString("advertencia");
			String mensajeEspecifico = resources.getString("advertenciaCompletaCampos");

			mostrarMensajeAcordion(tipo, mensajeEspecifico);
			return;
		}
		if (origen.equals(destino)) {
			String tipo = resources.getString("advertencia");
			String mensajeEspecifico = resources.getString("advertenciaOrigenDestinoIguales");
			mostrarMensajeAcordion(tipo, mensajeEspecifico);
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

		btnCalcular.setDisable(true);
		animacionCarga.setVisible(true);
		accordionResultados.getPanes().clear();

		Supplier<List<List<Recorrido>>> tareaDeFondo = () -> {
			try {
				Thread.sleep(2000); // solo para prueba, pausa artificial
				return coordinador.calcularRecorrido(origen, destino, dia, hora);

			} catch (InterruptedException e) {
				LOGGER.warn("El hilo de cálculo fue interrumpido.");
				throw new RuntimeException("Cálculo interrumpido", e);
			}
		};

		Consumer<List<List<Recorrido>>> enExito = (resultados) -> {
			animacionCarga.setVisible(false);
			mostrarResultados(resultados);
			btnCalcular.setDisable(false);
		};

		Consumer<Exception> enFallo = (ex) -> {
			mostrarMensajeAcordion(resources.getString("error"), resources.getString("errorCalculo"));
			btnCalcular.setDisable(false);
		};

		asyncService.ejecutarAsync(tareaDeFondo, enExito, enFallo);
	}

	/**
	 * Handles the action when the "Limpiar" button is pressed. Resets all input
	 * fields to default values, clears the results pane, and clears the route drawn
	 * on the map.
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
	 * Displays the list of available routes in the results Accordion.
	 * 
	 * @param listaRecorridos The list of route options returned by the coordinator.
	 */
	private void mostrarResultados(List<List<Recorrido>> listaRecorridos) {
		accordionResultados.getPanes().clear();

		if (webEngine != null)
			webEngine.executeScript("limpiarRecorrido()");

		if (listaRecorridos == null || listaRecorridos.isEmpty()) {
			LOGGER.info("No se encontraron resultados disponibles.");

			String mensaje = resources.getString("resultadoNoDisponible");
			String tipo = resources.getString("info");

			TitledPane panelInfo = utilidad.crearPanelMensaje(tipo, mensaje, resources);

			accordionResultados.getPanes().add(panelInfo);
			accordionResultados.setExpandedPane(panelInfo);
			return;
		}

		List<TitledPane> nuevosPaneles = new ArrayList<>();
		int i = 1;
		for (var opcion : listaRecorridos) {

			TitledPane panelOpcion = utilidad.crearPanelRecorrido(i++, opcion, this::dibujarRutaParaOpcion);
			nuevosPaneles.add(panelOpcion);
		}
		accordionResultados.getPanes().setAll(nuevosPaneles);

		if (!nuevosPaneles.isEmpty()) {
			accordionResultados.setExpandedPane(nuevosPaneles.get(0));
		}
	}

	/**
	 * Draws the geographical route of a chosen option on the embedded map
	 * (WebView). Generates a JSON string representing the route segments.
	 * 
	 * @param opcion The list of {@code Recorrido} objects representing the selected
	 *               route.
	 */
	private void dibujarRutaParaOpcion(List<Recorrido> opcion) {
		if (webEngine == null || opcion == null || opcion.isEmpty())
			return;

		String jsonRuta = utilidad.generarJsonRuta(opcion);

		String jsDraw = String.format("dibujarRecorridoSegmentado(%s);", jsonRuta);
		webEngine.executeScript(jsDraw);
	}

	/**
	 * Calls the coordinator to change the application language, but only if the
	 * selected language is different from the current one.
	 * 
	 * @param nombreIdiomaElegido The localized name (String) of the chosen
	 *                            language.
	 */
	private void cambiarIdiomaSiEsNecesario(String nombreIdiomaElegido) {
		String codigoIdioma = idiomasDisponibles.get(nombreIdiomaElegido);
		if (codigoIdioma != null && !codigoIdioma.equals(Coordinador.getLocaleActual().getLanguage())) {
			coordinador.cambiarIdioma(codigoIdioma);
		} else {
			LOGGER.debug("Idioma seleccionado ({}) ya es el actual.", codigoIdioma);
		}
	}

	/**
	 * Handles the selection of a stop in the combo boxes (Origin/Destination). It
	 * executes a JavaScript call to mark the selected stop's location on the map.
	 * 
	 * @param parada The selected {@code Parada} object.
	 * @param tipo   The type of stop ("origen" or "destino").
	 */
	public void handleSeleccionParada(Parada parada, String tipo) {
		if (parada != null && webEngine != null) {
			String jsCall = String.format(Locale.ROOT, "marcarParada(%f, %f, '%s', '%s');", parada.getLatitud(),
					parada.getLongitud(), parada.getDireccion(), tipo);

			webEngine.executeScript(jsCall);
		}
	}

	/**
	 * Clears the results area and displays a specific warning or error message in a
	 * single {@code TitledPane}.
	 * 
	 * @param tipo The type of message (e.g., "Advertencia", "Error", "Info"),
	 *             localized.
	 * @param msg  The specific content of the message.
	 */
	private void mostrarMensajeAcordion(String tipo, String msg) {
		accordionResultados.getPanes().clear();

		TitledPane panelMensaje = utilidad.crearPanelMensaje(tipo, msg, resources);
		accordionResultados.getPanes().add(panelMensaje);
		accordionResultados.setExpandedPane(panelMensaje);
	}
}