package colectivo.logica;

import java.time.LocalTime;
import java.util.ArrayList;
// import java.util.Arrays; // No se usa
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import colectivo.aplicacion.Constantes;
import colectivo.conexion.Factory;
import colectivo.dao.LineaDAO;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.modelo.Tramo;

public class Calculo {
	private static Map<String, Linea> lineasDelSistema = null;

	public Calculo() {
	}

	public List<List<Recorrido>> calcularRecorrido(Parada paradaOrigen, Parada paradaDestino, int diaSemana,
			LocalTime horaLlegaParada, Map<String, Tramo> tramos) {

		if (lineasDelSistema == null) {
			System.out.println("Calculo: Cargando líneas por primera vez..."); // Mensaje de depuración
			try {
				// Usamos la Factory para obtener el LineaDAO
				LineaDAO lineaDAO = (LineaDAO) Factory.getInstancia("LINEA"); // Usar clave en MAYÚSCULAS
				// Le pedimos los datos y los guardamos en la caché estática.
				lineasDelSistema = lineaDAO.buscarTodos();
				if (lineasDelSistema == null || lineasDelSistema.isEmpty()) {
					System.err.println("Error crítico: LineaDAO devolvió un mapa nulo o vacío.");
					return Collections.emptyList();
				}
				System.out.println("Calculo: Líneas cargadas exitosamente.");
			} catch (Exception e) {
				System.err.println("Error crítico al cargar las líneas dentro de Calculo: " + e.getMessage());
				e.printStackTrace();
				return Collections.emptyList(); // No podemos calcular sin líneas
			}
		}

		List<List<Recorrido>> todosLosResultados = new ArrayList<>();

		// Búsqueda de viajes directos
		buscarViajesDirectos(paradaOrigen, paradaDestino, diaSemana, horaLlegaParada, lineasDelSistema, tramos,
				todosLosResultados);

		// Ordenar los resultados para que el test sea consistente
		Collections.sort(todosLosResultados, Comparator.comparing(viaje -> viaje.get(0).getLinea().getCodigo()));

		return todosLosResultados;
	}

	private void buscarViajesDirectos(Parada paradaOrigen, Parada paradaDestino, int diaSemana,
			LocalTime horaLlegaParada, Map<String, Linea> lineas, Map<String, Tramo> tramos,
			List<List<Recorrido>> todosLosResultados) {

		for (Linea linea : lineasDelSistema.values()) {
			List<Parada> paradasDeLaLinea = linea.getParadas();
			int idxOrigen = paradasDeLaLinea.indexOf(paradaOrigen);
			int idxDestino = paradasDeLaLinea.indexOf(paradaDestino);

			if (idxOrigen != -1 && idxDestino != -1 && idxOrigen < idxDestino) {
				int tiempoHastaOrigen = calcularTiempoEntreParadas(paradasDeLaLinea, 0, idxOrigen, tramos);

				for (Linea.Frecuencia frecuencia : linea.getFrecuencias()) {
					if (frecuencia.getDiaSemana() == diaSemana) {
						LocalTime horaPasoPorOrigen = frecuencia.getHora().plusSeconds(tiempoHastaOrigen);
						if (!horaPasoPorOrigen.isBefore(horaLlegaParada)) {
							int duracionTrayecto = calcularTiempoEntreParadas(paradasDeLaLinea, idxOrigen, idxDestino,
									tramos);
							if (idxDestino + 1 <= paradasDeLaLinea.size()) {
								List<Parada> paradasDelRecorrido = paradasDeLaLinea.subList(idxOrigen, idxDestino + 1);
								Recorrido r = new Recorrido(linea, new ArrayList<>(paradasDelRecorrido),
										horaPasoPorOrigen, duracionTrayecto);
								todosLosResultados.add(Collections.singletonList(r));
								break;
							} else {
								System.err.println(
										"Error: Índice fuera de rango (Directo) para línea " + linea.getCodigo());
							}
						}
					}
				}
			}
		}
	}

	private int calcularTiempoEntreParadas(List<Parada> paradas, int idxInicio, int idxFin, Map<String, Tramo> tramos) {
	    int tiempo = 0;
	    for (int i = idxInicio; i < idxFin; i++) {
            if (i + 1 < paradas.size()) {
                String clave = paradas.get(i).getCodigo() + "-" + paradas.get(i + 1).getCodigo();
	            Tramo tramo = tramos.get(clave);
	            if (tramo != null && tramo.getTipo() == Constantes.COLECTIVO) {
	                tiempo += tramo.getTiempo();
	            }
            } else {
                 System.err.println("Error: Índice fuera de rango en calcularTiempoEntreParadas.");
                 break;
            }
	    }
	    return tiempo;
	}

	private static List<List<Recorrido>> buscarConexiones() {
		List<List<Recorrido>> recorridos = new ArrayList<>();

		return recorridos;
	}

	private List<Parada> buscarParadasCarganas() { // Renombrado de "Cercanas"
		List<Parada> paradasCercanas = new ArrayList<>();

		return paradasCercanas;
	}

}