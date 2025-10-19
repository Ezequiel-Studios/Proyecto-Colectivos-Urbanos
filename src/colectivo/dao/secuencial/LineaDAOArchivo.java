package colectivo.dao.secuencial;

import colectivo.dao.LineaDAO;
import colectivo.datos.CargarDatos;
import colectivo.datos.CargarParametros;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;

import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;

public class LineaDAOArchivo implements LineaDAO {

	private String rutaArchivo;

	public LineaDAOArchivo(String rutaArchivo) {
		this.rutaArchivo = rutaArchivo;
	}

	@Override
	public void insertar(Linea linea) {
		// Implementar escritura en archivo
	}

	@Override
	public void actualizar(Linea linea) {
		// Actualizar registro en archivo
	}

	@Override
	public void borrar(Linea linea) {
		// Borrar línea correspondiente en archivo
	}

	@Override
	public Map<String, Linea> buscarTodos() {
		Map<String, Linea> lineas = new HashMap<>();

		Map<Integer, Parada> paradasMap = CargarDatos.getParadasCargadas();
		if (paradasMap == null || paradasMap.isEmpty()) {
			System.err.println("Error: Las paradas deben ser cargadas antes que las líneas.");
			return Collections.emptyMap();
		}

		// Leer archivo y cargar las líneas al mapa
		try (BufferedReader br = new BufferedReader(new FileReader(this.rutaArchivo))) {
			String lineaTexto;
			while ((lineaTexto = br.readLine()) != null) {
				if (lineaTexto.trim().isEmpty()) {
					continue;
				}
				String[] partes = lineaTexto.split(";");
				String codigo = partes[0].trim();
				String nombreLinea = partes[1].trim();

				Linea linea = new Linea(codigo, nombreLinea);

				// 3. Recorremos los códigos de parada para agregarlos a la línea
				for (int i = 2; i < partes.length; i++) {
					int codigoParada = Integer.parseInt(partes[i].trim());
					Parada parada = paradasMap.get(codigoParada); // Buscamos el objeto Parada
					if (parada != null) {
						linea.agregarParada(parada);
					}
				}
				lineas.put(linea.getCodigo(), linea);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return Collections.emptyMap();
		}

		String rutaArchivoFrecuencias = CargarParametros.getArchivoFrecuencia();

		try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivoFrecuencias))) {
			String lineaTexto;
			while ((lineaTexto = br.readLine()) != null) {
				if (lineaTexto.trim().isEmpty()) {
					continue;
				}
				String[] partes = lineaTexto.split(";");
				String codigoLinea = partes[0].trim();
				int diaSemana = Integer.parseInt(partes[1].trim());
				LocalTime hora = LocalTime.parse(partes[2].trim());

				// 6. Buscamos la línea que ya creamos en la Parte 1 y le añadimos la frecuencia
				Linea lineaExistente = lineas.get(codigoLinea);
				if (lineaExistente != null) {
					lineaExistente.agregarFrecuencia(diaSemana, hora);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return lineas;

	}

	public String getRutaArchivo() {
		return rutaArchivo;
	}
}
