package colectivo.logica;

import colectivo.modelo.*;
import java.time.LocalTime;
import java.util.*;

public class Calculo {

    public static List<List<Recorrido>> calcularRecorrido(
            Parada paradaOrigen,
            Parada paradaDestino,
            int diaSemana,
            LocalTime horaLlegaParada,
            Map<String, Tramo> tramos) {

        List<List<Recorrido>> recorridosValidos = new LinkedList<>();
        if (paradaOrigen == null || paradaDestino == null || tramos == null || tramos.isEmpty()) {
            return recorridosValidos;
        }

        RecorridoBuilderResult[] resultados = reconstruirDesdeOrdenArchivo(paradaOrigen, paradaDestino, tramos);

        for (RecorridoBuilderResult r : resultados) {
            if (r != null && !r.paradas.isEmpty()) {
                
                Linea linea = null;
                Tramo primer = r.primerTramo;
                int lineaNumero = r.lineaNumero;

                // Buscar la línea por número
                linea = buscarLineaPorNumero(lineaNumero, paradaOrigen);

                // Si no se encuentra, buscar en las paradas del recorrido
                if (linea == null && !r.paradas.isEmpty()) {
                    for (Parada p : r.paradas) {
                        linea = buscarLineaPorNumero(lineaNumero, p);
                        if (linea != null) break;
                    }
                }

                // Si aún no se encuentra, crear una línea básica
                if (linea == null) {
                    String codigoFallback = "L" + lineaNumero;
                    linea = new Linea(codigoFallback, "Línea " + codigoFallback);
                }

                // Calcular duración sumando los tiempos de los tramos
                int duracion = 0;
                for (Tramo t : r.tramos) {
                    if (t != null) {
                        duracion += t.getTiempo();
                    }
                }

                // Obtener la hora de salida de las frecuencias de la línea
                LocalTime horaSalida = obtenerHoraSalida(linea, diaSemana, horaLlegaParada, duracion);

                // Si se encontró una hora de salida válida, crear el recorrido
                if (horaSalida != null) {
                    Recorrido recorrido = new Recorrido(linea, r.paradas, horaSalida, duracion);
                    List<Recorrido> lista = new ArrayList<>();
                    lista.add(recorrido);
                    recorridosValidos.add(lista);
                }
            }
        }

        return recorridosValidos;
    }

    /**
     * Busca una línea por número en las líneas asociadas a una parada
     */
    private static Linea buscarLineaPorNumero(int lineaNumero, Parada parada) {
        if (parada == null || parada.getLineas() == null) {
            return null;
        }
        
        // Buscar en todas las líneas de la parada
        for (Linea l : parada.getLineas()) {
            String codigo = l.getCodigo();
            // Verificar si el código coincide con el número de línea (L1I, L1R, L2I, L2R, etc.)
            if (codigo != null && codigo.matches("L" + lineaNumero + "[IR]")) {
                return l;
            }
        }
        return null;
    }

    /**
     * Obtiene la hora de salida más temprana que permita llegar al destino a tiempo
     */
    private static LocalTime obtenerHoraSalida(Linea linea, int diaSemana, LocalTime horaLlegaParada, int duracion) {
        LocalTime mejorHoraSalida = null;
        
        if (linea == null || linea.getFrecuencias() == null) {
            return null;
        }
        
        for (Linea.Frecuencia frecuencia : linea.getFrecuencias()) {
            if (frecuencia.getDiaSemana() == diaSemana) {
                LocalTime horaSalidaCandidata = frecuencia.getHora();
                LocalTime horaLlegadaCalculada = horaSalidaCandidata.plusMinutes(duracion);
                
                // Si llegamos antes o justo a tiempo
                if (!horaLlegadaCalculada.isAfter(horaLlegaParada)) {
                    if (mejorHoraSalida == null || horaSalidaCandidata.isAfter(mejorHoraSalida)) {
                        mejorHoraSalida = horaSalidaCandidata;
                    }
                }
            }
        }
        
        return mejorHoraSalida;
    }

    /**
     * Estructura auxiliar para devolver lo reconstruido a partir del orden del archivo.
     */
    private static class RecorridoBuilderResult {
        List<Parada> paradas;
        List<Tramo> tramos;
        Tramo primerTramo;
        int lineaNumero;
        
        RecorridoBuilderResult(List<Parada> p, List<Tramo> t, Tramo primer, int lineaNumero) {
            this.paradas = p;
            this.tramos = t;
            this.primerTramo = primer;
            this.lineaNumero = lineaNumero;
        }
    }

    /**
     * Recorre los tramos en el orden dado (orden del archivo). Para cada ocurrencia
     * donde encontramos la paradaOrigen en el campo inicio del tramo, intentamos
     * reconstruir la cadena de tramos siguientes hasta encontrar paradaDestino.
     */
    private static RecorridoBuilderResult[] reconstruirDesdeOrdenArchivo(
            Parada origen, Parada destino, Map<String, Tramo> tramos) {

        if (origen == null || destino == null) return new RecorridoBuilderResult[0];
        if (tramos == null || tramos.isEmpty()) return new RecorridoBuilderResult[0];

        Tramo[] arr = tramos.values().toArray(new Tramo[0]);
        List<RecorridoBuilderResult> encontrados = new ArrayList<>();

        for (int i = 0; i < arr.length; i++) {
            Tramo t = arr[i];
            if (t == null) continue;

            if (!t.getInicio().equals(origen)) {
                continue;
            }

            int lineaNumero = t.getTipo();

            List<Parada> paradasReconstruidas = new ArrayList<>();
            List<Tramo> tramosReconstruidos = new ArrayList<>();

            Parada actual = origen;
            paradasReconstruidas.add(actual);

            for (int j = i; j < arr.length; j++) {
                Tramo tj = arr[j];
                if (tj == null) continue;

                if (tj.getTipo() != lineaNumero) {
                    break;
                }

                if (!tj.getInicio().equals(actual)) {
                    break;
                }

                tramosReconstruidos.add(tj);
                Parada siguiente = tj.getFin();
                if (siguiente == null) break;
                paradasReconstruidas.add(siguiente);
                actual = siguiente;

                if (actual.equals(destino)) {
                    Tramo primerTramo = tramosReconstruidos.isEmpty() ? null : tramosReconstruidos.get(0);
                    RecorridoBuilderResult r = new RecorridoBuilderResult(
                            new ArrayList<>(paradasReconstruidas),
                            new ArrayList<>(tramosReconstruidos),
                            primerTramo,
                            lineaNumero
                    );
                    encontrados.add(r);
                    break;
                }
            }
        }

        return encontrados.toArray(new RecorridoBuilderResult[0]);
    }
}