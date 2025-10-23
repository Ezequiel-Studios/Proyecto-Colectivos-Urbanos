package colectivo.dao.secuencial;

import colectivo.dao.ParadaDAO;
import colectivo.modelo.Parada;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.InputStream;

public class ParadaDAOArchivo implements ParadaDAO {

	private String rutaArchivo;
	private Map<Integer, Parada> paradasMap;
	private boolean actualizar;

	// NUEVO: ctor sin argumentos que lee config.properties
	public ParadaDAOArchivo() {
		Properties prop = new Properties();
		try (InputStream input = new FileInputStream("config.properties")) {
			prop.load(input);
			this.rutaArchivo = prop.getProperty("parada");
			if (this.rutaArchivo == null) {
				System.err.println("Error crítico: La clave 'parada' no se encontró en config.properties.");
			}
		} catch (IOException ex) {
			System.err.println("Error crítico: No se pudo leer el archivo config.properties en ParadaDAO.");
			ex.printStackTrace();
		}
		this.paradasMap = new LinkedHashMap<>();
		this.actualizar = true;
	}

	public ParadaDAOArchivo(String rutaArchivo) {
		this.rutaArchivo = rutaArchivo;
		this.paradasMap = new LinkedHashMap<>();
		this.actualizar = true;
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
		if (actualizar) {
			this.paradasMap = leerDelArchivo(this.rutaArchivo);
			this.actualizar = false;
		}
		return this.paradasMap;
	}

	private Map<Integer, Parada> leerDelArchivo(String ruta) {
		Map<Integer, Parada> paradas = new LinkedHashMap<>();
		try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
			String linea;
			while ((linea = br.readLine()) != null) {
				if (linea.trim().isEmpty()) {
					continue;
				}
				String[] partes = linea.split(";");
				int codigo = Integer.parseInt(partes[0].trim());
				String direccion = partes[1].trim();
				double latitud = Double.parseDouble(partes[2].trim());
				double longitud = Double.parseDouble(partes[3].trim());
				Parada parada = new Parada(codigo, direccion, latitud, longitud);
				paradas.put(codigo, parada);
			}
		} catch (IOException | NumberFormatException e) {
			System.err.println("Error al leer o procesar el archivo de paradas: " + ruta);
			e.printStackTrace();
			return Collections.emptyMap();
		}
		return paradas;
	}

	public String getRutaArchivo() {
		return rutaArchivo;
	}
}