package colectivo.logica;

import colectivo.modelo.*;
import java.time.LocalTime;
import java.util.*;
import colectivo.interfaz.*;
public class Calculo {

	/**
	 * Calcula los posibles recorridos entre dos paradas.
	 */
	public static List<List<Recorrido>> calcularRecorrido(Parada paradaOrigen, Parada paradaDestino, int diaSemana,
			LocalTime horaLlegaParada, Map<String, Tramo> tramos) {

		List<List<Recorrido>> recorridosValidos = new LinkedList<>();

		// Verificar parámetros nulos
		if (paradaOrigen == null || paradaDestino == null || tramos == null) {
			return recorridosValidos;
		}

		// Obtener las líneas que pasan por la parada de origen
		List<Linea> lineasOrigen = paradaOrigen.getLineas();

		if (lineasOrigen == null || lineasOrigen.isEmpty()) {
			return recorridosValidos;
		}

		/* 
		Para cada línea que pasa por el origen
		System.out.println("\n=== Analizando recorrido de " + paradaOrigen + " a " + paradaDestino + " ===");
		System.out.println("Día: " + diaSemana + ", Hora llegada: " + horaLlegaParada);
		System.out.println("Líneas en origen: " + lineasOrigen.size());
		*/
		// Para cada línea que pasa por el origen
		for (Linea linea : lineasOrigen) {
			// Verificar si la línea también pasa por el destino
			if (pasaPorParada(linea, paradaDestino)) {
				// Verificar que el orden sea correcto (origen antes que destino)
				if (contieneAmbasParadasEnOrdenValido(linea, paradaOrigen, paradaDestino)) {
					// Obtener las paradas del recorrido entre origen y destino
					List<Parada> paradasRecorrido = obtenerParadasEntreOrigenYDestino(linea, paradaOrigen,
							paradaDestino);

					// Calcular la duración del recorrido
					int duracion = calcularDuracionRecorrido(paradasRecorrido, tramos, linea);

					// Obtener hora de salida válida para este día y hora
					LocalTime horaSalida = obtenerProximaHoraSalida(linea, diaSemana, horaLlegaParada, paradaOrigen,
							tramos);

					if (horaSalida != null) {
						// Crear el recorrido
						Recorrido recorrido = new Recorrido(linea, paradasRecorrido, horaSalida, duracion);

						// Agregar a la lista (cada recorrido es una lista de un solo elemento)
						List<Recorrido> listaRecorrido = new ArrayList<>();
						listaRecorrido.add(recorrido);
						recorridosValidos.add(listaRecorrido);
					}
				}
			}
		}
		Interfaz.resultado(recorridosValidos, paradaOrigen, paradaDestino, horaLlegaParada);
		return recorridosValidos;
	}

	/**
	 * Verifica si una línea pasa por una parada específica
	 */
	private static boolean pasaPorParada(Linea linea, Parada parada) {
		if (linea == null || linea.getParadas() == null || parada == null) {
			return false;
		}

		return linea.getParadas().contains(parada);
	}

	/**
	 * Verifica si una línea contiene ambas paradas en un orden válido
	 */
	private static boolean contieneAmbasParadasEnOrdenValido(Linea linea, Parada origen, Parada destino) {
		if (linea == null || linea.getParadas() == null || origen == null || destino == null) {
			return false;
		}

		List<Parada> paradas = linea.getParadas();
		int indexOrigen = paradas.indexOf(origen);
		int indexDestino = paradas.indexOf(destino);

		return indexOrigen != -1 && indexDestino != -1 && indexOrigen < indexDestino;
	}

