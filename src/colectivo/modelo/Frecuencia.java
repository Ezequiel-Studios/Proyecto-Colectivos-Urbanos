package colectivo.modelo;

import java.time.LocalTime;

public class Frecuencia {
    private int diaSemana;  // 1=Lunes ... 7=Domingo
    private int tipo;       // 0=normal, 1=feriado
    private int tiempo;     // tiempo entre colectivos (min)
    private LocalTime hora; // hora del primer servicio

    public Frecuencia(int diaSemana, int tipo, int tiempo, LocalTime hora) {
        this.diaSemana = diaSemana;
        this.tipo = tipo;
        this.tiempo = tiempo;
        this.hora = hora;
    }

    public int getDiaSemana() { return diaSemana; }
    public int getTipo() { return tipo; }
    public int getTiempo() { return tiempo; }
    public LocalTime getHora() { return hora; }

    @Override
    public String toString() {
        return "Frecuencia{" +
                "diaSemana=" + diaSemana +
                ", tipo=" + tipo +
                ", tiempo=" + tiempo +
                ", hora=" + hora +
                '}';
    }
}
