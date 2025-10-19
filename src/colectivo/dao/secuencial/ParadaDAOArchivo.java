package colectivo.dao.secuencial;

import colectivo.dao.ParadaDAO;
import colectivo.modelo.Parada;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class ParadaDAOArchivo implements ParadaDAO {

	private String rutaArchivo;

	public ParadaDAOArchivo(String rutaArchivo) {
		this.rutaArchivo = rutaArchivo;
	}

	@Override
	public void insertar(Parada parada) {
		// Implementar escritura en archivo
	}

	@Override
	public void actualizar(Parada parada) {
		// Actualizar registro en archivo
	}

	@Override
	public void borrar(Parada parada) {
		// Borrar línea correspondiente en archivo
	}

	@Override
	public Map<Integer, Parada> buscarTodos() {
		Map<Integer, Parada> paradas = new HashMap<>();
		try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
			String linea;
			while ((linea = br.readLine()) != null) {
				if (linea.trim().isEmpty())
					continue;
				String partes[] = linea.split(";");
				int codigo = Integer.parseInt(partes[0].trim());
				String direccion = partes[1].trim();
				double latitud = Double.parseDouble(partes[2].trim());
				double longitud = Double.parseDouble(partes[3].trim());
				Parada parada = new Parada(codigo, direccion, latitud, longitud);
				paradas.put(codigo, parada);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Leer archivo y cargar las paradas al mapa
		return paradas;
	}

	public String getRutaArchivo() {
		return rutaArchivo;
	}
}
