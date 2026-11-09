package controlador;

import java.time.LocalDateTime;
import java.util.ArrayList;

import modelo.GestorGimnasio;
import modelo.Historico;
import modelo.Usuario;
import modelo.Workout;

public class Controlador {

	private GestorGimnasio gestor = new GestorGimnasio();
	private static Usuario usuarioActual;

	public boolean hayConexion() {
		return gestor.hayConexion();
	}

	public Usuario loginUsuario(String email, String contrasena) throws Exception {
		Usuario usuario = gestor.loginUsuario(email, contrasena);
		if (usuario != null) {
			usuarioActual = usuario;
			if (gestor.hayConexion()) {
				gestor.subirHistoricoOffline(usuario.getEmail());
				String resultado = gestor.lanzarProcesoBackup();
				System.out.println("Proceso backup: " + resultado);
			}
		}
		return usuario;
	}

	public boolean registrarUsuario(String nombre, String apellidos, String email,
			String contrasena, String fechaNacimiento) throws Exception {
		return gestor.registrarUsuario(nombre, apellidos, email, contrasena, fechaNacimiento);
	}

	public boolean actualizarPerfil(String nombre, String apellidos, String email,
			String contrasena, String fechaNacimiento) throws Exception {
		Usuario actual = getUsuarioActual();
		if (actual == null) {
			return false;
		}
		boolean ok = gestor.actualizarPerfil(actual.getEmail(), nombre, apellidos, email,
				contrasena, fechaNacimiento);
		if (ok) {
			actual.setNombre(nombre);
			actual.setApellidos(apellidos);
			actual.setEmail(email);
			actual.setContrasena(contrasena);
			actual.setFechaNacimiento(fechaNacimiento);
		}
		return ok;
	}

	public Usuario consultarPerfil() throws Exception {
		Usuario actual = getUsuarioActual();
		if (actual == null) {
			return null;
		}
		Usuario usuarioLeido = gestor.obtenerUsuario(actual.getEmail());
		if (usuarioLeido != null) {
			usuarioActual = usuarioLeido;
		}
		return usuarioActual;
	}

	public ArrayList<Workout> obtenerWorkouts(Integer filtroNivel) throws Exception {
		Usuario usuario = getUsuarioActual();
		if (usuario == null) {
			return new ArrayList<>();
		}
		Usuario usuarioLeido = gestor.obtenerUsuario(usuario.getEmail());
		if (usuarioLeido != null) {
			usuarioActual = usuarioLeido;
			usuario = usuarioLeido;
		}
		ArrayList<Workout> lista = gestor.obtenerWorkoutsHastaNivel(usuario.getNivel());
		if (filtroNivel == null) {
			return lista;
		}
		ArrayList<Workout> filtrados = new ArrayList<>();
		for (int i = 0; i < lista.size(); i++) {
			if (lista.get(i).getNivel() <= filtroNivel) {
				filtrados.add(lista.get(i));
			}
		}
		return filtrados;
	}

	public Workout obtenerWorkoutPorId(String id) throws Exception {
		return gestor.obtenerWorkoutPorId(id);
	}

	// 0 = no sube, >0 = nuevo nivel, -1 = maximo
	public int guardarWorkoutRealizado(Workout workout, int tiempoTotalSegundos,
			double porcentajeCompletado) throws Exception {
		Usuario usuario = getUsuarioActual();
		Historico registro = new Historico(
				usuario.getEmail(),
				workout.getNombre(),
				workout.getNivel(),
				tiempoTotalSegundos,
				workout.getDuracionPrevista(),
				LocalDateTime.now(),
				porcentajeCompletado);
		gestor.guardarHistorico(registro);

		if (porcentajeCompletado >= 100.0) {
			return gestor.intentarSubirNivel(usuario);
		}
		return 0;
	}

	public ArrayList<Historico> obtenerHistorico() throws Exception {
		Usuario usuario = getUsuarioActual();
		if (usuario == null) {
			return new ArrayList<>();
		}
		return gestor.obtenerHistoricoUsuario(usuario.getEmail());
	}

	public static Usuario getUsuarioActual() {
		return usuarioActual;
	}

	public static void cerrarSesion() {
		usuarioActual = null;
	}
}
