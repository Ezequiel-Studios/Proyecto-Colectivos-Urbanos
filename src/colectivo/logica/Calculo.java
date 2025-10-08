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
        
        // Verificar parámetros nulos
        if (paradaOrigen == null || paradaDestino == null || tramos == null || tramos.isEmpty()) {
            return recorridosValidos;
        }
        
        // 1. Reconstruir los recorridos completos a partir de los tramos
        Map<String, Recorrido> recorridosReconstruidos = reconstruirRecorridos(tramos);
        
        // 2. Para cada recorrido, verificar si contiene ambas paradas en orden válido
        for (Recorrido recorrido : recorridosReconstruidos.values()) {
            if (recorrido != null && 
                contieneAmbasParadasEnOrdenValido(recorrido, paradaOrigen, paradaDestino) &&
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
        Map<String, Recorrido> recorridos = new HashMap<>();
        
        if (tramos == null || tramos.isEmpty()) {
            return recorridos;
        }
        
        // Agrupar tramos por tipo (línea)
        Map<Integer, List<Tramo>> tramosPorTipo = new HashMap<>();
        
        for (Tramo tramo : tramos.values()) {
            if (tramo == null) continue;
            
            int tipo = tramo.getTipo();
            List<Tramo> listaTramos = tramosPorTipo.get(tipo);
            if (listaTramos == null) {
                listaTramos = new ArrayList<>();
                tramosPorTipo.put(tipo, listaTramos);
            }
            listaTramos.add(tramo);
        }
        
        // Construir recorridos para cada tipo
        for (Map.Entry<Integer, List<Tramo>> entry : tramosPorTipo.entrySet()) {
            int tipo = entry.getKey();
            List<Tramo> tramosDelTipo = entry.getValue();
            
            // Reconstruir la secuencia de paradas
            List<Parada> paradasOrdenadas = reconstruirSecuenciaParadas(tramosDelTipo);
            
            if (!paradasOrdenadas.isEmpty()) {
                // Crear línea
                String codigoLinea = "L" + tipo;
                Linea linea = new Linea(codigoLinea, "Línea " + tipo);
                
                // Calcular duración total
                int duracion = calcularDuracionTotal(tramosDelTipo);
                
                // Hora de salida por defecto (se ajustaría con frecuencias reales)
                LocalTime horaSalida = LocalTime.of(6, 0);
                
                // Crear recorrido
                Recorrido recorrido = new Recorrido(linea, paradasOrdenadas, horaSalida, duracion);
                recorridos.put(codigoLinea, recorrido);
            }
        }
        
        return recorridos;
    }
    
    /**
     * Reconstruye la secuencia ordenada de paradas para un recorrido
     */
    private static List<Parada> reconstruirSecuenciaParadas(List<Tramo> tramos) {
        List<Parada> secuencia = new ArrayList<>();
        
        if (tramos == null || tramos.isEmpty()) {
            return secuencia;
        }
        
        // Crear mapa de conexiones
        Map<Parada, Parada> conexiones = new HashMap<>();
        Set<Parada> todasParadas = new HashSet<>();
        Set<Parada> paradasFin = new HashSet<>();
        
        for (Tramo tramo : tramos) {
            if (tramo.getInicio() != null && tramo.getFin() != null) {
                conexiones.put(tramo.getInicio(), tramo.getFin());
                todasParadas.add(tramo.getInicio());
                todasParadas.add(tramo.getFin());
                paradasFin.add(tramo.getFin());
            }
        }
        
        // Encontrar parada inicial (que no es fin de ningún tramo)
        Parada inicio = null;
        for (Parada parada : todasParadas) {
            if (!paradasFin.contains(parada)) {
                inicio = parada;
                break;
            }
        }
        
        // Si no se encuentra inicio, usar la primera parada del primer tramo
        if (inicio == null && !tramos.isEmpty()) {
            inicio = tramos.get(0).getInicio();
        }
        
        // Reconstruir secuencia
        if (inicio != null) {
            Parada actual = inicio;
            Set<Parada> visitadas = new HashSet<>();
            
            while (actual != null && !visitadas.contains(actual)) {
                secuencia.add(actual);
                visitadas.add(actual);
                actual = conexiones.get(actual);
            }
        }
        
        return secuencia;
    }
    
    /**
     * Calcula la duración total de un recorrido sumando los tiempos de todos los tramos
     */
    private static int calcularDuracionTotal(List<Tramo> tramos) {
        int duracionTotal = 0;
        if (tramos != null) {
            for (Tramo tramo : tramos) {
                if (tramo != null) {
                    duracionTotal += tramo.getTiempo();
                }
            }
        }
        return duracionTotal;
    }
    
    /**
     * Verifica si un recorrido contiene ambas paradas en un orden válido
     */
    private static boolean contieneAmbasParadasEnOrdenValido(Recorrido recorrido, Parada origen, Parada destino) {
        if (recorrido == null || recorrido.getParadas() == null || origen == null || destino == null) {
            return false;
        }
        
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
        // Implementación simplificada - siempre retorna true por ahora
        // En una implementación real, se verificarían las frecuencias de la línea
        return true;
    }
}