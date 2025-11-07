package colectivo.logica;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.conexion.Factory;
import colectivo.controlador.Constantes;
import colectivo.dao.LineaDAO;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.modelo.Tramo;

public class Calculo {

	private static Map<String, Linea> lineasDelSistema = null;
	private static final Logger LOGGER = LogManager.getLogger(Calculo.class);

	public Calculo() {
	}

	/**
	 * Calcula los posibles recorridos entre dos paradas, siguiendo la regla de
	 * prioridad: 1.Directo, 2.Bus-Bus, 3.Bus-Caminando.
	 * 
	 * @param paradaOrigen    parada de inicio del viaje.
	 * @param paradaDestino   parada de destino
	 * @param diaSemana       día de semana para la búsqueda
	 * @param horaLlegaParada
	 * @param tramos          mapa de tramos disponibles
	 * @return una lista de listas con todos los recorridos hallados.
	 */
	public List<List<Recorrido>> calcularRecorrido(Parada paradaOrigen, Parada paradaDestino, int diaSemana,
			LocalTime horaLlegaParada, Map<String, Tramo> tramos) {

		LOGGER.debug("Iniciando cálculo de recorrido de {} a {}.", paradaOrigen.getCodigo(), paradaDestino.getCodigo());
		if (lineasDelSistema == null) {
			try {
				LineaDAO lineaDAO = (LineaDAO) Factory.getInstancia("LINEA");
				lineasDelSistema = lineaDAO.buscarTodos();
				if (lineasDelSistema == null || lineasDelSistema.isEmpty()) {
					LOGGER.error("Error crítico: LineaDAO devolvió un mapa nulo o vacío.");
					return Collections.emptyList();
				}
			} catch (Exception e) {
				LOGGER.error("Error crítico al cargar las líneas dentro de Calculo: ", e.getMessage());
				return Collections.emptyList();
			}
		}

		List<List<Recorrido>> todosLosResultados = new ArrayList<>();
		buscarViajesDirectos(paradaOrigen, paradaDestino, diaSemana, horaLlegaParada, tramos, todosLosResultados);

		if (!todosLosResultados.isEmpty()) {
			LOGGER.info("Cálculo finalizado. Se encontraron {} resultados directos.", todosLosResultados.size());
			Collections.sort(todosLosResultados, Comparator.comparing(viaje -> viaje.get(0).getLinea().getCodigo()));
			return todosLosResultados;
		}

		buscarConexionBusBus(paradaOrigen, paradaDestino, diaSemana, horaLlegaParada, tramos, todosLosResultados);

		if (!todosLosResultados.isEmpty()) {
			LOGGER.info("Cálculo finalizado. Se encontraron {} resultados con conexión.", todosLosResultados.size());
			Collections.sort(todosLosResultados, Comparator.comparing(viaje -> viaje.get(viaje.size() - 1)
					.getHoraSalida().plusSeconds(viaje.get(viaje.size() - 1).getDuracion())));
			return todosLosResultados;
		}

		buscarConexionCaminando(paradaOrigen, paradaDestino, diaSemana, horaLlegaParada, tramos, todosLosResultados);

		LOGGER.info("Cálculo finalizado. Se encontraron {} resultados con conexión caminando.",
				todosLosResultados.size());
		Collections.sort(todosLosResultados, Comparator.comparing(viaje -> {
			Recorrido ultimoTramo = viaje.get(viaje.size() - 1);
			return ultimoTramo.getHoraSalida().plusSeconds(ultimoTramo.getDuracion());
		}));

		return todosLosResultados;
	}

	/**
	 * Busca rutas directas (un único colectivo) entre la parada de origen y de
	 * destino.
	 * 
	 * @param paradaOrigen
	 * @param paradaDestino
	 * @param diaSemana
	 * @param horaLlegaParada
	 * @param tramos
	 * @param todosLosResultados lista con todos los recorridos encontrados.
	 */
	private void buscarViajesDirectos(Parada paradaOrigen, Parada paradaDestino, int diaSemana,
			LocalTime horaLlegaParada, Map<String, Tramo> tramos, List<List<Recorrido>> todosLosResultados) {

		LOGGER.debug("Buscando viajes directos de {} a {}.", paradaOrigen.getCodigo(), paradaDestino.getCodigo());
		for (Linea linea : lineasDelSistema.values()) {
			List<Parada> paradasDeLaLinea = linea.getParadas();
			int idxOrigen = paradasDeLaLinea.indexOf(paradaOrigen);
			int idxDestino = paradasDeLaLinea.indexOf(paradaDestino);

			boolean paradaValida = (idxOrigen != -1 && idxDestino != -1 && idxOrigen < idxDestino);

			if (paradaValida) {
				int tiempoHastaOrigen = calcularTiempoEntreParadas(paradasDeLaLinea, 0, idxOrigen, tramos);

				Iterator<Linea.Frecuencia> iter = linea.getFrecuencias().iterator();
				boolean primeraEncontrada = false;

				while (iter.hasNext() && !primeraEncontrada) {
					Linea.Frecuencia frecuencia = iter.next();

					boolean mismoDia = frecuencia.getDiaSemana() == diaSemana;
					LocalTime horaPasoPorOrigen = frecuencia.getHora().plusSeconds(tiempoHastaOrigen);
					boolean horaValida = !horaPasoPorOrigen.isBefore(horaLlegaParada);

					if (mismoDia && horaValida) {
						if (idxDestino + 1 <= paradasDeLaLinea.size()) {
							List<Parada> paradasDelRecorrido = paradasDeLaLinea.subList(idxOrigen, idxDestino + 1);
							int duracionTrayecto = calcularTiempoEntreParadas(paradasDeLaLinea, idxOrigen, idxDestino,
									tramos);
							Recorrido r = new Recorrido(linea, new ArrayList<>(paradasDelRecorrido), horaPasoPorOrigen,
									duracionTrayecto);
							todosLosResultados.add(Collections.singletonList(r));

							primeraEncontrada = true;
						} else {
							LOGGER.error("Error: Índice fuera de rango (Directo) para línea ", linea.getCodigo());
						}
					}
				}
			}
		}
	}

