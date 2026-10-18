import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.swing.SwingWorker;

// Ejecuta trabajo pesado (base de datos) fuera del hilo de la interfaz
public class Tarea {

    public static <T> void ejecutar(Callable<T> trabajo,
                                    Consumer<T> alTerminar,
                                    Consumer<Exception> alFallar) {

        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return trabajo.call();
            }

            @Override
            protected void done() {
                try {
                    alTerminar.accept(get());
                } catch (java.util.concurrent.ExecutionException e) {
                    Throwable causa = e.getCause();
                    alFallar.accept(causa instanceof Exception
                            ? (Exception) causa : e);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }
}
