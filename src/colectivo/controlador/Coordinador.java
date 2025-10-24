package colectivo.controlador;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import colectivo.interfaz.ControladorInterfaz;
import colectivo.logica.Calculo;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.modelo.Tramo;

public class Coordinador {

    private Calculo calculo;
    private ControladorInterfaz ControladorInterfaz;
    
    private List<Parada> paradas;
    private List<Linea> lineas;
    private List<Recorrido> recorridos;
    private Map<String, Tramo> tramos;

    public void setCalculo(Calculo calculo) { this.calculo = calculo; }
    public void setInterfaz(ControladorInterfaz ControladorInterfaz) { this.ControladorInterfaz = ControladorInterfaz; }
    
    public void setParadas(List<Parada> paradas) { this.paradas = paradas; }
    public void setLineas(List<Linea> lineas) { this.lineas = lineas; }
    public void setRecorridos(List<Recorrido> recorridos) { this.recorridos = recorridos; }
    public void setTramos(Map<String, Tramo> tramos) { this.tramos = tramos; }

    /**
     * Calls the method responsible for calculating possible routes 
     * between two bus stops given the day and time.
     * @param origen     the origin bus stop
     * @param destino    the destination bus stop
     * @param diaSemana  the day of the week
     * @param hora       the departure time
     * @return a list of all the possible routes that match the
     * needs of the user, represented as lists.
     * */
    public List<List<Recorrido>> calcularRecorrido(Parada origen, Parada destino, int diaSemana, LocalTime hora){
    	return calculo.calcularRecorrido(origen, destino, diaSemana, hora, tramos);
    }

    /**
     * Returns the list of all bus lines loaded.
     * @return a list with all the lines.
     * */
    public List<Linea> getLineas() {
        return lineas;
    }

    /**
     * Returns the list of all bus stops loaded.
     * @return a list with all the stops.
     * */
    public List<Parada> getParadas() {
        return paradas;
    }

    /**
     * Returns the map with all route segments loaded.
     * @return a map with the route segments.
     * */
    public Map<String, Tramo> getTramos() {
        return tramos;
    }

}