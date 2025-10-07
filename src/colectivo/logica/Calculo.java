package colectivo.logica;

import colectivo.modelo.*;
import java.time.LocalTime;
import java.util.*;

public class Calculo {
    
    /**
     * Calcula los posibles recorridos entre dos paradas.
     */
    public static List<List<Recorrido>> calcularRecorrido(
            Parada paradaOrigen,
            Parada paradaDestino,
            int diaSemana,
            LocalTime horaLlegaParada,
            Map<String, Tramo> tramos) {
        
        List<List<Recorrido>> recorridosValidos = new LinkedList<>();
        
        // 1. Reconstruir los recorridos completos a partir de los tramos
        Map<String, Recorrido> recorridosReconstruidos = reconstruirRecorridos(tramos);
        
        // 2. Para cada recorrido, verificar si contiene ambas paradas en orden válido
        for (Recorrido recorrido : recorridosReconstruidos.values()) {
            if (contieneAmbasParadasEnOrdenValido(recorrido, paradaOrigen, paradaDestino) &&
                cumpleHorario(recorrido, diaSemana, horaLlegaParada)) {
                
                List<Recorrido> resultado = new ArrayList<>();
                resultado.add(recorrido);
                recorridosValidos.add(resultado);
            }
        }
        
        return recorridosValidos;
    }
    
    /**
     * Reconstruye los recorridos completos a partir de los tramos
     */
    private static Map<String, Recorrido> reconstruirRecorridos(Map<String, Tramo> tramos) {
        Map<String, List<Tramo>> tramosPorLinea = new HashMap<>();
        
        // Agrupar tramos por línea (usando el tipo como identificador de línea)
        for (Tramo tramo : tramos.values()) {
            String claveLinea = "L" + tramo.getTipo();
            
            List<Tramo> listaTramos = tramosPorLinea.get(claveLinea);
            if (listaTramos == null) {
                listaTramos = new ArrayList<>();
                tramosPorLinea.put(claveLinea, listaTramos);
            }
            
            listaTramos.add(tramo);
        }
        
        // Construir los recorridos completos
        Map<String, Recorrido> recorridos = new HashMap<>();
        
        for (Map.Entry<String, List<Tramo>> entry : tramosPorLinea.entrySet()) {
            String claveLinea = entry.getKey();
            List<Tramo> tramosDelRecorrido = entry.getValue();
            
            // Reconstruir la secuencia de paradas
            List<Parada> paradasOrdenadas = reconstruirSecuenciaParadas(tramosDelRecorrido);
            
            if (!paradasOrdenadas.isEmpty()) {
                // Crear un objeto Recorrido
                Linea linea = new Linea(claveLinea, "Línea " + claveLinea.substring(1));
                LocalTime horaSalida = LocalTime.of(6, 0); // Hora por defecto, se ajustará después
                int duracion = calcularDuracionTotal(tramosDelRecorrido);
                
                Recorrido recorrido = new Recorrido(linea, paradasOrdenadas, horaSalida, duracion);
                recorridos.put(claveLinea, recorrido);
            }
        }
        
        return recorridos;
    }
    
    /**
     * Reconstruye la secuencia ordenada de paradas para un recorrido
     */
    private static List<Parada> reconstruirSecuenciaParadas(List<Tramo> tramos) {
        if (tramos.isEmpty()) return new ArrayList<>();
        
        // Crear un mapa de conexiones: inicio -> fin
        Map<Parada, Parada> conexiones = new HashMap<>();
        Set<Parada> todasParadas = new HashSet<>();
        
        for (Tramo tramo : tramos) {
            conexiones.put(tramo.getInicio(), tramo.getFin());
            todasParadas.add(tramo.getInicio());
            todasParadas.add(tramo.getFin());
        }
        
        // Encontrar la parada inicial (que no es fin de ningún otro tramo)
        Parada inicio = null;
        for (Parada parada : todasParadas) {
            boolean esInicio = true;
            for (Tramo tramo : tramos) {
                if (tramo.getFin().equals(parada)) {
                    esInicio = false;
                    break;
                }
            }
            if (esInicio) {
                inicio = parada;
                break;
            }
        }
        
        // Si no encontramos inicio, usar la primera parada del primer tramo
        if (inicio == null) {
            inicio = tramos.get(0).getInicio();
        }
        
        // Reconstruir la secuencia
        List<Parada> secuencia = new ArrayList<>();
        Parada actual = inicio;
        
        while (actual != null && !secuencia.contains(actual)) {
            secuencia.add(actual);
            actual = conexiones.get(actual);
        }
        
        return secuencia;
    }
    
    /**
     * Calcula la duración total de un recorrido sumando los tiempos de todos los tramos
     */
    private static int calcularDuracionTotal(List<Tramo> tramos) {
        int duracionTotal = 0;
        for (Tramo tramo : tramos) {
            duracionTotal += tramo.getTiempo();
        }
        return duracionTotal;
    }
    
    /**
     * Verifica si un recorrido contiene ambas paradas en un orden válido
     */
    private static boolean contieneAmbasParadasEnOrdenValido(Recorrido recorrido, Parada origen, Parada destino) {
        List<Parada> paradas = recorrido.getParadas();
        int indexOrigen = -1;
        int indexDestino = -1;
        
        for (int i = 0; i < paradas.size(); i++) {
            Parada parada = paradas.get(i);
            if (parada.equals(origen)) {
                indexOrigen = i;
            }
            if (parada.equals(destino)) {
                indexDestino = i;
            }
        }
        
        return indexOrigen != -1 && indexDestino != -1 && indexOrigen < indexDestino;
    }
    
    /**
     * Verifica si el recorrido cumple con el día y horario solicitado
     */
    private static boolean cumpleHorario(Recorrido recorrido, int diaSemana, LocalTime horaLlegaParada) {
        // Por ahora retornamos true, pero aquí puedes implementar la lógica
        // para filtrar por día de la semana y horario según las frecuencias de la línea
        return true;
    }
}