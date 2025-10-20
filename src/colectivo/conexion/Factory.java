package colectivo.conexion;

import java.util.Hashtable;
import java.util.ResourceBundle;

public class Factory {
    private static Hashtable<String, Object> instancias = new Hashtable<String, Object>();

    public static Object getInstancia(String objName) {
        try {
            // 1. Verifico si ya existe una instancia de este objeto en la caché.
            Object obj = instancias.get(objName);
            
            // 2. Si no existe, la creo.
            if (obj == null) {
                // Lee el archivo factory.properties
                ResourceBundle rb = ResourceBundle.getBundle("factory");
                // Busca el nombre de la clase asociado a la clave (ej: "paradaDAO")
                String sClassname = rb.getString(objName);
                // Crea una nueva instancia de esa clase usando su nombre
                obj = Class.forName(sClassname).getDeclaredConstructor().newInstance();
                
                // 3. Guardo la nueva instancia en la caché para la próxima vez.
                instancias.put(objName, obj);
            }
            // 4. Devuelvo la instancia (ya sea la encontrada o la recién creada).
            return obj;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
}