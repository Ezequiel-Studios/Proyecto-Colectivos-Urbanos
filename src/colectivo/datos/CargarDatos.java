package colectivo.datos;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

public class CargarDatos {

	public static Map<Integer, Parada> cargarParadas(String nombreArchivo) throws IOException {
		Map<Integer, Parada> paradas = new TreeMap<Integer, Parada>();
		
		try(BufferedReader br = new BufferedReader(new FileReader(nombreArchivo))) {
			String linea;
			while((linea = br.readLine()) != null) {
				if(linea.trim().isEmpty()) 
					continue;
				String partes[] = linea.split(";");
				int codigo = Integer.parseInt(partes[0].trim());
	            String direccion = partes[1].trim();
	            double latitud = Double.parseDouble(partes[2].trim());
	            double longitud = Double.parseDouble(partes[3].trim());
	            
	            Parada parada = new Parada(codigo, direccion, latitud, longitud);
	            paradas.put(codigo, parada);
			}
		}
		return paradas;
	}

	public static Map<String, Tramo> cargarTramos(String nombreArchivo, Map<Integer, Parada> paradas)
			throws FileNotFoundException {
		return null;
	}

	public static Map<String, Linea> cargarLineas(String nombreArchivo, String nombreArchivoFrecuencia,
			Map<Integer, Parada> paradas) throws FileNotFoundException {
		return null;
	}

}

