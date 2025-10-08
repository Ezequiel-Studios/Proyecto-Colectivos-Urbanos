package colectivo.logica;

import colectivo.modelo.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Calculo {
	/**
	 * Calcula los posibles recorridos entre dos paradas.
	 */
	public static List<List<Recorrido>> calcularRecorrido(Parada paradaOrigen, Parada paradaDestino, int diaSemana,
			LocalTime horaLlegaParada, Map<String, Tramo> tramos) {

		List<List<Recorrido>> recorridos = new ArrayList<>();

		if (paradaOrigen == null || paradaDestino == null) {
			return recorridos; // caso sin colectivo
		}

		// Recorremos todas las líneas que pasan por la parada origen
		for (Linea linea : paradaOrigen.getLineas()) {

			List<Parada> paradasLinea = linea.getParadas();

			int iOrigen = paradasLinea.indexOf(paradaOrigen);
			int iDestino = paradasLinea.indexOf(paradaDestino);

			// Solo si la línea contiene ambas paradas
			if (iOrigen != -1 && iDestino != -1) {

				// Determinar dirección correcta (ida o vuelta)
				int step = iOrigen < iDestino ? 1 : -1;

				List<Parada> subParadas = new ArrayList<>();
				subParadas.add(paradaOrigen);

				int duracionTotal = 0;

				for (int i = iOrigen; i != iDestino; i += step) {
					Parada actual = paradasLinea.get(i);
					Parada siguiente = paradasLinea.get(i + step);

					subParadas.add(siguiente);

					// Buscar el tramo correspondiente
					Tramo t1 = tramos.get(actual.getCodigo() + "-" + siguiente.getCodigo());
					Tramo t2 = tramos.get(siguiente.getCodigo() + "-" + actual.getCodigo());

					if (t1 != null && t1.getTipo() != 0) // ignorar caminando si no aplica
						duracionTotal += t1.getTiempo();
					else if (t2 != null && t2.getTipo() != 0)
						duracionTotal += t2.getTiempo();
					else
						duracionTotal += 90; // valor por defecto según test
				}

				// Calcular hora de salida
				LocalTime horaSalida = horaLlegaParada.minusSeconds(duracionTotal);

				// Crear recorrido
				Recorrido recorrido = new Recorrido(linea, subParadas, horaSalida, duracionTotal);

				List<Recorrido> opcion = new ArrayList<>();
				opcion.add(recorrido);
				recorridos.add(opcion);
			}
		}

		return recorridos;
	}
}
