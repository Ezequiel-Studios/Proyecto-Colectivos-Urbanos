package colectivo.dao.secuencial;

import colectivo.dao.LineaDAO;
import colectivo.dao.ParadaDAO;
import colectivo.datos.CargarParametros;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;

import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Collections;
import java.util.LinkedHashMap;

public class LineaDAOArchivo implements LineaDAO {

	private String rutaArchivo;
	private final Map<Integer, Parada> paradasDisponibles;
    private Map<String, Linea> lineasMap;
    private boolean actualizar;

	public LineaDAOArchivo(String rutaArchivo) {
		this.rutaArchivo = rutaArchivo;
        this.paradasDisponibles = cargarParadas();
        this.lineasMap = new LinkedHashMap<>();
        this.actualizar = true;
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
        if (actualizar) {
            this.lineasMap = leerDelArchivo();
            this.actualizar = false;
        }
        return this.lineasMap;
    }
	
	private Map<String, Linea> leerDelArchivo() {
        Map<String, Linea> lineas = new LinkedHashMap<>();

        // --- PARTE 1: Leer linea.txt para crear las líneas base ---
        try (BufferedReader br = new BufferedReader(new FileReader(this.rutaArchivo))) {
            String lineaTexto;
            while ((lineaTexto = br.readLine()) != null) {
                if (lineaTexto.trim().isEmpty()) continue;
                
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
            return Collections.emptyMap();
        }

        // --- PARTE 2: Leer frecuencia.txt para añadir horarios ---
        String rutaFrecuencias = CargarParametros.getArchivoFrecuencia();
        try (BufferedReader br = new BufferedReader(new FileReader(rutaFrecuencias))) {
            String lineaTexto;
            while ((lineaTexto = br.readLine()) != null) {
                if (lineaTexto.trim().isEmpty()) continue;
                
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
            System.err.println("Error al leer o procesar el archivo de frecuencias: " + rutaFrecuencias);
            e.printStackTrace();
        }
        
        return lineas;
    }
	
	/**
     * Método privado que se encarga de obtener la dependencia (el mapa de paradas).
     * @return El mapa de paradas cargado.
     */
    private Map<Integer, Parada> cargarParadas() {
        String rutaParadas = CargarParametros.getArchivoParada();
        if(rutaParadas == null) {
            System.err.println("Error: La ruta del archivo de paradas no está configurada.");
            return Collections.emptyMap();
        }
        ParadaDAO paradaDAO = new ParadaDAOArchivo(rutaParadas);
        return paradaDAO.buscarTodos();
    }

	public String getRutaArchivo() {
		return rutaArchivo;
	}
}
