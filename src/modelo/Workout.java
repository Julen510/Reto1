package modelo;

import java.io.Serializable;
import java.util.ArrayList;

public class Workout implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private String nombre;
	private String descripcion;
	private int nivel;
	private String video;
	private ArrayList<Ejercicio> ejercicios = new ArrayList<>();

	public Workout() {
	}

	public Workout(String id, String nombre, String descripcion, int nivel, String video,
			ArrayList<Ejercicio> ejercicios) {
		this.id = id;
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.nivel = nivel;
		this.video = video;
		this.ejercicios = ejercicios;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public int getNivel() {
		return nivel;
	}

	public void setNivel(int nivel) {
		this.nivel = nivel;
	}

	public String getVideo() {
		return video;
	}

	public void setVideo(String video) {
		this.video = video;
	}

	public ArrayList<Ejercicio> getEjercicios() {
		return ejercicios;
	}

	public void setEjercicios(ArrayList<Ejercicio> ejercicios) {
		this.ejercicios = ejercicios;
	}

	public int getDuracionPrevista() {
		int total = 0;
		for (Ejercicio ejercicio : ejercicios) {
			total += ejercicio.getDuracionPrevista();
		}
		return total;
	}
}
