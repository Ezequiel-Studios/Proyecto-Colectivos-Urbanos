package colectivo.dao.secuencial;

import colectivo.conexion.Factory;
import colectivo.dao.ParadaDAO;
import colectivo.dao.TramoDAO;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;
import java.util.Map;
import java.util.Properties;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;

public class TramoDAOArchivo implements TramoDAO {

	private String rutaArchivo;
	private Map<Integer, Parada> paradasDisponibles;
	private Map<String, Tramo> tramosMap;
	private boolean actualizar;

	public TramoDAOArchivo() {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream("config.properties");
			prop.load(input);
			// Busca la propiedad "tramo"
			this.rutaArchivo = prop.getProperty("tramo");
			if (this.rutaArchivo == null) {
				System.err.println("Error crítico: La clave 'tramo' no se encontró en config.properties.");
			}
		} catch (IOException ex) {
			System.err.println("Error crítico: No se pudo leer el archivo config.properties en TramoDAO.");
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
		this.tramosMap = new LinkedHashMap<>();
		this.actualizar = true;
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
		if (this.rutaArchivo == null) {
			System.err.println("Error: No se puede buscar tramos porque la ruta del archivo es nula.");
			return Collections.emptyMap();
		}
		if (actualizar) {
			this.tramosMap = leerDelArchivo(this.rutaArchivo);
			this.actualizar = false;
		}
		return this.tramosMap;
	}

	private Map<String, Tramo> leerDelArchivo(String ruta) {
		Map<String, Tramo> tramos = new LinkedHashMap<>();

		if (paradasDisponibles == null || paradasDisponibles.isEmpty()) {
			System.err.println("Error: No se pueden cargar los tramos sin las paradas. El mapa de paradas está vacío.");
			return Collections.emptyMap();
		}

		try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
			String linea;
			while ((linea = br.readLine()) != null) {
				if (linea.trim().isEmpty())
					continue;

				String[] partes = linea.split(";");
				int codigoInicio = Integer.parseInt(partes[0].trim());
				int codigoFin = Integer.parseInt(partes[1].trim());
				int tiempo = Integer.parseInt(partes[2].trim());
				int tipo = Integer.parseInt(partes[3].trim());

				Parada paradaInicio = this.paradasDisponibles.get(codigoInicio);
				Parada paradaFin = this.paradasDisponibles.get(codigoFin);

				if (paradaInicio != null && paradaFin != null) {
					Tramo tramo = new Tramo(paradaInicio, paradaFin, tiempo, tipo);
					String clave = codigoInicio + "-" + codigoFin;
					tramos.put(clave, tramo);
				}
			}
		} catch (IOException | NumberFormatException e) {
			e.printStackTrace();
			return Collections.emptyMap();
		}
		return tramos;
	}

	/**
	 * Método privado que se encarga de obtener la dependencia (el mapa de paradas).
	 * 
	 * @return El mapa de paradas cargado.
	 */
	private Map<Integer, Parada> cargarParadas() {
		try {
			// Usa la Factory para obtener el ParadaDAO
			ParadaDAO paradaDAO = (ParadaDAO) Factory.getInstancia("PARADA");
			return paradaDAO.buscarTodos();
		} catch (Exception e) {
			System.err.println("Error al obtener ParadaDAO desde la Factory en TramoDAO.");
			e.printStackTrace();
			return Collections.emptyMap();
		}
	}

	public String getRutaArchivo() {
		return rutaArchivo;
	}
}
