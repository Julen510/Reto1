package modelo;

import java.io.Serializable;
import java.util.ArrayList;

public class Ejercicio implements Serializable {

	private static final long serialVersionUID = 1L;

	private String nombre;
	private String descripcion;
	private int descanso;
	private ArrayList<Serie> series = new ArrayList<>();

	public Ejercicio() {
	}

	public Ejercicio(String nombre, String descripcion, int descanso, ArrayList<Serie> series) {
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.descanso = descanso;
		this.series = series;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public int getDescanso() {
		return descanso;
	}

	public void setDescanso(int descanso) {
		this.descanso = descanso;
	}

	public ArrayList<Serie> getSeries() {
		return series;
	}

	public void setSeries(ArrayList<Serie> series) {
		this.series = series;
	}

	//Duración prevista = series + un descanso por serie
	 
	public int getDuracionPrevista() {
		int total = 0;
		for (Serie serie : series) {
			int reps = serie.getRepeticiones();
			if (reps < 1) {
				reps = 1;
			}
			total += serie.getDuracion() * reps;
			total += descanso;
		}
		return total;
	}
}
