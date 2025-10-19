package colectivo.dao.secuencial;

import colectivo.dao.TramoDAO;
import colectivo.datos.CargarDatos;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

public class TramoDAOArchivo implements TramoDAO {

	private String rutaArchivo;

	public TramoDAOArchivo(String rutaArchivo) {
		this.rutaArchivo = rutaArchivo;
	}

	@Override
	public void insertar(Tramo tramo) {
		// Implementar escritura en archivo
	}

	@Override
	public void actualizar(Tramo tramo) {
		// Actualizar registro en archivo
	}

	@Override
	public void borrar(Tramo tramo) {
		// Borrar línea correspondiente en archivo
	}

	@Override
	public Map<String, Tramo> buscarTodos() {
		Map<String, Tramo> tramos = new HashMap<>();
		Map<Integer, Parada> paradasMap = CargarDatos.getParadasCargadas();

		if (paradasMap == null || paradasMap.isEmpty()) {
			System.err.println("Error: Las paradas deben ser cargadas antes que los tramos.");
			return Collections.emptyMap();
		}

		// Leer archivo y cargar los tramos al mapa
		try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
			String lineaTexto;
			while ((lineaTexto = br.readLine()) != null) {
				if (lineaTexto.trim().isEmpty())
					continue;
				String partes[] = lineaTexto.split(";");
				int codigoInicio = Integer.parseInt(partes[0].trim());
				int codigoFin = Integer.parseInt(partes[1].trim());
				int tiempo = Integer.parseInt(partes[2].trim());
				int tipo = Integer.parseInt(partes[3].trim());

				Parada paradaInicio = paradasMap.get(codigoInicio);
				Parada paradaFin = paradasMap.get(codigoFin);

				if (paradaInicio != null && paradaFin != null) {
					Tramo tramo = new Tramo(paradaInicio, paradaFin, tiempo, tipo);
					String key = codigoInicio + "-" + codigoFin;
					tramos.put(key, tramo);

					// Agregamos también el tramo inverso
					Tramo tramoInverso = new Tramo(paradaFin, paradaInicio, tiempo, tipo);
					String keyInverso = codigoFin + "-" + codigoInicio;
					tramos.put(keyInverso, tramoInverso);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return Collections.emptyMap();
		}

		return tramos;
	}

	public String getRutaArchivo() {
		return rutaArchivo;
	}
}
