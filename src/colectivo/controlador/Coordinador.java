package colectivo.controlador;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import colectivo.interfaz.InterfazJavaFX;
import colectivo.modelo.*;
import colectivo.logica.Calculo;

public class Coordinador {

    private Calculo calculo;
    private InterfazJavaFX interfazJavaFX;
    
    // Listas directas de tus datos
    private List<Parada> paradas;
    private List<Linea> lineas;
    private List<Recorrido> recorridos;
    private Map<String, Tramo> tramos;

    // Getters y Setters básicos
    public void setCalculo(Calculo calculo) { this.calculo = calculo; }
    public void setInterfaz(InterfazJavaFX interfazJavaFX) { this.interfazJavaFX = interfazJavaFX; }
    
    public void setParadas(List<Parada> paradas) { this.paradas = paradas; }
    public void setLineas(List<Linea> lineas) { this.lineas = lineas; }
    public void setRecorridos(List<Recorrido> recorridos) { this.recorridos = recorridos; }
    public void setTramos(Map<String, Tramo> tramos) { this.tramos = tramos; }

    // Métodos del controlador
    public List<List<Recorrido>> calcularRecorrido(Parada origen, Parada destino, int diaSemana, LocalTime hora){
    	return calculo.calcularRecorrido(origen, destino, diaSemana, hora, tramos);
    }

    public List<Linea> getLineas() {
        return lineas;
    }

    public List<Parada> getParadas() {
        return paradas;
    }
}