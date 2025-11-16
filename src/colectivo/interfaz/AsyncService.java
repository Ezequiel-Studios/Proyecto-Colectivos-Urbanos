package colectivo.interfaz;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import java.util.concurrent.CompletableFuture;

/**
 * This service provides a thread-safe utility to execute time-consuming tasks
 * (e.g., I/O operations, database queries, heavy computations) on a background
 * thread pool, preventing the freezing of the JavaFX Application Thread.
 * 
 * @author Juliana Martin
 * @author Ezequiel Ramos
 * @author Nerea Toledo
 */
public class AsyncService {

	/** Logger instance for logging events, errors and exceptions. */
	private static final Logger LOGGER = LogManager.getLogger(AsyncService.class);

	/**
	 * A thread pool that executes tasks asynchronously. Using
	 * {@code newCachedThreadPool} dynamically creates new threads as needed and
	 * reuses idle threads.
	 */
	private final ExecutorService executor = Executors.newCachedThreadPool(runnable -> {
		Thread thread = new Thread(runnable);
		thread.setDaemon(true);
		return thread;
	});

	/**
	 * Executes a task asynchronously and handles the result on the JavaFX UI
	 * thread.
	 * 
	 * @param tarea   The background task to execute, a {@code Supplier} that
	 *                returns a result of type T.
	 * @param onExito The handler (Consumer) to execute on the UI thread upon
	 *                successful completion, receiving the result T.
	 * @param onError The handler (Consumer) to execute on the UI thread if an
	 *                exception occurs during the task, receiving the root
	 *                Exception.
	 */
	public <T> void ejecutarAsync(Supplier<T> tarea, Consumer<T> onExito, Consumer<Exception> onError) {

		CompletableFuture.supplyAsync(tarea, executor).whenComplete((resultado, error) -> {
			Platform.runLater(() -> {
				if (error != null) {
					LOGGER.error("Error en tarea asíncrona: {}", error.getCause().getMessage(), error.getCause());
					onError.accept((Exception) error.getCause());
				} else {
					onExito.accept(resultado);
				}
			});
		});
	}

	/**
	 * Initiates an orderly shutdown of the thread pool. Attempts to stop all
	 * actively executing tasks and halts the processing of waiting tasks.
	 */
	public void shutdown() {
		LOGGER.info("Apagando AsyncService...");
		executor.shutdown();
	}
}