package colectivo.interfaz;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import colectivo.controlador.Coordinador;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.util.StringConverter;

public class ControladorInterfaz {

    @FXML private ComboBox<Parada>  comboOrigen;
    @FXML private ComboBox<Parada>  comboDestino;
    @FXML private ComboBox<String>  comboDia;     // Lunes..Domingo
    @FXML private ComboBox<Integer> comboHora;    // 0..23
    @FXML private ComboBox<Integer> comboMinuto;  // 0..59
    @FXML private Button            btnCalcular;
    @FXML private TextArea          resultadoArea;

    private Coordinador coordinador;
    private final Map<String,Integer> diasMap = new HashMap<>();

    public void init(Coordinador coordinador, List<Parada> paradasDisponibles) {
        this.coordinador = coordinador;

        // Paradas
        comboOrigen.getItems().setAll(paradasDisponibles);
        comboDestino.getItems().setAll(paradasDisponibles);

        // Día de la semana (palabras)
        comboDia.getItems().setAll("Lunes","Martes","Miércoles","Jueves","Viernes","Sábado","Domingo");
        comboDia.getSelectionModel().select("Lunes");
        diasMap.put("Lunes", 1);
        diasMap.put("Martes", 2);
        diasMap.put("Miércoles", 3);
        diasMap.put("Jueves", 4);
        diasMap.put("Viernes", 5);
        diasMap.put("Sábado", 6);
        diasMap.put("Domingo", 7);

        // Hora y minuto (dos dígitos)
        comboHora.getItems().setAll(rango(0, 23));
        comboMinuto.getItems().setAll(rango(0, 59));
        comboHora.setConverter(dosDigitos());
        comboMinuto.setConverter(dosDigitos());

        // Selección inicial (como la imagen)
        comboHora.getSelectionModel().select(Integer.valueOf(10));
        comboMinuto.getSelectionModel().select(Integer.valueOf(0));
    }

    @FXML
    private void onCalcular() {
        Parada origen  = comboOrigen.getValue();
        Parada destino = comboDestino.getValue();
        String diaTxt  = comboDia.getValue();
        Integer hh     = comboHora.getValue();
        Integer mm     = comboMinuto.getValue();

        if (origen == null || destino == null || diaTxt == null || hh == null || mm == null) {
            pintarAdvertencia("⚠️ Por favor complete todos los campos.");
            return;
        }
        if (origen.equals(destino)) {
            pintarAdvertencia("⚠️ La parada de origen y destino no pueden ser la misma.");
            return;
        }

        int dia = diasMap.get(diaTxt);
        LocalTime hora = LocalTime.of(hh, mm);

        try {
            var recorridos = coordinador.calcularRecorrido(origen, destino, dia, hora);
            mostrarResultados(recorridos);
        } catch (Exception ex) {
            pintarError("❌ Error al calcular el recorrido: " + ex.getMessage());
        }
    }

    private void mostrarResultados(List<List<Recorrido>> listaRecorridos) {
        if (listaRecorridos == null || listaRecorridos.isEmpty()) {
            resultadoArea.setText("ℹ️ No hay recorridos disponibles para la búsqueda realizada.");
            resultadoArea.setStyle("-fx-control-inner-background: #d1ecf1; -fx-text-fill: #0c5460;");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════\n");
        sb.append("          RECORRIDOS DISPONIBLES\n");
        sb.append("═══════════════════════════════════════════\n\n");

        int i = 1;
        for (var opcion : listaRecorridos) {
            sb.append("🚏 Opción ").append(i++).append(":\n");
            sb.append("───────────────────────────────────────────\n");
            for (var r : opcion) {
                if (r.getLinea() != null) {
                    sb.append("  🚍 Línea: ").append(r.getLinea().getNombre())
                      .append(" (").append(r.getLinea().getCodigo()).append(")\n");
                } else {
                    sb.append("  🚶 Tramo Caminando\n");
                }
                var ps = r.getParadas();
                if (!ps.isEmpty()) {
                    sb.append("     Desde: ").append(ps.get(0).getDireccion()).append("\n");
                    sb.append("     Hasta: ").append(ps.get(ps.size() - 1).getDireccion()).append("\n");
                }
                sb.append("     Sale:  ").append(r.getHoraSalida()).append("\n");

                // Duración sin mostrar segundos cuando son 0
                int totalSeg = r.getDuracion();
                int min = totalSeg / 60;
                int seg = totalSeg % 60;

                sb.append("     Duración: ").append(min).append(" min");
                if (seg != 0) {
                    sb.append(" ").append(seg).append(" seg");
                }
                sb.append("\n\n");
            }
        }
        resultadoArea.setText(sb.toString());
        resultadoArea.setStyle("-fx-control-inner-background: #f8f9fa; -fx-text-fill: black;");
    }

    // Utilitarios
    private List<Integer> rango(int desde, int hasta) {
        List<Integer> l = new ArrayList<>();
        for (int i = desde; i <= hasta; i++) l.add(i);
        return l;
    }

    private StringConverter<Integer> dosDigitos() {
        return new StringConverter<Integer>() {
            @Override public String toString(Integer value) {
                if (value == null) return "";
                return String.format("%02d", value);
            }
            @Override public Integer fromString(String s) {
                return (s == null || s.isEmpty()) ? null : Integer.valueOf(s);
            }
        };
    }

    private void pintarAdvertencia(String msg) {
        resultadoArea.setText(msg);
        resultadoArea.setStyle("-fx-control-inner-background: #fff3cd; -fx-text-fill: #856404;");
    }

    private void pintarError(String msg) {
        resultadoArea.setText(msg);
        resultadoArea.setStyle("-fx-control-inner-background: #f8d7da; -fx-text-fill: #721c24;");
    }
}