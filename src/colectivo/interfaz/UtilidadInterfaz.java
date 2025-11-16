package colectivo.interfaz;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.logica.Recorrido;
import colectivo.modelo.Parada;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.util.StringConverter;

/**
 * This class provides utility methods for the UI layer (ControladorInterfaz).
 * It handles formatting, i18n localization of UI elements, data conversion, and
 * the construction of complex visual components (like results panels).
 * 
 * @author Juliana Martin
 * @author Ezequiel Ramos
 * @author Nerea Toledo
 */
public class UtilidadInterfaz {

	/**
	 * Resource bundle containing localized strings for the current application
	 * locale.
	 */
	private final ResourceBundle resources;

	/** Logger instance for logging events, errors and exceptions. */
	private static final Logger LOGGER = LogManager.getLogger(UtilidadInterfaz.class);

	/**
	 * Constructor that initializes the utility with the current resources.
	 * 
	 * @param resources The current {@code ResourceBundle} for localization.
	 */
	public UtilidadInterfaz(ResourceBundle resources) {
		this.resources = resources;
	}

	/**
	 * Creates a custom list cell factory for the {@code Parada} ComboBoxes. This
	 * custom cell overrides {@code updateItem} to display the stop's code and
	 * address, prepended by the localized word for "Stop" (fetched from the
	 * {@code ResourceBundle}).
	 * 
	 * @return A new {@code ListCell} implementation for displaying {@code Parada}
	 *         objects.
	 */
	public ListCell<Parada> createParadaListCell() {
		return new ListCell<Parada>() {
			@Override
			protected void updateItem(Parada item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
				} else {
					setText(resources.getString("etiquetaParada") + " " + item.getCodigo() + ": "
							+ item.getDireccion());
				}
			}
		};
	}

	/**
	 * Generates a sequential list of integers within a given range. Used to
	 * populate ComboBoxes for hours and minutes.
	 * 
	 * @param desde Starting integer value (inclusive).
	 * @param hasta Ending integer value (inclusive).
	 * @return A list containing all integers in the specified range.
	 */
	public List<Integer> rango(int desde, int hasta) {
		List<Integer> l = new ArrayList<>();
		for (int i = desde; i <= hasta; i++)
			l.add(i);
		return l;
	}

	/**
	 * Creates a {@code StringConverter} for formatting time values (hours/minutes).
	 * Used for displaying hours and minutes consistently.
	 * 
	 * @return a StringConverter for formatting integer values with two digits.
	 */
	public StringConverter<Integer> dosDigitos() {
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
	 * Generates a JSON string representing a route option for visualization on the
	 * map (WebView). Translates the list of {@code Recorrido} objects into a JSON
	 * array structure, including segment type ("bus" or "caminando") and the
	 * sequence of stop coordinates (latitude, longitude).
	 * 
	 * @param opcion The list of {@code Recorrido} objects representing one route
	 *               option.
	 * @return A JSON string suitable for execution by the {@code WebEngine}.
	 */
	public String generarJsonRuta(List<Recorrido> opcion) {
		if (opcion == null || opcion.isEmpty())
			return "[]";

		StringBuilder jsonBuilder = new StringBuilder("[");

		for (int i = 0; i < opcion.size(); i++) {
			Recorrido r = opcion.get(i);
			jsonBuilder.append("{");

			String tipo = (r.getLinea() == null) ? "caminando" : "bus";
			jsonBuilder.append("\"tipo\":\"").append(tipo).append("\",");

			jsonBuilder.append("\"paradas\":[");
			List<Parada> paradasSegmento = r.getParadas();
			for (int j = 0; j < paradasSegmento.size(); j++) {
				Parada p = paradasSegmento.get(j);
				jsonBuilder.append(
						String.format(Locale.ROOT, "{\"lat\":%f, \"lon\":%f}", p.getLatitud(), p.getLongitud()));
				if (j < paradasSegmento.size() - 1)
					jsonBuilder.append(",");
			}
			jsonBuilder.append("]");

			jsonBuilder.append("}");
			if (i < opcion.size() - 1)
				jsonBuilder.append(",");
		}
		jsonBuilder.append("]");

		return jsonBuilder.toString();
	}

	/**
	 * Creates a customized {@code TitledPane} to display a single route option
	 * result. Handles the construction of the panel title, which includes the route
	 * option number and the line name/walking segment type (localized).
	 * 
	 * @param indice      The sequential number of the route option.
	 * @param opcion      The route option data.
	 * @param dibujarRuta The callback function to execute when the panel is
	 *                    expanded (for map drawing).
	 * @return A fully configured {@code TitledPane} ready to be added to the
	 *         {@code Accordion}.
	 */
	public TitledPane crearPanelRecorrido(int indice, List<Recorrido> opcion, Consumer<List<Recorrido>> dibujarRuta) {

		String tituloPanel = resources.getString("opcion") + " " + indice;

		if (!opcion.isEmpty()) {
			Recorrido primerTramo = opcion.get(0);
			if (primerTramo.getLinea() != null) {
				tituloPanel += ": (" + resources.getString("linea") + " " + primerTramo.getLinea().getNombre() + ")";
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

		panelOpcion.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
			if (isNowExpanded) {
				dibujarRuta.accept(opcion);
			}
		});

		return panelOpcion;
	}

	/**
	 * Creates a customized {@code TitledPane} to display error, warning, or info
	 * messages. Styles the panel based on the message type (e.g., red for errors,
	 * yellow for warnings) using standard color conventions, which is good practice
	 * for user feedback.
	 * 
	 * @param tipo      The localized message type (e.g., "Error", "Advertencia",
	 *                  "Info").
	 * @param msg       The specific message content.
	 * @param resources The resource bundle for comparison.
	 * @return A fully configured and styled {@code TitledPane}.
	 */
	public TitledPane crearPanelMensaje(String tipo, String msg, ResourceBundle resources) {
		Label label = new Label(msg);
		label.setWrapText(true);

		String titulo = tipo;

		TitledPane panel = new TitledPane(titulo, label);

		if (tipo.equals(resources.getString("error"))) {
			panel.setStyle("-fx-control-inner-background: #f8d7da; -fx-text-fill: #721c24;");
		} else if (tipo.equals(resources.getString("advertencia"))) {
			panel.setStyle("-fx-control-inner-background: #fff3cd; -fx-text-fill: #856404;");
		} else if (tipo.equals(resources.getString("info"))) {
			panel.setStyle("-fx-control-inner-background: #d1ecf1; -fx-text-fill: #0c5460;");
		} else
			LOGGER.error("Etiqueta {} no encontrada.", tipo);

		return panel;
	}
}