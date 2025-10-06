package colectivo.modelo;

import java.util.List;

public class Linea {
    private String codigo;
    private String nombre;
    private List<Tramo> tramos;
    private List<Frecuencia> frecuencias;

    public Linea(String codigo, String nombre, List<Tramo> tramos, List<Frecuencia> frecuencias) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.tramos = tramos;
        this.frecuencias = frecuencias;
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public List<Tramo> getTramos() { return tramos; }
    public List<Frecuencia> getFrecuencias() { return frecuencias; }

    @Override
    public String toString() {
        return "Linea{" +
                "codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", tramos=" + tramos +
                ", frecuencias=" + frecuencias +
                '}';
    }
}
