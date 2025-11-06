package colectivo.test;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import colectivo.conexion.Factory;
import colectivo.dao.LineaDAO;
import colectivo.dao.ParadaDAO;
import colectivo.dao.TramoDAO;
import colectivo.logica.Calculo;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.modelo.Tramo;

class TestCalcularRecorridoDAO_GP {

	private Map<Integer, Parada> paradas;
	private Map<String, Linea> lineas;
	private Map<String, Tramo> tramos;

	private Calculo calculo;

	@BeforeEach
	void setUp() throws Exception {
		paradas = ((ParadaDAO) Factory.getInstancia("PARADA")).buscarTodos();
		tramos = ((TramoDAO) Factory.getInstancia("TRAMO")).buscarTodos();
		lineas = ((LineaDAO) Factory.getInstancia("LINEA")).buscarTodos();

		calculo = new Calculo();
	}

	@Test
	void testSinColectivo() {
		Parada paradaOrigen = paradas.get(91);
		Parada paradaDestino = paradas.get(26);
		int diaSemana = 7; // Domingo
		LocalTime horaLlegaParada = LocalTime.of(21, 00);

		List<List<Recorrido>> recorridos = calculo.calcularRecorrido(paradaOrigen, paradaDestino, diaSemana,
				horaLlegaParada, tramos);

		assertTrue(recorridos.isEmpty());
	}

	@Test
	void testDirecto() {
		Parada paradaOrigen = paradas.get(4);
		Parada paradaDestino = paradas.get(9);
		int diaSemana = 1;
		LocalTime horaLlegaParada = LocalTime.of(10, 30);

		List<List<Recorrido>> recorridos = calculo.calcularRecorrido(paradaOrigen, paradaDestino, diaSemana,
				horaLlegaParada, tramos);

		assertEquals(1, recorridos.size());
		assertEquals(1, recorridos.get(0).size());

		Recorrido recorrido1 = recorridos.get(0).get(0);

		assertEquals(lineas.get("L1"), recorrido1.getLinea());
		List<Parada> paradas1 = new ArrayList<Parada>();
		paradas1.add(paradas.get(4));
		paradas1.add(paradas.get(5));
		paradas1.add(paradas.get(6));
		paradas1.add(paradas.get(7));
		paradas1.add(paradas.get(8));
		paradas1.add(paradas.get(9));
		assertIterableEquals(paradas1, recorrido1.getParadas());

		assertEquals(LocalTime.of(10, 32, 0), recorrido1.getHoraSalida());
		assertEquals(210, recorrido1.getDuracion());
	}

	@Test
	void testConexion() {
		Parada paradaOrigen = paradas.get(70);
		Parada paradaDestino = paradas.get(47);
		int diaSemana = 1;
		LocalTime horaLlegaParada = LocalTime.of(10, 30);

		List<List<Recorrido>> recorridos = calculo.calcularRecorrido(paradaOrigen, paradaDestino, diaSemana,
				horaLlegaParada, tramos);

		assertEquals(1, recorridos.size());
		assertEquals(2, recorridos.get(0).size());

		Recorrido recorrido1 = recorridos.get(0).get(0);
		Recorrido recorrido2 = recorridos.get(0).get(1);

		assertEquals(lineas.get("L2"), recorrido1.getLinea());

		assertEquals(LocalTime.of(10, 41, 30), recorrido1.getHoraSalida());
		assertEquals(1740, recorrido1.getDuracion());

		assertEquals(lineas.get("L1"), recorrido2.getLinea());

		assertEquals(LocalTime.of(11, 25, 30), recorrido2.getHoraSalida());
		assertEquals(690, recorrido2.getDuracion());
	}

	@Test
	void testConexionCaminando() {
		Parada paradaOrigen = paradas.get(20);
		Parada paradaDestino = paradas.get(6);
		int diaSemana = 1; // Lunes
		LocalTime horaLlegaParada = LocalTime.of(10, 30);

		List<List<Recorrido>> recorridos = calculo.calcularRecorrido(paradaOrigen, paradaDestino, diaSemana,
				horaLlegaParada, tramos);

		assertEquals(1, recorridos.size());
		assertEquals(3, recorridos.get(0).size());

		Recorrido recorrido1 = recorridos.get(0).get(0);
		Recorrido recorrido2 = recorridos.get(0).get(1);
		Recorrido recorrido3 = recorridos.get(0).get(2);

		assertEquals(lineas.get("L1"), recorrido1.getLinea());
		assertEquals(LocalTime.of(10, 44, 30), recorrido1.getHoraSalida());
		assertEquals(570, recorrido1.getDuracion());

		assertNull(recorrido2.getLinea());
		assertEquals(LocalTime.of(10, 54, 0), recorrido2.getHoraSalida());
		assertEquals(94, recorrido2.getDuracion());

		assertEquals(lineas.get("L1"), recorrido3.getLinea());
		assertEquals(LocalTime.of(11, 00, 0), recorrido3.getHoraSalida());
		assertEquals(210, recorrido3.getDuracion());
	}
}