	/**
	 * Obtiene las paradas del recorrido entre origen y destino (inclusive)
	 */
	private static List<Parada> obtenerParadasEntreOrigenYDestino(Linea linea, Parada origen, Parada destino) {
		List<Parada> resultado = new ArrayList<>();

		if (linea == null || linea.getParadas() == null) {
			return resultado;
		}

		List<Parada> todasParadas = linea.getParadas();
		int indexOrigen = todasParadas.indexOf(origen);
		int indexDestino = todasParadas.indexOf(destino);

		if (indexOrigen != -1 && indexDestino != -1 && indexOrigen < indexDestino) {
			// Agregar todas las paradas desde origen hasta destino (inclusive)
			for (int i = indexOrigen; i <= indexDestino; i++) {
				resultado.add(todasParadas.get(i));
			}
		}

		return resultado;
	}

	/**
	 * Calcula la duración del recorrido sumando los tiempos de los tramos
	 */
	private static int calcularDuracionRecorrido(List<Parada> paradas, Map<String, Tramo> tramos, Linea linea) {
		int duracionTotal = 0;

		if (paradas == null || paradas.size() < 2 || tramos == null) {
			return duracionTotal;
		}

		// Sumar los tiempos de los tramos consecutivos
		for (int i = 0; i < paradas.size() - 1; i++) {
			Parada inicio = paradas.get(i);
			Parada fin = paradas.get(i + 1);

			// Buscar el tramo correspondiente
			String claveTramo = inicio.getCodigo() + "-" + fin.getCodigo();
			Tramo tramo = tramos.get(claveTramo);

			// Solo sumar si el tramo existe y es de tipo colectivo (tipo 1)
			if (tramo != null && tramo.getTipo() == 1) {
				duracionTotal += tramo.getTiempo();
			}
		}

		return duracionTotal;
	}

	/**
	 * Obtiene la próxima hora de salida válida para una línea en un día específico
	 */
	private static LocalTime obtenerProximaHoraSalida(Linea linea, int diaSemana, LocalTime horaLlegaParada,
			Parada paradaOrigen, Map<String, Tramo> tramos) {
		if (linea == null || horaLlegaParada == null) {
			return null;
		}

		List<?> frecuencias = linea.getFrecuencias();
		if (frecuencias == null || frecuencias.isEmpty()) {
			return null;
		}

		// Calcular tiempo desde inicio de línea hasta paradaOrigen
		int tiempoHastaOrigen = calcularTiempoHastaParada(linea, paradaOrigen, tramos);

		LocalTime mejorHora = null;

		try {
			for (Object frecuenciaObj : frecuencias) {
				Class<?> frecuenciaClass = frecuenciaObj.getClass();
				java.lang.reflect.Method getDiaSemana = frecuenciaClass.getMethod("getDiaSemana");
				java.lang.reflect.Method getHora = frecuenciaClass.getMethod("getHora");

				int dia = (int) getDiaSemana.invoke(frecuenciaObj);
				LocalTime horaSalidaInicio = (LocalTime) getHora.invoke(frecuenciaObj);

				if (dia == diaSemana) {
					// Calcular hora de llegada a paradaOrigen
					LocalTime horaLlegadaOrigen = horaSalidaInicio.plusSeconds(tiempoHastaOrigen);

					if (!horaLlegadaOrigen.isBefore(horaLlegaParada)) {
						if (mejorHora == null || horaLlegadaOrigen.isBefore(mejorHora)) {
							mejorHora = horaLlegadaOrigen;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return mejorHora;
	}

	private static int calcularTiempoHastaParada(Linea linea, Parada paradaDestino, Map<String, Tramo> tramos) {
		List<Parada> paradas = linea.getParadas();
		int tiempoAcumulado = 0;

		for (int i = 0; i < paradas.size() - 1; i++) {
		    Parada inicio = paradas.get(i);
		    Parada fin = paradas.get(i + 1);
		    String clave = inicio.getCodigo() + "-" + fin.getCodigo();
		    Tramo tramo = tramos.get(clave);

		    if (tramo != null && tramo.getTipo() == 1) {
		        tiempoAcumulado += tramo.getTiempo();
		    }

		    if (fin.equals(paradaDestino)) {
		        break;
		    }
		}

		return tiempoAcumulado;
	}
}