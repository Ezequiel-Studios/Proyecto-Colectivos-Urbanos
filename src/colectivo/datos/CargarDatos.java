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
	
			try (BufferedReader br = new BufferedReader(new FileReader(nombreArchivo))) {
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
			}
			return paradas;
		}
	
		public static Map<String, Tramo> cargarTramos(String nombreArchivo, Map<Integer, Parada> paradas)
		        throws FileNotFoundException {
		    Map<String, Tramo> tramos = new TreeMap<>();
	
		    try (BufferedReader br = new BufferedReader(new FileReader(nombreArchivo))) {
		        String lineaTexto;
		        while ((lineaTexto = br.readLine()) != null) {
		            if (lineaTexto.trim().isEmpty())
		                continue;
	
		            String partes[] = lineaTexto.split(";");
		            int codigoInicio = Integer.parseInt(partes[0].trim());
		            int codigoFin = Integer.parseInt(partes[1].trim());
		            int tiempo = Integer.parseInt(partes[2].trim());
		            int tipo = Integer.parseInt(partes[3].trim());
	
		            Parada inicio = paradas.get(codigoInicio);
		            Parada fin = paradas.get(codigoFin);
	
		            if (inicio != null && fin != null) {
		                Tramo tramo = new Tramo(inicio, fin, tiempo, tipo);
		                String key = codigoInicio + "-" + codigoFin;
		                tramos.put(key, tramo);
		            }
		        }
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
	
		    return tramos;
		}
	
	
		public static Map<String, Linea> cargarLineas(String nombreArchivo, String nombreArchivoFrecuencia,
				Map<Integer, Parada> paradas) throws FileNotFoundException {
			Map<String, Linea> lineas = new TreeMap<>();
	
			try (BufferedReader br = new BufferedReader(new FileReader(nombreArchivo))) {
				String lineaTexto;
				while ((lineaTexto = br.readLine()) != null) {
					if (lineaTexto.trim().isEmpty())
						continue;
	
					String[] partes = lineaTexto.split(";");
					String codigo = partes[0].trim();
					String nombreLinea = partes[1].trim();
					Linea linea = new Linea(codigo, nombreLinea);
	
					for (int i = 2; i < partes.length; i++) {
						int codigoParada = Integer.parseInt(partes[i].trim());
						Parada parada = paradas.get(codigoParada);
						if (parada != null) {
							linea.agregarParada(parada);
							parada.agregarLinea(linea);
						}
					}
	
					lineas.put(linea.getCodigo(), linea);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
	
			return lineas;
		}
	}