	/**
	 * Calcula el tiempo de viaje (solo tramos COLECTIVO) entre dos índices de
	 * parada.
	 * 
	 * @param paradas   lista con todas las paradas
	 * @param idxInicio índice de parada de origen
	 * @param idxFin    índice de parada de destino
	 * @param tramos    mapa con los tramos disponibles
	 * @return el tiempo entre paradas.
	 */
	private int calcularTiempoEntreParadas(List<Parada> paradas, int idxInicio, int idxFin, Map<String, Tramo> tramos) {

		LOGGER.debug("Calculando tiempo de {} ({}) a {} ({})", paradas.get(idxInicio).getCodigo(), idxInicio,
				paradas.get(idxFin).getCodigo(), idxFin);
		int tiempo = 0;
		for (int i = idxInicio; i < idxFin; i++) {
			if (i + 1 < paradas.size()) {
				String clave = paradas.get(i).getCodigo() + "-" + paradas.get(i + 1).getCodigo();
				Tramo tramo = tramos.get(clave);

				if (tramo != null && tramo.getTipo() == Constantes.COLECTIVO) {
					tiempo += tramo.getTiempo();
					LOGGER.debug(" -> Sumo tramo: {} ({}s). Total: {}s", clave, tramo.getTiempo(), tiempo);
				}
			} else {
				LOGGER.error("Error: Índice fuera de rango en calcularTiempoEntreParadas.");
				break;
			}
		}
		LOGGER.info("Duración final calculada: {} segundos.", tiempo);
		return tiempo;
	}

