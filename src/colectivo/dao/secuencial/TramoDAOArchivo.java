package colectivo.dao.secuencial;

import colectivo.dao.ParadaDAO;
import colectivo.dao.TramoDAO;
import colectivo.datos.CargarParametros;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;

public class TramoDAOArchivo implements TramoDAO {

	private String rutaArchivo;
	private Map<Integer, Parada> paradasDisponibles;
	private Map<String, Tramo> tramosMap;
	private boolean actualizar;

	public TramoDAOArchivo(String rutaArchivo) {
		this.rutaArchivo = rutaArchivo;
		this.paradasDisponibles = cargarParadas(); // Llama al método ayudante
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
		// Obtenemos la ruta desde CargarParametros
		String rutaParadas = CargarParametros.getArchivoParada();
		if (rutaParadas == null) {
			System.err.println("Error: La ruta del archivo de paradas no está configurada en CargarParametros.");
			return Collections.emptyMap();
		}
		// Creamos la instancia del DAO de paradas y le pedimos los datos.
		ParadaDAO paradaDAO = new ParadaDAOArchivo(rutaParadas);
		return paradaDAO.buscarTodos();
	}

	public String getRutaArchivo() {
		return rutaArchivo;
	}
}
