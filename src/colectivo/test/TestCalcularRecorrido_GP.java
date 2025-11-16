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
import colectivo.logica.Recorrido;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

/**
 * Unit test for the {@code Calculo} service (route finding logic), made
 * specifically for the city General Pico. This test verifies the core business
 * logic of the route calculation system, ensuring that all strategies (direct,
 * bus-bus, walk-bus) work correctly under various conditions.
 * 
 * @author Juliana Martin
 * @author Ezequiel Ramos
 * @author Nerea Toledo
 */
class TestCalcularRecorrido_GP {

	/** Map containing all available stops, keyed by their integer code. */
	private Map<Integer, Parada> paradas;

	/** Map containing all available lines, keyed by their string code. */
	private Map<String, Linea> lineas;

	/** Map containing all available route segments, keyed by composite string. */
	private Map<String, Tramo> tramos;

	/** The main business logic service under test. */
	private Calculo calculo;

	/**
	 * Sets up the test environment before each test method runs. Uses the
	 * {@code Factory} to retrieve the necessary DAO implementations and loads the
	 * full system model (stops, segments, lines) into memory.
	 * 
	 * @throws Exception if data loading fails (e.g., Factory or DAO issues).
	 */
	@BeforeEach
	void setUp() throws Exception {
		paradas = (Factory.getInstancia("PARADA", ParadaDAO.class)).buscarTodos();
		tramos = (Factory.getInstancia("TRAMO", TramoDAO.class)).buscarTodos();
		lineas = (Factory.getInstancia("LINEA", LineaDAO.class)).buscarTodos();

		calculo = new Calculo(lineas);
	}

	/**
	 * Test case for a trip with no available service. Asserts that when a query is
	 * made for a non-existent route or for a time when all services are unavailable
	 * (e.g., late Sunday), the result list is empty.
	 */
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

	/**
	 * Test case for a direct bus route (highest priority strategy). Verifies that
	 * the {@code CalculoDirectoService} finds exactly one segment, that the bus
	 * line and stop sequence are correct, and that the calculated departure time
	 * and total duration match the expected schedule.
	 */
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

	/**
	 * Test case for a single bus-to-bus transfer route. Verifies that the
	 * {@code CalculoBusBusService} finds a two-segment route (Line L2 -> Line L1)
	 * and correctly calculates the schedule for both segments, respecting the
	 * layover time at the transfer stop.
	 */
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

	/**
	 * Test case for a three-segment Bus-Walk-Bus route. Verifies that the
	 * {@code CalculoCaminandoService} correctly finds the three segments (Bus A ->
	 * Walk -> Bus C) and correctly calculates the total time, ensuring the walk
	 * segment has a null line and the second bus waits for the walk to finish.
	 */
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