	/**
	 * Busca conexiones únicamente entre colectivos (Bus-Bus).
	 * 
	 * @param paradaOrigen
	 * @param paradaDestino
	 * @param diaSemana
	 * @param horaLlegaParada
	 * @param tramos
	 * @param todosLosResultados
	 */
	private void buscarConexionBusBus(Parada paradaOrigen, Parada paradaDestino, int diaSemana,
			LocalTime horaLlegaParada, Map<String, Tramo> tramos, List<List<Recorrido>> todosLosResultados) {

		LOGGER.debug("Buscando viajes con conexión de {} a {}.", paradaOrigen.getCodigo(), paradaDestino.getCodigo());
		Set<String> combinacionesEncontradas = new HashSet<>();

		for (Linea lineaA : lineasDelSistema.values()) {
			List<Parada> paradasA = lineaA.getParadas();
			int idxOrigenA = paradasA.indexOf(paradaOrigen);

			if (idxOrigenA != -1) {
				for (int i = idxOrigenA + 1; i < paradasA.size(); i++) {
					Parada paradaTransbordo = paradasA.get(i);

					for (Linea lineaB : lineasDelSistema.values()) {
						if (!lineaA.equals(lineaB)) {
							List<Parada> paradasB = lineaB.getParadas();
							int idxTransbordoB = paradasB.indexOf(paradaTransbordo);
							int idxDestinoB = paradasB.indexOf(paradaDestino);

							if (idxTransbordoB != -1 && idxDestinoB != -1 && idxTransbordoB < idxDestinoB) {
								String combinacion = lineaA.getCodigo() + "->" + lineaB.getCodigo();

								if (!combinacionesEncontradas.contains(combinacion)) {
									Recorrido tramo1 = calcularTramoDeViaje(lineaA, diaSemana, idxOrigenA, i,
											horaLlegaParada, tramos);

									if (tramo1 != null) {
										LocalTime horaLlegadaTransbordo = tramo1.getHoraSalida()
												.plusSeconds(tramo1.getDuracion());

										Recorrido tramo2 = calcularTramoDeViaje(lineaB, diaSemana, idxTransbordoB,
												idxDestinoB, horaLlegadaTransbordo, tramos);

										if (tramo2 != null) {
											todosLosResultados.add(List.of(tramo1, tramo2));
											combinacionesEncontradas.add(combinacion);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Busca conexiones Bus-Caminando-Bus.
	 * 
	 * @param paradaOrigen
	 * @param paradaDestino
	 * @param diaSemana
	 * @param horaLlegaParada
	 * @param tramos
	 * @param todosLosResultados
	 */
	private void buscarConexionCaminando(Parada paradaOrigen, Parada paradaDestino, int diaSemana,
			LocalTime horaLlegaParada, Map<String, Tramo> tramos, List<List<Recorrido>> todosLosResultados) {

		LOGGER.debug("Buscando viajes con conexión caminando de {} a {}.", paradaOrigen.getCodigo(),
				paradaDestino.getCodigo());
		Set<String> combinacionesEncontradas = new HashSet<>();

		for (Linea lineaA : lineasDelSistema.values()) {
			List<Parada> paradasA = lineaA.getParadas();
			int idxOrigenA = paradasA.indexOf(paradaOrigen);

			if (idxOrigenA != -1) {
				for (int i = idxOrigenA + 1; i < paradasA.size(); i++) {
					Parada paradaBajada = paradasA.get(i);

					for (Tramo tramoCaminando : tramos.values()) {
						if (tramoCaminando.getInicio().equals(paradaBajada)
								&& tramoCaminando.getTipo() == Constantes.CAMINANDO) {

							Parada paradaFinCaminata = tramoCaminando.getFin();

							for (Linea lineaC : lineasDelSistema.values()) {
								List<Parada> paradasC = lineaC.getParadas();
								int idxOrigenC = paradasC.indexOf(paradaFinCaminata);
								int idxDestinoC = paradasC.indexOf(paradaDestino);

								if (idxOrigenC != -1 && idxDestinoC != -1 && idxOrigenC < idxDestinoC) {
									String combinacion = lineaA.getCodigo() + "->CAMINANDO->" + lineaC.getCodigo();

									if (!combinacionesEncontradas.contains(combinacion)) {
										Recorrido tramo1 = calcularTramoDeViaje(lineaA, diaSemana, idxOrigenA, i,
												horaLlegaParada, tramos);

										if (tramo1 != null) {
											LocalTime horaLlegadaBajada = tramo1.getHoraSalida()
													.plusSeconds(tramo1.getDuracion());

											Recorrido tramoCaminata = new Recorrido(null,
													List.of(paradaBajada, paradaFinCaminata), horaLlegadaBajada,
													tramoCaminando.getTiempo());
											LocalTime horaFinCaminata = horaLlegadaBajada
													.plusSeconds(tramoCaminando.getTiempo());

											Recorrido tramo3 = calcularTramoDeViaje(lineaC, diaSemana, idxOrigenC,
													idxDestinoC, horaFinCaminata, tramos);

											if (tramo3 != null) {
												todosLosResultados.add(List.of(tramo1, tramoCaminata, tramo3));
												combinacionesEncontradas.add(combinacion);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Método ayudante para calcular un solo tramo de viaje. Encuentra el primer
	 * colectivo disponible para un segmento.
	 * 
	 * @param linea
	 * @param diaSemana
	 * @param idxInicio
	 * @param idxFin
	 * @param horaMinima
	 * @param tramos
	 * @return el recorrido encontrado.
	 */
	private Recorrido calcularTramoDeViaje(Linea linea, int diaSemana, int idxInicio, int idxFin, LocalTime horaMinima,
			Map<String, Tramo> tramos) {

		LOGGER.debug("Calculando tramo de viaje con conexión caminando de {} a {}.", idxInicio, idxFin);
		int tiempoHastaInicioTramo = calcularTiempoEntreParadas(linea.getParadas(), 0, idxInicio, tramos);

		Iterator<Linea.Frecuencia> iter = linea.getFrecuencias().iterator();
		Recorrido recorridoEncontrado = null;

		while (iter.hasNext() && recorridoEncontrado == null) {
			Linea.Frecuencia frecuencia = iter.next();
			if (frecuencia.getDiaSemana() == diaSemana) {
				LocalTime horaPasoPorInicio = frecuencia.getHora().plusSeconds(tiempoHastaInicioTramo);
				if (!horaPasoPorInicio.isBefore(horaMinima)) {
					int duracionTramo = calcularTiempoEntreParadas(linea.getParadas(), idxInicio, idxFin, tramos);
					if (idxFin + 1 <= linea.getParadas().size()) {
						List<Parada> paradasTramo = linea.getParadas().subList(idxInicio, idxFin + 1);
						recorridoEncontrado = new Recorrido(linea, new ArrayList<>(paradasTramo), horaPasoPorInicio,
								duracionTramo);
					}
				}
			}
		}

		return recorridoEncontrado;
	}
}