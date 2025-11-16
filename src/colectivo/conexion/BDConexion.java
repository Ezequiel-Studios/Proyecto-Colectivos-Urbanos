package colectivo.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Database connection manager (Singleton pattern). This class is responsible
 * for managing the connection to the database. It implements the Singleton
 * design pattern to ensure that only one instance of the database connection
 * exists throughout the application's lifecycle.
 * 
 * @author Juliana Martin
 * @author Ezequiel Ramos
 * @author Nerea Toledo
 */
public class BDConexion {

	/**
	 * Private static instance of {@code Connection}. Holds the single database
	 * connection for the application.
	 */
	private static Connection con = null;

	/** Logger instance for logging events, errors and exceptions. */
	private static final Logger LOGGER = LogManager.getLogger(BDConexion.class);

	/**
	 * Retrieves the unique instance of the database connection.
	 * 
	 * @return the single active connection object to the database.
	 * @throws RuntimeException if an error occurs during the connection creation.
	 */
	public static Connection getConnection() {
		try {
			if (con == null) {
				Runtime.getRuntime().addShutdownHook(new MiShDwnHook());
				ResourceBundle rb = ResourceBundle.getBundle("jdbc");
				String driver = rb.getString("driver");
				String url = rb.getString("url");
				String usr = rb.getString("usr");
				String pwd = rb.getString("pwd");
				String schema = rb.getString("schema");
				Class.forName(driver);
				con = DriverManager.getConnection(url, usr, pwd);
				Statement statement = con.createStatement();
				try {
					statement.execute("set search_path to '" + schema + "'");
				} finally {
					statement.close();
				}
			}
			return con;
		} catch (Exception ex) {
			LOGGER.fatal("No se pudo crear la conexión a la BD.", ex);
			throw new RuntimeException("Error al crear la conexion", ex);
		}
	}

	/**
	 * Internal class to handle JVM shutdown
	 */
	private static class MiShDwnHook extends Thread {

		/**
		 * Executes the connection closing logic.
		 */
		public void run() {
			try {
				Connection con = BDConexion.getConnection();
				con.close();
				LOGGER.info("La conexión a la BD fue cerrada exitosamente.");
			} catch (Exception ex) {
				LOGGER.error("Error intentando cerrar la conexión a la BD.", ex);
				throw new RuntimeException(ex);
			}
		}
	}
}