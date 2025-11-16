package colectivo.conexion;

import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Factory pattern implementation. This final class acts as a central factory
 * for creating and managing object instances.
 * 
 * @author Juliana Martin
 * @author Ezequiel Ramos
 * @author Nerea Toledo
 */
public final class Factory {

	/** Internal cache that stores the single instance of each requested object. */
	private static final ConcurrentHashMap<String, Object> INSTANCIAS = new ConcurrentHashMap<>();

	/** Logger instance for logging events, errors and exceptions. */
	private static final Logger LOGGER = LogManager.getLogger(Factory.class);

	/** Private constructor to prevent instantiation. */
	private Factory() {
		throw new AssertionError("No instanciable");
	}

	/**
	 * Retrieves a unique instance of an object by name.
	 * 
	 * @param objName      The key name of the object defined in the
	 *                     "factory.properties" file.
	 * @param expectedType The {@code Class} object representing the expected return
	 *                     type.
	 * @return The single instance of the requested object.
	 * @throws ClassCastException if the created instance does not match the
	 *                            expected type.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getInstancia(String objName, Class<T> expectedType) {

		Object instance = INSTANCIAS.computeIfAbsent(objName, Factory::crearInstancia);

		if (!expectedType.isInstance(instance)) {
			String error = String.format("ERROR DE CONFIGURACIÓN: %s. Esperado: %s, Obtenido: %s", objName,
					expectedType.getName(), instance.getClass().getName());
			LOGGER.fatal(error);
			throw new ClassCastException(error);
		}
		return (T) instance;
	}

	/**
	 * Loads the class name from "factory.properties" using the provided key. Uses
	 * {@code Class.forName()} and reflection to create a new instance.
	 * 
	 * @param clave The key name of the object to be created.
	 * @return A newly created instance of the object.
	 * @throws RuntimeException if the key is not found.
	 */
	private static Object crearInstancia(String clave) {
		try {
			LOGGER.info("Creando instancia para: {}", clave);
			ResourceBundle rb = ResourceBundle.getBundle("factory");

			if (!rb.containsKey(clave)) {
				LOGGER.fatal("Clave no encontrada en factory.properties: {}", clave);
				throw new IllegalArgumentException("Clave no encontrada en factory.properties: " + clave);
			}

			String className = rb.getString(clave);
			Object instance = Class.forName(className).getDeclaredConstructor().newInstance();

			LOGGER.debug("Instancia creada: {} -> {}", clave, className);
			return instance;

		} catch (Exception ex) {
			LOGGER.fatal("Error creando instancia para: {}", clave, ex);
			throw new RuntimeException("Error Factory al crear: " + clave, ex);
		}
	}

	/**
	 * Clears the entire factory cache. Removes all instances, forcing a new
	 * creation via reflection.
	 */
	public static void clearCache() {
		INSTANCIAS.clear();
		LOGGER.info("Cache de Factory limpiado");
	}

	/**
	 * Forces the creation of a new instance for a specific object, replacing the
	 * cached one.
	 * 
	 * @param objName      The key name of the object
	 * @param expectedType The {@code Class} object representing the expected return
	 *                     type.
	 * @return The newly created and cached instance of the requested object.
	 */
	public static <T> T reloadInstancia(String objName, Class<T> expectedType) {
		INSTANCIAS.remove(objName);
		return getInstancia(objName, expectedType);
	}
}