package colectivo.logica;

import java.time.LocalTime;
import java.util.ArrayList;
// import java.util.Arrays; // No se usa
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import colectivo.aplicacion.Constantes;
// import colectivo.aplicacion.Coordinador; // Eliminado para evitar dependencia circular
import colectivo.datos.CargarDatos;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.modelo.Tramo;

public class Calculo {

	// private Coordinador coordinador; // Eliminado, no se usa si el método es estático
	
	public static List<List<Recorrido>> calcularRecorrido(Parada paradaOrigen, Parada paradaDestino, int diaSemana,
			LocalTime horaLlegaParada, Map<String, Tramo> tramos) {

		// Tu test depende de que esto funcione:
		Map<String, Linea> lineasDelSistema = CargarDatos.getLineasCargadas();
		if (lineasDelSistema == null) {
			return Collections.emptyList();
		}

		List<List<Recorrido>> todosLosResultados = new ArrayList<>();

		// Búsqueda de viajes directos
		buscarViajesDirectos(paradaOrigen, paradaDestino, diaSemana, horaLlegaParada, lineasDelSistema, tramos,
				todosLosResultados);

		// Ordenar los resultados para que el test sea consistente
		Collections.sort(todosLosResultados, Comparator.comparing(viaje -> viaje.get(0).getLinea().getCodigo()));

		return todosLosResultados;
	}

	private static void buscarViajesDirectos(Parada paradaOrigen, Parada paradaDestino, int diaSemana,
			LocalTime horaLlegaParada, Map<String, Linea> lineas, Map<String, Tramo> tramos,
			List<List<Recorrido>> todosLosResultados) {

		for (Linea linea : lineas.values()) {
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
							List<Parada> paradasDelRecorrido = paradasDeLaLinea.subList(idxOrigen, idxDestino + 1);

							Recorrido r = new Recorrido(linea, new ArrayList<>(paradasDelRecorrido), horaPasoPorOrigen,
									duracionTrayecto);

							todosLosResultados.add(Collections.singletonList(r));

							break;
						}
					}
				}
			}
		}
	}

	private static int calcularTiempoEntreParadas(List<Parada> paradas, int idxInicio, int idxFin, Map<String, Tramo> tramos) {
		int tiempo = 0;
		for (int i = idxInicio; i < idxFin; i++) {
			String clave = paradas.get(i).getCodigo() + "-" + paradas.get(i + 1).getCodigo();
			Tramo tramo = tramos.get(clave);
			if (tramo != null && tramo.getTipo() == Constantes.COLECTIVO) {
				tiempo += tramo.getTiempo();
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