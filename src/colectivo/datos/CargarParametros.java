package colectivo.datos;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CargarParametros {

	private static String archivoLinea;
	private static String archivoParada;
	private static String archivoTramo;
	private static String archivoFrecuencia;

	/**
	 * Carga los parametros del archivo "config.properties"
	 * 
	 * @throws IOException
	 */
	public static void parametros() throws IOException {

		Properties prop = new Properties();
		InputStream input = new FileInputStream("config.properties");
		prop.load(input);
		archivoLinea = prop.getProperty("linea");
		archivoParada = prop.getProperty("parada");
		archivoTramo = prop.getProperty("tramo");
		archivoFrecuencia = prop.getProperty("frecuencia");

	}

	public static String getArchivoLinea() {
		return archivoLinea;
	}

	public static String getArchivoParada() {
		return archivoParada;
	}

	public static String getArchivoTramo() {
		return archivoTramo;
	}

	public static String getArchivoFrecuencia() {
		return archivoFrecuencia;
	}

}
