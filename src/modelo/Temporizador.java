package modelo;

// hilo del temporizador (como en MultiHilo: extends Thread + sleep)
public class Temporizador extends Thread {

	private boolean activo = true;
	private boolean pausado = false;
	private Runnable accion;

	public Temporizador(Runnable accion) {
		this.accion = accion;
	}

	public void pausar() {
		pausado = true;
	}

	public void reanudar() {
		pausado = false;
	}

	public void detener() {
		activo = false;
		// despierto el sleep por si esta dormido
		interrupt();
	}

	@Override
	public void run() {
		while (activo) {
			try {
				Thread.sleep(1000);
				if (activo && !pausado && accion != null) {
					accion.run();
				}
			} catch (InterruptedException e) {
				break;
			}
		}
	}
}
