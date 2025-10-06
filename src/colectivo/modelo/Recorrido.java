package colectivo.modelo;

import java.time.LocalTime;
import java.util.List;

public class Recorrido {
    private String codigo;
    private LocalTime horaSalida;
    private int duracion; // en minutos
    private List<Parada> paradas;

    public Recorrido(String codigo, LocalTime horaSalida, int duracion, List<Parada> paradas) {
        this.codigo = codigo;
        this.horaSalida = horaSalida;
        this.duracion = duracion;
        this.paradas = paradas;
    }

    public String getCodigo() { return codigo; }
    public LocalTime getHoraSalida() { return horaSalida; }
    public int getDuracion() { return duracion; }
    public List<Parada> getParadas() { return paradas; }

    @Override
    public String toString() {
        return "Recorrido{" +
                "codigo='" + codigo + '\'' +
                ", horaSalida=" + horaSalida +
                ", duracion=" + duracion +
                ", paradas=" + paradas +
                '}';
    }
}
