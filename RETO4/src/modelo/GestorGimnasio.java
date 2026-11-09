package modelo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class GestorGimnasio {

	private static final String COL_USUARIOS = "usuarios";
	private static final String COL_WORKOUTS = "workouts";
	private static final String SUB_HISTORICO = "historico";

	private static final String USUARIOS_DAT = "backup/usuarios.dat";
	private static final String WORKOUTS_DAT = "backup/workouts.dat";
	private static final String HISTORICO_XML = "backup/historico.xml";
	private static final String WORKOUTS_JSON = "backup/workouts.json";

	private static final DateTimeFormatter FORMATO_FECHA =
			DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
	private static final int NIVEL_MAXIMO = 10;

	// este main lo usa el ProcessBuilder para el backup
	public static void main(String[] args) {
		if (args.length > 0 && args[0].equals("backup")) {
			try {
				GestorGimnasio gestor = new GestorGimnasio();
				gestor.crearBackups();
				System.out.println("correcto");
				System.exit(0);
			} catch (Exception e) {
				System.out.println("error");
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	public boolean hayConexion() {
		Firestore db = CnnFirebase.conectar();
		if (db == null) {
			return false;
		}
		try {
			// .get().get() espera el resultado de firebase
			db.collection(COL_USUARIOS).limit(1).get().get();
			return true;
		} catch (Exception e) {
			System.out.println("Sin conexion");
			return false;
		} finally {
			cerrarFirestore(db);
		}
	}

	public Usuario loginUsuario(String email, String contrasena) throws Exception {
		email = email.trim();
		contrasena = contrasena.trim();

		Firestore db = CnnFirebase.conectar();
		if (db != null) {
			try {
				ArrayList<Usuario> usuariosDat = leerUsuariosDat();
				ArrayList<Usuario> todos = leerUsuariosFirebase(db);

				for (int i = 0; i < todos.size(); i++) {
					Usuario u = todos.get(i);
					if (u.getEmail().equals(email)) {
						if (!u.getContrasena().equals(contrasena)) {
							return null;
						}
						// si subio de nivel offline, lo paso a firebase
						sincronizarNivelLocal(db, usuariosDat, u);
						guardarUsuariosDat(todos);
						return u;
					}
				}
				return null;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				cerrarFirestore(db);
			}
		}

		return loginSinConexion(email, contrasena);
	}

	private void sincronizarNivelLocal(Firestore db, ArrayList<Usuario> usuariosDat,
			Usuario usuario) throws Exception {
		for (int i = 0; i < usuariosDat.size(); i++) {
			Usuario delDat = usuariosDat.get(i);
			if (delDat.getEmail().equals(usuario.getEmail())
					&& delDat.getNivel() > usuario.getNivel()) {
				DocumentSnapshot documento = buscarUsuario(db, usuario.getEmail());
				if (documento != null) {
					documento.getReference().update("nivel", String.valueOf(delDat.getNivel())).get();
				}
				usuario.setNivel(delDat.getNivel());
				return;
			}
		}
	}

	private DocumentSnapshot buscarUsuario(Firestore db, String email) throws Exception {
		List<QueryDocumentSnapshot> documentos = db.collection(COL_USUARIOS)
				.whereEqualTo("email", email)
				.get().get().getDocuments();
		if (documentos.isEmpty()) {
			return null;
		}
		return documentos.get(0);
	}

	public boolean registrarUsuario(String nombre, String apellidos, String email,
			String contrasena, String fechaNacimiento) throws Exception {
		Firestore db = CnnFirebase.conectar();
		if (db == null) {
			return false;
		}
		try {
			DocumentSnapshot encontrado = buscarUsuario(db, email);
			if (encontrado != null) {
				return false;
			}
			Usuario nuevo = new Usuario(email.trim(), nombre, apellidos, contrasena,
					fechaNacimiento, 0);
			String id = siguienteIdUsuario(db);
			db.collection(COL_USUARIOS).document(id).set(datosUsuario(nuevo)).get();
			guardarUsuariosDat(leerUsuariosFirebase(db));
			return true;
		} finally {
			cerrarFirestore(db);
		}
	}

	private String siguienteIdUsuario(Firestore db) throws Exception {
		int max = -1;
		for (QueryDocumentSnapshot documento : db.collection(COL_USUARIOS).get().get().getDocuments()) {
			int n = numeroDelId(documento.getId());
			if (n > max) {
				max = n;
			}
		}
		return String.format("usuario%02d", max + 1);
	}

	public boolean actualizarPerfil(String emailActual, String nombre, String apellidos,
			String emailNuevo, String contrasena, String fechaNacimiento) throws Exception {
		emailActual = emailActual.trim();
		emailNuevo = emailNuevo.trim();

		Firestore db = CnnFirebase.conectar();
		if (db == null) {
			return false;
		}
		try {
			DocumentSnapshot documento = buscarUsuario(db, emailActual);
			if (documento == null) {
				return false;
			}
			if (!emailActual.equals(emailNuevo)) {
				DocumentSnapshot otroUsuario = buscarUsuario(db, emailNuevo);
				if (otroUsuario != null) {
					return false;
				}
			}

			Usuario usuario = leerUsuario(documento);
			usuario.setNombre(nombre);
			usuario.setApellidos(apellidos);
			usuario.setEmail(emailNuevo);
			usuario.setContrasena(contrasena);
			usuario.setFechaNacimiento(fechaNacimiento);

			documento.getReference().update(datosUsuario(usuario)).get();
			guardarUsuariosDat(leerUsuariosFirebase(db));

			// si cambia el email tambien en el historico
			if (!emailActual.equals(emailNuevo)) {
				actualizarEmailEnHistoricoXml(emailActual, emailNuevo);
				actualizarEmailEnHistoricoFirebase(documento.getReference(), emailNuevo);
			}
			return true;
		} finally {
			cerrarFirestore(db);
		}
	}

	public Usuario obtenerUsuario(String email) throws Exception {
		Firestore db = CnnFirebase.conectar();
		if (db != null) {
			try {
				DocumentSnapshot documento = buscarUsuario(db, email);
				if (documento == null) {
					return null;
				}
				return leerUsuario(documento);
			} finally {
				cerrarFirestore(db);
			}
		}
		ArrayList<Usuario> usuarios = leerUsuariosDat();
		for (int i = 0; i < usuarios.size(); i++) {
			if (usuarios.get(i).getEmail().equals(email)) {
				return usuarios.get(i);
			}
		}
		return null;
	}

	public ArrayList<Workout> obtenerWorkoutsHastaNivel(int nivelUsuario) throws Exception {
		Firestore db = CnnFirebase.conectar();
		if (db != null) {
			try {
				// workouts con nivel <= al del usuario
				List<QueryDocumentSnapshot> documentos = db.collection(COL_WORKOUTS)
						.whereLessThanOrEqualTo("nivel", nivelUsuario)
						.get().get().getDocuments();
				ArrayList<Workout> lista = new ArrayList<>();
				for (int i = 0; i < documentos.size(); i++) {
					lista.add(leerWorkout(documentos.get(i)));
				}
				if (lista.isEmpty()) {
					ArrayList<Workout> todos = leerWorkoutsFirebase(db);
					for (int i = 0; i < todos.size(); i++) {
						if (todos.get(i).getNivel() <= nivelUsuario) {
							lista.add(todos.get(i));
						}
					}
				}
				ordenarWorkoutsPorNivel(lista);
				return lista;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				cerrarFirestore(db);
			}
		}
		ArrayList<Workout> lista = new ArrayList<>();
		ArrayList<Workout> todos = leerWorkoutsDat();
		for (int i = 0; i < todos.size(); i++) {
			if (todos.get(i).getNivel() <= nivelUsuario) {
				lista.add(todos.get(i));
			}
		}
		return lista;
	}

	public Workout obtenerWorkoutPorId(String id) throws Exception {
		if (id == null || id.trim().equals("")) {
			return null;
		}
		Firestore db = CnnFirebase.conectar();
		if (db != null) {
			try {
				DocumentSnapshot documento = db.collection(COL_WORKOUTS).document(id).get().get();
				if (documento.exists()) {
					return leerWorkout(documento);
				}
			} finally {
				cerrarFirestore(db);
			}
		}
		ArrayList<Workout> todos = leerWorkoutsDat();
		for (int i = 0; i < todos.size(); i++) {
			if (id.equals(todos.get(i).getId())) {
				return todos.get(i);
			}
		}
		return null;
	}

	public void guardarHistorico(Historico registro) throws Exception {
		// siempre al xml (por si no hay conexion)
		anadirAlXml(registro);

		Firestore db = CnnFirebase.conectar();
		if (db == null) {
			return;
		}
		try {
			DocumentSnapshot documentoUsuario = buscarUsuario(db, registro.getEmailUsuario());
			if (documentoUsuario == null) {
				return;
			}
			guardarHistoricoFirebase(documentoUsuario.getReference(), registro);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cerrarFirestore(db);
		}
	}

	// al entrar online sube lo del xml que falte en firebase
	public void subirHistoricoOffline(String email) throws Exception {
		Firestore db = CnnFirebase.conectar();
		if (db == null) {
			return;
		}
		try {
			DocumentSnapshot documentoUsuario = buscarUsuario(db, email);
			if (documentoUsuario == null) {
				return;
			}

			DocumentReference usuarioRef = documentoUsuario.getReference();
			ArrayList<Historico> delXml = leerXmlPorUsuario(email);
			ArrayList<Historico> deFirebase = leerHistoricoFirebase(usuarioRef);

			for (int i = 0; i < delXml.size(); i++) {
				Historico h = delXml.get(i);
				if (!historicoYaExiste(deFirebase, h)) {
					guardarHistoricoFirebase(usuarioRef, h);
					deFirebase.add(h);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cerrarFirestore(db);
		}
	}

	private void guardarHistoricoFirebase(DocumentReference usuarioRef, Historico registro)
			throws Exception {
		String id = siguienteIdHistorico(usuarioRef);
		usuarioRef.collection(SUB_HISTORICO).document(id).set(datosHistorico(registro)).get();
	}

	private String siguienteIdHistorico(DocumentReference usuarioRef) throws Exception {
		int max = 0;
		List<QueryDocumentSnapshot> documentos = usuarioRef.collection(SUB_HISTORICO).get().get()
				.getDocuments();
		for (int i = 0; i < documentos.size(); i++) {
			int n = numeroDelId(documentos.get(i).getId());
			if (n > max) {
				max = n;
			}
		}
		return String.format("historico%02d", max + 1);
	}

	private boolean historicoYaExiste(ArrayList<Historico> listaFirebase, Historico buscado) {
		String fechaBuscada = buscado.getFecha().format(FORMATO_FECHA);
		for (int i = 0; i < listaFirebase.size(); i++) {
			Historico h = listaFirebase.get(i);
			String fechaFirebase = h.getFecha().format(FORMATO_FECHA);
			if (fechaBuscada.equals(fechaFirebase)
					&& buscado.getNombreWorkout().equals(h.getNombreWorkout())
					&& buscado.getTiempoTotalSegundos() == h.getTiempoTotalSegundos()) {
				return true;
			}
		}
		return false;
	}

	public ArrayList<Historico> obtenerHistoricoUsuario(String email) throws Exception {
		Firestore db = CnnFirebase.conectar();
		if (db != null) {
			try {
				DocumentSnapshot documentoUsuario = buscarUsuario(db, email);
				if (documentoUsuario != null) {
					ArrayList<Historico> lista = leerHistoricoFirebase(documentoUsuario.getReference());
					if (!lista.isEmpty()) {
						ordenarHistoricoPorFecha(lista);
						return lista;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				cerrarFirestore(db);
			}
		}
		return leerXmlPorUsuario(email);
	}

	private Map<String, Object> datosHistorico(Historico h) {
		Map<String, Object> datos = new HashMap<>();
		// Timestamp.now() guarda fecha real, no texto
		datos.put("añadido", Timestamp.now());
		datos.put("email", h.getEmailUsuario());
		datos.put("fecha", h.getFecha().format(FORMATO_FECHA));
		datos.put("workout", h.getNombreWorkout());
		datos.put("nivel", h.getNivel());
		datos.put("porcentaje", h.getPorcentajeCompletado());
		datos.put("tiempoPrevisto", h.getTiempoPrevistoSegundos());
		datos.put("tiempoTotal", h.getTiempoTotalSegundos());
		return datos;
	}

	private ArrayList<Historico> leerHistoricoFirebase(DocumentReference usuarioRef)
			throws Exception {
		ArrayList<Historico> lista = new ArrayList<>();
		List<QueryDocumentSnapshot> documentos = usuarioRef.collection(SUB_HISTORICO).get().get()
				.getDocuments();
		for (int i = 0; i < documentos.size(); i++) {
			lista.add(leerHistorico(documentos.get(i)));
		}
		return lista;
	}

	private Historico leerHistorico(QueryDocumentSnapshot documento) {
		Historico h = new Historico();
		h.setEmailUsuario(documento.getString("email"));
		h.setNombreWorkout(documento.getString("workout"));

		if (documento.getLong("nivel") != null) {
			h.setNivel(documento.getLong("nivel").intValue());
		} else {
			h.setNivel(0);
		}
		if (documento.getLong("tiempoTotal") != null) {
			h.setTiempoTotalSegundos(documento.getLong("tiempoTotal").intValue());
		} else {
			h.setTiempoTotalSegundos(0);
		}
		if (documento.getLong("tiempoPrevisto") != null) {
			h.setTiempoPrevistoSegundos(documento.getLong("tiempoPrevisto").intValue());
		} else {
			h.setTiempoPrevistoSegundos(0);
		}
		if (documento.getDouble("porcentaje") != null) {
			h.setPorcentajeCompletado(documento.getDouble("porcentaje"));
		} else {
			h.setPorcentajeCompletado(0);
		}

		String fecha = documento.getString("fecha");
		if (fecha != null && !fecha.equals("")) {
			h.setFecha(LocalDateTime.parse(fecha, FORMATO_FECHA));
		} else {
			h.setFecha(LocalDateTime.now());
		}
		return h;
	}

	// 0 = no sube, >0 = nuevo nivel, -1 = nivel maximo
	public int intentarSubirNivel(Usuario usuario) throws Exception {
		int nivelActual = usuario.getNivel();
		ArrayList<Workout> delNivel = new ArrayList<>();
		ArrayList<Historico> historico = obtenerHistoricoUsuario(usuario.getEmail());

		Firestore db = CnnFirebase.conectar();
		if (db != null) {
			try {
				for (QueryDocumentSnapshot documento : db.collection(COL_WORKOUTS).get().get()
						.getDocuments()) {
					Workout w = leerWorkout(documento);
					if (w.getNivel() == nivelActual) {
						delNivel.add(w);
					}
				}
				if (delNivel.isEmpty() || !workoutsCompletados(delNivel, historico)) {
					return 0;
				}
				return guardarSubidaNivel(usuario, nivelActual, db);
			} finally {
				cerrarFirestore(db);
			}
		}

		ArrayList<Workout> todos = leerWorkoutsDat();
		for (int i = 0; i < todos.size(); i++) {
			if (todos.get(i).getNivel() == nivelActual) {
				delNivel.add(todos.get(i));
			}
		}
		if (delNivel.isEmpty() || !workoutsCompletados(delNivel, historico)) {
			return 0;
		}
		return guardarSubidaNivel(usuario, nivelActual, null);
	}

	private int guardarSubidaNivel(Usuario usuario, int nivelActual, Firestore db)
			throws Exception {
		if (nivelActual >= NIVEL_MAXIMO) {
			usuario.setNivel(NIVEL_MAXIMO);
			if (db != null) {
				DocumentSnapshot documentoUsuario = buscarUsuario(db, usuario.getEmail());
				if (documentoUsuario != null) {
					documentoUsuario.getReference()
							.update("nivel", String.valueOf(NIVEL_MAXIMO)).get();
				}
			}
			actualizarUsuarioEnBackup(usuario);
			return -1;
		}

		int nuevo = nivelActual + 1;
		usuario.setNivel(nuevo);
		if (db != null) {
			DocumentSnapshot documentoUsuario = buscarUsuario(db, usuario.getEmail());
			if (documentoUsuario != null) {
				documentoUsuario.getReference().update("nivel", String.valueOf(nuevo)).get();
			}
		}
		actualizarUsuarioEnBackup(usuario);
		return nuevo;
	}

	private boolean workoutsCompletados(ArrayList<Workout> delNivel,
			ArrayList<Historico> historico) {
		for (int i = 0; i < delNivel.size(); i++) {
			Workout w = delNivel.get(i);
			boolean completo = false;
			for (int j = 0; j < historico.size(); j++) {
				Historico h = historico.get(j);
				if (h.getNombreWorkout() != null && h.getNombreWorkout().equals(w.getNombre())
						&& h.getNivel() == w.getNivel()
						&& h.getPorcentajeCompletado() >= 100.0) {
					completo = true;
					break;
				}
			}
			if (!completo) {
				return false;
			}
		}
		return true;
	}

	public void crearBackups() throws Exception {
		ArrayList<Usuario> usuarios;
		ArrayList<Workout> workouts;
		ArrayList<Historico> historico = leerXmlCompleto();

		Firestore db = CnnFirebase.conectar();
		if (db != null) {
			try {
				usuarios = leerUsuariosFirebase(db);
				workouts = leerWorkoutsFirebase(db);
				if (usuarios.isEmpty()) {
					usuarios = leerUsuariosDat();
				}
				if (workouts.isEmpty()) {
					workouts = leerWorkoutsDat();
				}
			} finally {
				cerrarFirestore(db);
			}
		} else {
			usuarios = leerUsuariosDat();
			workouts = leerWorkoutsDat();
		}

		guardarUsuariosDat(usuarios);
		guardarWorkoutsDat(workouts);
		guardarXmlCompleto(historico);
		guardarWorkoutsJson(workouts);
	}

	public String lanzarProcesoBackup() {
		try {
			ProcessBuilder processBuilder;
			// se le pasa el proceso (hace falta -cp por las librerias de firebase)
			processBuilder = new ProcessBuilder(
					"java",
					"-cp",
					System.getProperty("java.class.path"),
					"modelo.GestorGimnasio",
					"backup");
			processBuilder.directory(new File("."));

			// start lanza el proceso secundario
			Process process = processBuilder.start();

			// esperar a que termine
			int estado = process.waitFor();
			if (estado == 0) {
				return "correcto";
			}
			return "error";
		} catch (Exception e) {
			e.printStackTrace();
			return "error";
		}
	}

	private ArrayList<Usuario> leerUsuariosFirebase(Firestore db) throws Exception {
		ArrayList<Usuario> lista = new ArrayList<>();
		for (QueryDocumentSnapshot documento : db.collection(COL_USUARIOS).get().get().getDocuments()) {
			lista.add(leerUsuario(documento));
		}
		return lista;
	}

	private ArrayList<Workout> leerWorkoutsFirebase(Firestore db) throws Exception {
		ArrayList<Workout> lista = new ArrayList<>();
		for (QueryDocumentSnapshot documento : db.collection(COL_WORKOUTS).get().get().getDocuments()) {
			lista.add(leerWorkout(documento));
		}
		ordenarWorkoutsPorNivel(lista);
		return lista;
	}

	private Usuario leerUsuario(DocumentSnapshot documento) {
		Usuario u = new Usuario();
		u.setEmail(documento.getString("email"));
		u.setNombre(documento.getString("nombre"));
		u.setApellidos(documento.getString("apellido"));

		Object contrasenaLeida = documento.get("contraseña");
		if (contrasenaLeida != null) {
			u.setContrasena(contrasenaLeida.toString());
		} else {
			u.setContrasena("");
		}

		u.setFechaNacimiento(leerNacimiento(documento));

		int nivel = 0;
		if (documento.get("nivel") != null) {
			nivel = Integer.parseInt(documento.get("nivel").toString());
		}
		u.setNivel(nivel);
		return u;
	}

	private Map<String, Object> datosUsuario(Usuario u) {
		Map<String, Object> datos = new HashMap<>();
		datos.put("email", u.getEmail());
		datos.put("nombre", u.getNombre());
		datos.put("apellido", u.getApellidos());
		datos.put("contraseña", u.getContrasena());
		datos.put("nacimiento", convertirNacimiento(u.getFechaNacimiento()));
		datos.put("nivel", String.valueOf(u.getNivel()));
		return datos;
	}

	// texto dd/MM/yyyy -> Timestamp de firebase
	private Object convertirNacimiento(String fecha) {
		if (fecha == null || fecha.trim().equals("")) {
			return "";
		}
		try {
			SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
			Date fechaParseada = formato.parse(fecha.trim());
			return Timestamp.of(fechaParseada);
		} catch (Exception e) {
			return fecha.trim();
		}
	}

	// puede venir como Timestamp o como texto
	private String leerNacimiento(DocumentSnapshot documento) {
		Object nacimientoLeido = documento.get("nacimiento");
		if (nacimientoLeido == null) {
			return "";
		}
		if (nacimientoLeido instanceof Timestamp) {
			Timestamp fechaTimestamp = (Timestamp) nacimientoLeido;
			SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
			return formato.format(fechaTimestamp.toDate());
		}
		return nacimientoLeido.toString().trim();
	}

	private Workout leerWorkout(DocumentSnapshot documento) throws Exception {
		Workout w = new Workout();
		w.setId(documento.getId());
		w.setNombre(documento.getString("nombre"));
		w.setDescripcion(documento.getString("descripcion"));
		w.setVideo(documento.getString("video"));

		int nivel = 0;
		if (documento.get("nivel") != null) {
			nivel = Integer.parseInt(documento.get("nivel").toString());
		}
		w.setNivel(nivel);
		w.setEjercicios(leerEjercicios(documento.getReference()));
		return w;
	}

	private ArrayList<Ejercicio> leerEjercicios(DocumentReference referencia)
			throws Exception {
		ArrayList<Ejercicio> ejercicios = new ArrayList<>();
		ArrayList<QueryDocumentSnapshot> documentosEjercicios = new ArrayList<>(
				referencia.collection("ejercicios").get().get().getDocuments());
		ordenarPorId(documentosEjercicios);
		for (int i = 0; i < documentosEjercicios.size(); i++) {
			QueryDocumentSnapshot documentoEjercicio = documentosEjercicios.get(i);
			Ejercicio ej = new Ejercicio();
			ej.setNombre(documentoEjercicio.getString("nombre"));
			ej.setDescripcion(documentoEjercicio.getString("descripcion"));
			if (documentoEjercicio.getLong("descanso") != null) {
				ej.setDescanso(documentoEjercicio.getLong("descanso").intValue());
			} else {
				ej.setDescanso(0);
			}

			ArrayList<Serie> series = new ArrayList<>();
			ArrayList<QueryDocumentSnapshot> documentosSeries = new ArrayList<>(
					documentoEjercicio.getReference().collection("series").get().get().getDocuments());
			ordenarPorId(documentosSeries);
			for (int j = 0; j < documentosSeries.size(); j++) {
				QueryDocumentSnapshot documentoSerie = documentosSeries.get(j);
				Serie s = new Serie();
				s.setNombre(documentoSerie.getString("nombre"));
				s.setFoto(documentoSerie.getString("foto"));
				if (documentoSerie.getLong("repeticiones") != null) {
					s.setRepeticiones(documentoSerie.getLong("repeticiones").intValue());
				} else {
					s.setRepeticiones(0);
				}
				if (documentoSerie.getLong("duracion") != null) {
					s.setDuracion(documentoSerie.getLong("duracion").intValue());
				} else {
					s.setDuracion(0);
				}
				series.add(s);
			}
			ej.setSeries(series);
			ejercicios.add(ej);
		}
		return ejercicios;
	}

	private void ordenarPorId(ArrayList<QueryDocumentSnapshot> documentos) {
		for (int i = 0; i < documentos.size() - 1; i++) {
			for (int j = 0; j < documentos.size() - 1 - i; j++) {
				int idPrimero = numeroDelId(documentos.get(j).getId());
				int idSegundo = numeroDelId(documentos.get(j + 1).getId());
				if (idPrimero > idSegundo) {
					QueryDocumentSnapshot primero = documentos.get(j);
					documentos.set(j, documentos.get(j + 1));
					documentos.set(j + 1, primero);
				}
			}
		}
	}

	// usuario03 -> 3, historico12 -> 12
	private int numeroDelId(String id) {
		if (id == null) {
			return 0;
		}
		String numeros = "";
		for (int i = 0; i < id.length(); i++) {
			char c = id.charAt(i);
			if (c >= '0' && c <= '9') {
				numeros = numeros + c;
			} else {
				numeros = "";
			}
		}
		if (numeros.equals("")) {
			return 0;
		}
		return Integer.parseInt(numeros);
	}

	private Usuario loginSinConexion(String email, String contrasena) throws Exception {
		ArrayList<Usuario> usuarios = leerUsuariosDat();
		Usuario encontrado = null;
		for (int i = 0; i < usuarios.size(); i++) {
			Usuario u = usuarios.get(i);
			if (u.getEmail().equals(email)) {
				encontrado = u;
				break;
			}
		}
		if (encontrado == null) {
			throw new Exception("El usuario no está registrado");
		}
		if (!encontrado.getContrasena().equals(contrasena)) {
			return null;
		}
		return encontrado;
	}

	private ArrayList<Usuario> leerUsuariosDat() throws IOException {
		File f = new File(USUARIOS_DAT);
		if (!f.exists() || f.length() == 0) {
			return new ArrayList<>();
		}
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(f));
			ArrayList<Usuario> lista = (ArrayList<Usuario>) in.readObject();
			return lista;
		} catch (Exception e) {
			System.out.println("usuarios.dat ilegible, se reinicia: " + e.getMessage());
			return new ArrayList<>();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					// nada
				}
			}
		}
	}

	private ArrayList<Workout> leerWorkoutsDat() throws IOException {
		File f = new File(WORKOUTS_DAT);
		if (!f.exists()) {
			return new ArrayList<>();
		}
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(f));
			ArrayList<Workout> lista = (ArrayList<Workout>) in.readObject();
			return lista;
		} catch (Exception e) {
			throw new IOException("Error al leer workouts.dat");
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	private void guardarUsuariosDat(ArrayList<Usuario> usuarios) throws IOException {
		guardarDat(USUARIOS_DAT, usuarios);
	}

	private void guardarWorkoutsDat(ArrayList<Workout> workouts) throws IOException {
		guardarDat(WORKOUTS_DAT, workouts);
	}

	private void actualizarUsuarioEnBackup(Usuario usuario) throws IOException {
		ArrayList<Usuario> lista = leerUsuariosDat();
		boolean encontrado = false;
		for (int i = 0; i < lista.size(); i++) {
			if (lista.get(i).getEmail().equals(usuario.getEmail())) {
				lista.set(i, usuario);
				encontrado = true;
				break;
			}
		}
		if (!encontrado) {
			lista.add(usuario);
		}
		guardarUsuariosDat(lista);
	}

	private void actualizarEmailEnHistoricoXml(String emailAntiguo, String emailNuevo)
			throws Exception {
		Document doc = cargarXml();
		XPath xpath = XPathFactory.newInstance().newXPath();
		String consulta = "//workout[emailUsuario='" + emailAntiguo + "']/emailUsuario";
		NodeList nodos = (NodeList) xpath.evaluate(consulta, doc, XPathConstants.NODESET);
		for (int i = 0; i < nodos.getLength(); i++) {
			nodos.item(i).setTextContent(emailNuevo);
		}
		guardarXml(doc);
	}

	private void actualizarEmailEnHistoricoFirebase(DocumentReference usuarioRef, String emailNuevo)
			throws Exception {
		List<QueryDocumentSnapshot> documentos = usuarioRef.collection(SUB_HISTORICO).get().get()
				.getDocuments();
		for (int i = 0; i < documentos.size(); i++) {
			documentos.get(i).getReference().update("email", emailNuevo).get();
		}
	}

	private void guardarDat(String ruta, Object datos) throws IOException {
		File f = new File(ruta);
		File padre = f.getParentFile();
		if (padre != null && !padre.exists()) {
			padre.mkdirs();
		}
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(f));
			out.writeObject(datos);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	private void anadirAlXml(Historico registro) throws Exception {
		Document doc = cargarXml();
		Element raiz = doc.getDocumentElement();
		raiz.appendChild(crearNodoHistorico(doc, registro));
		guardarXml(doc);
	}

	private void guardarXmlCompleto(ArrayList<Historico> lista) throws Exception {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.newDocument();
		Element raiz = doc.createElement("historico");
		doc.appendChild(raiz);
		for (int i = 0; i < lista.size(); i++) {
			raiz.appendChild(crearNodoHistorico(doc, lista.get(i)));
		}
		File f = new File(HISTORICO_XML);
		File padre = f.getParentFile();
		if (padre != null && !padre.exists()) {
			padre.mkdirs();
		}
		guardarXml(doc);
	}

	private Element crearNodoHistorico(Document doc, Historico registro) {
		Element workout = doc.createElement("workout");

		Element emailUsuario = doc.createElement("emailUsuario");
		emailUsuario.appendChild(doc.createTextNode(registro.getEmailUsuario()));
		workout.appendChild(emailUsuario);

		Element nombreWorkout = doc.createElement("nombreWorkout");
		nombreWorkout.appendChild(doc.createTextNode(registro.getNombreWorkout()));
		workout.appendChild(nombreWorkout);

		Element nivel = doc.createElement("nivel");
		nivel.appendChild(doc.createTextNode(String.valueOf(registro.getNivel())));
		workout.appendChild(nivel);

		Element tiempoTotal = doc.createElement("tiempoTotalSegundos");
		tiempoTotal.appendChild(doc.createTextNode(String.valueOf(registro.getTiempoTotalSegundos())));
		workout.appendChild(tiempoTotal);

		Element tiempoPrevisto = doc.createElement("tiempoPrevistoSegundos");
		tiempoPrevisto.appendChild(doc.createTextNode(String.valueOf(registro.getTiempoPrevistoSegundos())));
		workout.appendChild(tiempoPrevisto);

		Element fecha = doc.createElement("fecha");
		fecha.appendChild(doc.createTextNode(registro.getFecha().format(FORMATO_FECHA)));
		workout.appendChild(fecha);

		Element porcentaje = doc.createElement("porcentajeCompletado");
		porcentaje.appendChild(doc.createTextNode(String.valueOf(registro.getPorcentajeCompletado())));
		workout.appendChild(porcentaje);

		return workout;
	}

	private ArrayList<Historico> leerXmlCompleto() throws Exception {
		Document doc = cargarXml();
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList nodos = (NodeList) xpath.evaluate("//workout", doc, XPathConstants.NODESET);
		ArrayList<Historico> lista = new ArrayList<>();
		for (int i = 0; i < nodos.getLength(); i++) {
			Node nodo = nodos.item(i);
			if (nodo.getNodeType() == Node.ELEMENT_NODE) {
				lista.add(leerHistoricoXml((Element) nodo));
			}
		}
		return lista;
	}

	private ArrayList<Historico> leerXmlPorUsuario(String email)
			throws Exception {
		Document doc = cargarXml();
		XPath xpath = XPathFactory.newInstance().newXPath();
		String consulta = "//workout[emailUsuario='" + email + "']";
		NodeList nodos = (NodeList) xpath.evaluate(consulta, doc, XPathConstants.NODESET);

		ArrayList<Historico> lista = new ArrayList<>();
		for (int i = 0; i < nodos.getLength(); i++) {
			Node nodo = nodos.item(i);
			if (nodo.getNodeType() == Node.ELEMENT_NODE) {
				lista.add(leerHistoricoXml((Element) nodo));
			}
		}
		ordenarHistoricoPorFecha(lista);
		return lista;
	}

	private Historico leerHistoricoXml(Element el) {
		Historico h = new Historico();
		h.setEmailUsuario(el.getElementsByTagName("emailUsuario").item(0).getTextContent());
		h.setNombreWorkout(el.getElementsByTagName("nombreWorkout").item(0).getTextContent());
		h.setNivel(Integer.parseInt(el.getElementsByTagName("nivel").item(0).getTextContent()));
		h.setTiempoTotalSegundos(Integer.parseInt(
				el.getElementsByTagName("tiempoTotalSegundos").item(0).getTextContent()));
		h.setTiempoPrevistoSegundos(Integer.parseInt(
				el.getElementsByTagName("tiempoPrevistoSegundos").item(0).getTextContent()));
		String fecha = el.getElementsByTagName("fecha").item(0).getTextContent();
		if (fecha != null && !fecha.equals("")) {
			h.setFecha(LocalDateTime.parse(fecha, FORMATO_FECHA));
		} else {
			h.setFecha(LocalDateTime.now());
		}
		h.setPorcentajeCompletado(Double.parseDouble(
				el.getElementsByTagName("porcentajeCompletado").item(0).getTextContent()));
		return h;
	}

	private Document cargarXml() throws Exception {
		File f = new File(HISTORICO_XML);
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		if (!f.exists()) {
			File padre = f.getParentFile();
			if (padre != null && !padre.exists()) {
				padre.mkdirs();
			}
			Document doc = builder.newDocument();
			Element raiz = doc.createElement("historico");
			doc.appendChild(raiz);
			guardarXml(doc);
			return doc;
		}
		Document doc = builder.parse(f);
		doc.getDocumentElement().normalize();
		return doc;
	}

	private void guardarXml(Document doc) throws Exception {
		File f = new File(HISTORICO_XML);
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.transform(new DOMSource(doc), new StreamResult(f));
	}

	private void guardarWorkoutsJson(ArrayList<Workout> workouts) throws IOException {
		File padre = new File("backup");
		if (!padre.exists()) {
			padre.mkdirs();
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		FileWriter writer = new FileWriter(WORKOUTS_JSON);
		gson.toJson(workouts, writer);
		writer.close();
	}

	private void ordenarWorkoutsPorNivel(ArrayList<Workout> lista) {
		for (int i = 0; i < lista.size() - 1; i++) {
			for (int j = 0; j < lista.size() - 1 - i; j++) {
				if (lista.get(j).getNivel() > lista.get(j + 1).getNivel()) {
					Workout primero = lista.get(j);
					lista.set(j, lista.get(j + 1));
					lista.set(j + 1, primero);
				}
			}
		}
	}

	private void ordenarHistoricoPorFecha(ArrayList<Historico> lista) {
		for (int i = 0; i < lista.size() - 1; i++) {
			for (int j = 0; j < lista.size() - 1 - i; j++) {
				if (lista.get(j).getFecha().isBefore(lista.get(j + 1).getFecha())) {
					Historico primero = lista.get(j);
					lista.set(j, lista.get(j + 1));
					lista.set(j + 1, primero);
				}
			}
		}
	}

	private void cerrarFirestore(Firestore db) {
		if (db != null) {
			try {
				db.close();
			} catch (Exception e) {
				// ignorar
			}
		}
	}
}
