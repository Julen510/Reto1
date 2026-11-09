package modelo;

import java.time.LocalDateTime;

public class Historico {

	private String emailUsuario;
	private String nombreWorkout;
	private int nivel;
	private int tiempoTotalSegundos;
	private int tiempoPrevistoSegundos;
	private LocalDateTime fecha;
	private double porcentajeCompletado;

	public Historico() {
	}

	public Historico(String emailUsuario, String nombreWorkout, int nivel,
			int tiempoTotalSegundos, int tiempoPrevistoSegundos, LocalDateTime fecha,
			double porcentajeCompletado) {
		this.emailUsuario = emailUsuario;
		this.nombreWorkout = nombreWorkout;
		this.nivel = nivel;
		this.tiempoTotalSegundos = tiempoTotalSegundos;
		this.tiempoPrevistoSegundos = tiempoPrevistoSegundos;
		this.fecha = fecha;
		this.porcentajeCompletado = porcentajeCompletado;
	}

	public String getEmailUsuario() {
		return emailUsuario;
	}

	public void setEmailUsuario(String emailUsuario) {
		this.emailUsuario = emailUsuario;
	}

	public String getNombreWorkout() {
		return nombreWorkout;
	}

	public void setNombreWorkout(String nombreWorkout) {
		this.nombreWorkout = nombreWorkout;
	}

	public int getNivel() {
		return nivel;
	}

	public void setNivel(int nivel) {
		this.nivel = nivel;
	}

	public int getTiempoTotalSegundos() {
		return tiempoTotalSegundos;
	}

	public void setTiempoTotalSegundos(int tiempoTotalSegundos) {
		this.tiempoTotalSegundos = tiempoTotalSegundos;
	}

	public int getTiempoPrevistoSegundos() {
		return tiempoPrevistoSegundos;
	}

	public void setTiempoPrevistoSegundos(int tiempoPrevistoSegundos) {
		this.tiempoPrevistoSegundos = tiempoPrevistoSegundos;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

	public void setFecha(LocalDateTime fecha) {
		this.fecha = fecha;
	}

	public double getPorcentajeCompletado() {
		return porcentajeCompletado;
	}

	public void setPorcentajeCompletado(double porcentajeCompletado) {
		this.porcentajeCompletado = porcentajeCompletado;
	}
}
