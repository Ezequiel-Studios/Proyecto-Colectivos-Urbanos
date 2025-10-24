package colectivo.dao.secuencial;

import colectivo.conexion.Factory;
import colectivo.dao.LineaDAO;
import colectivo.dao.ParadaDAO;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;

import java.util.Map;
import java.util.Properties;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalTime;
import java.util.Collections;
import java.util.LinkedHashMap;

public class LineaDAOArchivo implements LineaDAO {

	private String rutaArchivo; 
	private String rutaArchivoFrecuencias;
	private final Map<Integer, Parada> paradasDisponibles;
	private Map<String, Linea> lineasMap;
	private boolean actualizar;

	/**
	 * Constructor that loads configuration properties, initizalizes
	 * stops and prepares the structure to store lines.
	 * */
	public LineaDAOArchivo() {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream("config.properties");
			prop.load(input);
			this.rutaArchivo = prop.getProperty("linea");
			this.rutaArchivoFrecuencias = prop.getProperty("frecuencia");

			if (this.rutaArchivo == null || this.rutaArchivoFrecuencias == null) {
				System.err.println("Error crítico: Claves 'linea' o 'frecuencia' no encontradas en config.properties.");
			}
		} catch (IOException ex) {
			System.err.println("Error crítico: No se pudo leer config.properties en LineaDAO.");
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		this.paradasDisponibles = cargarParadas();
		this.lineasMap = new LinkedHashMap<>();
		this.actualizar = true;
	}

	@Override
	public void insertar(Linea linea) {
		
	}

	@Override
	public void actualizar(Linea linea) {
		
	}

	@Override
	public void borrar(Linea linea) {
		
	}

	/**
	 * Returns all bus lines currently loaded.
	 * If the data needs to refreshed, reads them from the
	 * file first.
	 * @return a map containing all loaded line objects.
	 * */
	@Override
	public Map<String, Linea> buscarTodos() {
		if (this.rutaArchivo == null || this.rutaArchivoFrecuencias == null) {
			System.err.println("Error: No se pueden buscar líneas porque las rutas de los archivos son nulas.");
			return Collections.emptyMap();
		}
		if (actualizar) {
			this.lineasMap = leerDelArchivo();
			this.actualizar = false;
		}
		return this.lineasMap;
	}

	/**
	 * Private method responsible for reading the lines, stops and 
	 * frequencies from their files.
	 * @return a map containing all line objects with their associated stops 
	 * and frequencies.
	 * */
	private Map<String, Linea> leerDelArchivo() {
		Map<String, Linea> lineas = new LinkedHashMap<>();

		if (this.paradasDisponibles == null || this.paradasDisponibles.isEmpty()) {
			System.err.println("Error: No se pudieron cargar las paradas necesarias para leer las líneas.");
			return Collections.emptyMap();
		}

		try (BufferedReader br = new BufferedReader(new FileReader(this.rutaArchivo))) {
			String lineaTexto;
			while ((lineaTexto = br.readLine()) != null) {
				if (lineaTexto.trim().isEmpty())
					continue;

				String[] partes = lineaTexto.split(";");
				String codigo = partes[0].trim();
				String nombre = partes[1].trim();
				Linea linea = new Linea(codigo, nombre);

				for (int i = 2; i < partes.length; i++) {
					int codigoParada = Integer.parseInt(partes[i].trim());
					Parada parada = this.paradasDisponibles.get(codigoParada);
					if (parada != null) {
						linea.agregarParada(parada);
					}
				}
				lineas.put(codigo, linea);
			}
		} catch (IOException | NumberFormatException e) {
			System.err.println("Error al leer o procesar el archivo de líneas: " + this.rutaArchivo);
			e.printStackTrace();
		}

		try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivoFrecuencias))) {
			String lineaTexto;
			while ((lineaTexto = br.readLine()) != null) {
				if (lineaTexto.trim().isEmpty())
					continue;

				String[] partes = lineaTexto.split(";");
				String codigoLinea = partes[0].trim();
				int diaSemana = Integer.parseInt(partes[1].trim());
				LocalTime hora = LocalTime.parse(partes[2].trim());

				Linea lineaExistente = lineas.get(codigoLinea);
				if (lineaExistente != null) {
					lineaExistente.agregarFrecuencia(diaSemana, hora);
				}
			}
		} catch (IOException | RuntimeException e) {
			System.err.println("Error al leer o procesar el archivo de frecuencias: " + this.rutaArchivoFrecuencias);
			e.printStackTrace();
		}

		return lineas;
	}

	/**
	 * Private method that loads the map of bus stops.
	 * @return the loaded map of stops.
	 */
	private Map<Integer, Parada> cargarParadas() {
		try {
			ParadaDAO paradaDAO = (ParadaDAO) Factory.getInstancia("PARADA");
			return paradaDAO.buscarTodos();
		} catch (Exception e) {
			System.err.println("Error al obtener ParadaDAO desde la Factory en LineaDAO.");
			e.printStackTrace();
			return Collections.emptyMap();
		}
	}

	public String getRutaArchivo() {
		return rutaArchivo;
	}
}