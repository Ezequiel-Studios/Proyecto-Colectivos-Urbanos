package colectivo.logica;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import colectivo.conexion.Factory;
import colectivo.controlador.Constantes;
import colectivo.dao.LineaDAO;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.modelo.Tramo;

public class Calculo {
	
	private static Map<String, Linea> lineasDelSistema = null;

	public Calculo() {
		
	}

	/**
	 * Calculates possible routes between two stops.
	 * @param paradaOrigen     Origin stop.
	 * @param paradaDestino    Destination stop.
	 * @param diaSemana        Day of the week (integer).
	 * @param horaLlegaParada  Desired arrival time at origin stop.
	 * @param tramos           Map of all segments between stops.
	 * @return a list of lists with all the possible routes found.
	 * */
	public List<List<Recorrido>> calcularRecorrido(Parada paradaOrigen, Parada paradaDestino, int diaSemana,
			LocalTime horaLlegaParada, Map<String, Tramo> tramos) {

		if (lineasDelSistema == null) {
			try {
				LineaDAO lineaDAO = (LineaDAO) Factory.getInstancia("LINEA");
				lineasDelSistema = lineaDAO.buscarTodos();
				if (lineasDelSistema == null || lineasDelSistema.isEmpty()) {
					System.err.println("Error crítico: LineaDAO devolvió un mapa nulo o vacío.");
					return Collections.emptyList();
				}
			} catch (Exception e) {
				System.err.println("Error crítico al cargar las líneas dentro de Calculo: " + e.getMessage());
				e.printStackTrace();
				return Collections.emptyList(); 
			}
		}

		List<List<Recorrido>> todosLosResultados = new ArrayList<>();

		buscarViajesDirectos(paradaOrigen, paradaDestino, diaSemana, horaLlegaParada, tramos, todosLosResultados);
		Collections.sort(todosLosResultados, Comparator.comparing(viaje -> viaje.get(0).getLinea().getCodigo()));

		return todosLosResultados;
	}

	/**
	 * Finds direct routes where the origin and destination stop are in
	 * the same line.
	 * @param paradaOrigen       Origin stop.
	 * @param paradaDestino      Destination stop.
	 * @param diaSemana          Day of the week (integer).
	 * @param horaLlegaParada    Desired arrival time.
	 * @param lineas             Map of all lines.
	 * @param tramos             Map of all segments between stops.
	 * @param todosLosResultados List of lists of all possible routes (to be populated).
	 * */
	private void buscarViajesDirectos(Parada paradaOrigen, Parada paradaDestino, int diaSemana, LocalTime horaLlegaParada, 
			Map<String, Tramo> tramos, List<List<Recorrido>> todosLosResultados) {

	    for (Linea linea : lineasDelSistema.values()) {
	        List<Parada> paradasDeLaLinea = linea.getParadas();
	        int idxOrigen = paradasDeLaLinea.indexOf(paradaOrigen);
	        int idxDestino = paradasDeLaLinea.indexOf(paradaDestino);

	        boolean paradaValida = (idxOrigen != -1 && idxDestino != -1 && idxOrigen < idxDestino);

	        if (paradaValida) {
	        	int tiempoHastaOrigen = calcularTiempoEntreParadas(paradasDeLaLinea, 0, idxOrigen, tramos);

	            List<Linea.Frecuencia> frecuenciasValidas = new ArrayList<>();
	            for (Linea.Frecuencia frecuencia : linea.getFrecuencias()) {
	                boolean mismoDia = frecuencia.getDiaSemana() == diaSemana;
	                LocalTime horaPasoPorOrigen = frecuencia.getHora().plusSeconds(tiempoHastaOrigen);
	                boolean horaValida = !horaPasoPorOrigen.isBefore(horaLlegaParada);

	                if (mismoDia && horaValida) {
	                    frecuenciasValidas.add(frecuencia);
	                }
	            }

	            if (!frecuenciasValidas.isEmpty()) {
	                Linea.Frecuencia primeraFrecuencia = frecuenciasValidas.get(0);
	                LocalTime horaPasoPorOrigen = primeraFrecuencia.getHora().plusSeconds(tiempoHastaOrigen);

	                if (idxDestino + 1 <= paradasDeLaLinea.size()) {
	                    List<Parada> paradasDelRecorrido = paradasDeLaLinea.subList(idxOrigen, idxDestino + 1);
	                    Recorrido r = new Recorrido(linea, new ArrayList<>(paradasDelRecorrido), horaPasoPorOrigen,
	                            calcularTiempoEntreParadas(paradasDeLaLinea, idxOrigen, idxDestino, tramos));
	                    todosLosResultados.add(Collections.singletonList(r));
	                } else {
	                    System.err.println("Error: Índice fuera de rango (Directo) para línea " + linea.getCodigo());
	                }
	            }
	        }
	    }
	}

	/**
	 * Calculates total travel time between two stops by summing each 
	 * segment's time.
	 * @param paradas   List of stops.
	 * @param idxInicio Origin stop index.
	 * @param idxFin    Destination stop index.
	 * @param tramos    Map with all the route segments.
	 * @return the travel time as an integer.
	 * */
	private int calcularTiempoEntreParadas(List<Parada> paradas, int idxInicio, int idxFin, Map<String, Tramo> tramos) {
	    int tiempo = 0;

	    for (int i = idxInicio; i < idxFin; i++) {
	        String clave = paradas.get(i).getCodigo() + "-" + paradas.get(i + 1).getCodigo();
	        Tramo tramo = tramos.get(clave);

	        if (tramo != null && tramo.getTipo() == Constantes.COLECTIVO) 
	            tiempo += tramo.getTiempo();
	    }
	    return tiempo;
	}

	/**
	 * Unimplemented method.
	 * */
	private static List<List<Recorrido>> buscarConexiones() {
		List<List<Recorrido>> recorridos = new ArrayList<>();

		return recorridos;
	}

	/**
	 * Unimplemented method.
	 * */
	private List<Parada> buscarParadasCercanas() { 
		List<Parada> paradasCercanas = new ArrayList<>();

		return paradasCercanas;
	}

}