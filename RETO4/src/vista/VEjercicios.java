package vista;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import controlador.Controlador;
import modelo.Ejercicio;
import modelo.Serie;
import modelo.Temporizador;
import modelo.Usuario;
import modelo.Workout;

public class VEjercicios extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final int SEGUNDOS_CUENTA = 5;
	private static final int SEGUNDOS_DESCANSO = 10;

	private JPanel contentPane;
	private Workout workout;
	private Controlador controlador = new Controlador();

	private JLabel lblNombreWorkout;
	private JLabel lblCronometroWorkout;
	private JLabel lblAviso;
	private JButton btnAtras;
	private JButton btnSalir;
	private JButton btnIniciar;
	private JLabel lblNombreEjercicio;
	private JLabel lblDescEjercicio;
	private JLabel lblCronometroEjercicio;
	private JPanel panelSeries;
	private ArrayList<VSerie> tarjetas = new ArrayList<>();

	private ArrayList<Serie> cola = new ArrayList<>();
	private ArrayList<Integer> colaDescansos = new ArrayList<>();

	private ArrayList<Ejercicio> listaEjercicios = new ArrayList<>();
	private int indiceEjercicio;

	private Temporizador hilo;
	// esperando / cuenta / serie / descanso
	private String fase = "esperando";
	private boolean pausado;
	private boolean terminado;
	private boolean cronometrosActivos;

	private int segundosWorkout;
	private int segundosEjercicio;
	private int cuentaActual;
	private int indiceActual;
	private int indiceRepeticion;
	private int seriesCompletadas;
	private int totalSeriesWorkout;
	private int ejerciciosCompletados;
	// repeticiones / series / ejercicios
	private String tipoDescanso;
	private boolean pasarAlSiguienteEjercicio;
	private boolean cuentaInicioPendiente = true;

	public VEjercicios() {
		inicializarComponentes();
	}

	public VEjercicios(Workout workout) {
		this.workout = workout;
		inicializarComponentes();
		indiceActual = 0;
		indiceRepeticion = 0;
		indiceEjercicio = 0;
		seriesCompletadas = 0;
		ejerciciosCompletados = 0;
		prepararListaEjercicios();
		cargarEjercicioActual();
		iniciarHilo();
	}

	private void inicializarComponentes() {
		setTitle("Gimnasio / Workouts / Ejercicios");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(50, 20, 1000, 700);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		btnAtras = new JButton("Atrás");
		btnAtras.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnAtras.setBounds(20, 15, 100, 36);
		btnAtras.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				abandonarWorkout();
			}
		});
		contentPane.add(btnAtras);

		lblNombreWorkout = new JLabel("Workout");
		lblNombreWorkout.setFont(new Font("Tahoma", Font.BOLD, 24));
		lblNombreWorkout.setBounds(140, 15, 500, 36);
		contentPane.add(lblNombreWorkout);

		lblNombreEjercicio = new JLabel("Ejercicio");
		lblNombreEjercicio.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblNombreEjercicio.setBounds(20, 65, 700, 28);
		contentPane.add(lblNombreEjercicio);

		lblDescEjercicio = new JLabel("Descripcion");
		lblDescEjercicio.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblDescEjercicio.setBounds(20, 98, 740, 28);
		contentPane.add(lblDescEjercicio);

		JLabel lblTituloCronWorkout = new JLabel("Tiempo workout");
		lblTituloCronWorkout.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblTituloCronWorkout.setBounds(780, 15, 160, 18);
		contentPane.add(lblTituloCronWorkout);

		lblCronometroWorkout = new JLabel("00:00", SwingConstants.CENTER);
		lblCronometroWorkout.setFont(new Font("Tahoma", Font.BOLD, 28));
		lblCronometroWorkout.setBounds(780, 35, 160, 35);
		contentPane.add(lblCronometroWorkout);

		JLabel lblTituloCronEj = new JLabel("Tiempo ejercicio");
		lblTituloCronEj.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblTituloCronEj.setBounds(780, 80, 160, 18);
		contentPane.add(lblTituloCronEj);

		lblCronometroEjercicio = new JLabel("00:00", SwingConstants.CENTER);
		lblCronometroEjercicio.setFont(new Font("Tahoma", Font.BOLD, 24));
		lblCronometroEjercicio.setBounds(780, 100, 160, 30);
		contentPane.add(lblCronometroEjercicio);

		panelSeries = new JPanel();
		panelSeries.setLayout(null);
		panelSeries.setOpaque(false);
		JScrollPane scrollSeries = new JScrollPane(panelSeries);
		scrollSeries.setBounds(20, 150, 940, 270);
		scrollSeries.setBorder(BorderFactory.createEmptyBorder());
		scrollSeries.getViewport().setOpaque(false);
		scrollSeries.setOpaque(false);
		scrollSeries.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollSeries.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		contentPane.add(scrollSeries);

		lblAviso = new JLabel("Pulsa Iniciar para empezar");
		lblAviso.setHorizontalAlignment(SwingConstants.LEFT);
		lblAviso.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblAviso.setBounds(20, 430, 700, 30);
		contentPane.add(lblAviso);

		btnIniciar = new JButton("Iniciar");
		btnIniciar.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnIniciar.setBounds(20, 480, 140, 36);
		btnIniciar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				accionBotonPrincipal();
			}
		});
		contentPane.add(btnIniciar);

		btnSalir = new JButton("Salir");
		btnSalir.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnSalir.setBounds(840, 480, 120, 36);
		btnSalir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				finalizarWorkout(false);
			}
		});
		contentPane.add(btnSalir);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				finalizarWorkout(false);
			}
		});
	}

	private void prepararListaEjercicios() {
		listaEjercicios.clear();
		totalSeriesWorkout = 0;
		if (workout == null) {
			return;
		}
		ArrayList<Ejercicio> origen = workout.getEjercicios();
		if (origen == null) {
			return;
		}
		for (int i = 0; i < origen.size(); i++) {
			Ejercicio ej = origen.get(i);
			if (ej == null || ej.getSeries() == null || ej.getSeries().isEmpty()) {
				continue;
			}
			listaEjercicios.add(ej);
			totalSeriesWorkout += ej.getSeries().size();
		}
	}

	private void iniciarHilo() {
		// el hilo llama cada segundo; invokeLater para tocar la pantalla
		hilo = new Temporizador(new Runnable() {
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						actualizarCadaSegundo();
					}
				});
			}
		});
		hilo.start();
	}

	private void actualizarCadaSegundo() {
		if (terminado || pausado) {
			return;
		}

		if (cronometrosActivos) {
			segundosWorkout++;
			segundosEjercicio++;
			actualizarCronometros();
		}

		if (fase.equals("esperando")) {
			return;
		}

		if (fase.equals("cuenta")) {
			cuentaActual--;
			lblAviso.setText("Empieza en... " + Math.max(cuentaActual, 0));
			if (cuentaActual <= 0) {
				empezarEjercicioEnCurso();
			}
			return;
		}

		cuentaActual--;
		if (fase.equals("serie")) {
			actualizarEjercicioEnCurso(cuentaActual);
			if (cuentaActual <= 0) {
				terminarRepeticion();
			}
		} else if (fase.equals("descanso")) {
			lblAviso.setText(textoDescanso() + Math.max(cuentaActual, 0) + " s");
			if (cuentaActual <= 0) {
				finDescanso();
			}
		}
	}

	private void actualizarCronometros() {
		lblCronometroWorkout.setText(formatear(segundosWorkout));
		lblCronometroEjercicio.setText(formatear(segundosEjercicio));
	}

	private void accionBotonPrincipal() {
		if (pausado) {
			pausado = false;
			if (hilo != null) {
				hilo.reanudar();
			}
			btnIniciar.setText("Pausar");
			restaurarAviso();
			return;
		}

		if (fase.equals("esperando")) {
			if (cuentaInicioPendiente) {
				iniciarCuentaInicio();
			} else {
				// Tras descansos: empieza directo, sin cuenta atrás
				empezarEjercicioEnCurso();
			}
			return;
		}

		pausado = true;
		if (hilo != null) {
			hilo.pausar();
		}
		btnIniciar.setText("Reanudar");
		lblAviso.setText("Pausado");
	}

	private void restaurarAviso() {
		if (fase.equals("cuenta")) {
			lblAviso.setText("Empieza en... " + cuentaActual);
		} else if (fase.equals("serie")) {
			lblAviso.setText("");
		} else if (fase.equals("descanso")) {
			lblAviso.setText(textoDescanso() + cuentaActual + " s");
		} else if (fase.equals("esperando")) {
			lblAviso.setText("Pulsa Iniciar para empezar");
		}
	}

	private String textoDescanso() {
		if (tipoDescanso.equals("repeticiones")) {
			return "Descanso entre repeticiones: ";
		}
		if (tipoDescanso.equals("series")) {
			return "Descanso entre series: ";
		}
		return "Descanso entre ejercicios: ";
	}

	private int totalRepeticionesActual() {
		if (indiceActual < 0 || indiceActual >= cola.size()) {
			return 1;
		}
		int reps = cola.get(indiceActual).getRepeticiones();
		return reps > 0 ? reps : 1;
	}

	private void iniciarCuentaInicio() {
		fase = "cuenta";
		cuentaActual = SEGUNDOS_CUENTA;
		pausado = false;
		cronometrosActivos = false;
		if (hilo != null) {
			hilo.reanudar();
		}
		btnIniciar.setEnabled(false);
		lblAviso.setText("Empieza en... " + cuentaActual);
		resaltarTarjetaActual();
	}

	private void empezarEjercicioEnCurso() {
		if (indiceActual < 0 || indiceActual >= cola.size()) {
			finalizarWorkout(true);
			return;
		}

		// Tras la primera cuenta atrás del workout, ya no se vuelve a usar
		cuentaInicioPendiente = false;

		fase = "serie";
		Serie serie = cola.get(indiceActual);
		cuentaActual = serie.getDuracion();
		cronometrosActivos = true;
		pausado = false;
		if (hilo != null) {
			hilo.reanudar();
		}
		// Solo reinicia el cronómetro del ejercicio en la 1ª repetición
		if (indiceRepeticion == 0) {
			segundosEjercicio = 0;
			lblCronometroEjercicio.setText("00:00");
		}
		actualizarCabecera();
		lblAviso.setText("");
		actualizarEjercicioEnCurso(cuentaActual);
		if (indiceActual < tarjetas.size()) {
			tarjetas.get(indiceActual).actualizarRepeticiones(indiceRepeticion + 1);
		}
		resaltarTarjetaActual();
		btnIniciar.setEnabled(true);
		btnIniciar.setText("Pausar");
	}

	private void actualizarEjercicioEnCurso(int restantes) {
		if (indiceActual >= 0 && indiceActual < tarjetas.size()) {
			tarjetas.get(indiceActual).actualizarCuenta(Math.max(restantes, 0));
		}
	}

	private void terminarRepeticion() {
		Serie serie = cola.get(indiceActual);
		int totalReps = totalRepeticionesActual();
		indiceRepeticion++;

		btnIniciar.setEnabled(true);
		btnIniciar.setText("Pausar");
		pasarAlSiguienteEjercicio = false;

		if (indiceRepeticion < totalReps) {
			iniciarDescanso("repeticiones");
			if (indiceActual < tarjetas.size()) {
				tarjetas.get(indiceActual).actualizarCuenta(serie.getDuracion());
				tarjetas.get(indiceActual).actualizarRepeticiones(indiceRepeticion);
			}
			return;
		}

		// Serie terminada
		if (indiceActual >= 0 && indiceActual < tarjetas.size()) {
			tarjetas.get(indiceActual).marcarTerminada();
		}
		seriesCompletadas++;
		indiceActual++;
		indiceRepeticion = 0;

		if (indiceActual < cola.size()) {
			// Quedan series del mismo ejercicio → descanso entre series
			iniciarDescanso("series");
			return;
		}

		// Ejercicio completo = todas sus series terminadas
		ejerciciosCompletados++;

		// Última serie del ejercicio: siempre hay descanso (también en el último ejercicio)
		if (indiceEjercicio + 1 < listaEjercicios.size()) {
			pasarAlSiguienteEjercicio = true;
			iniciarDescanso("ejercicios");
		} else {
			// Última serie del último ejercicio → descanso de serie; luego finDescanso cierra
			iniciarDescanso("series");
		}
	}

	private void iniciarDescanso(String tipo) {
		tipoDescanso = tipo;
		fase = "descanso";
		cuentaActual = obtenerDescansoActual();
		lblAviso.setText(textoDescanso() + cuentaActual + " s");
	}

	private int obtenerDescansoActual() {
		int descanso = SEGUNDOS_DESCANSO;
		int idx = Math.min(indiceActual, colaDescansos.size() - 1);
		if (idx < 0) {
			idx = colaDescansos.size() - 1;
		}
		if (idx >= 0 && idx < colaDescansos.size()) {
			descanso = colaDescansos.get(idx);
		}
		if (descanso <= 0) {
			descanso = SEGUNDOS_DESCANSO;
		}
		return descanso;
	}

	private void finDescanso() {
		if (pasarAlSiguienteEjercicio) {
			pasarAlSiguienteEjercicio = false;
			indiceEjercicio++;
			indiceActual = 0;
			indiceRepeticion = 0;
			cargarEjercicioActual();
			empezarEjercicioEnCurso();
			return;
		}
		if (indiceActual >= cola.size()) {
			finalizarWorkout(true);
			return;
		}
		empezarEjercicioEnCurso();
	}

	private void abandonarWorkout() {
		if (terminado) {
			return;
		}
		terminado = true;
		if (hilo != null) {
			hilo.detener();
		}
		if (workout == null) {
			dispose();
			return;
		}
		VWorkouts ventana = new VWorkouts();
		ventana.setVisible(true);
		dispose();
	}

	private void cargarEjercicioActual() {
		cola.clear();
		colaDescansos.clear();
		tarjetas.clear();
		panelSeries.removeAll();

		if (indiceEjercicio < 0 || indiceEjercicio >= listaEjercicios.size()) {
			lblNombreEjercicio.setText("");
			lblDescEjercicio.setText("");
			panelSeries.revalidate();
			panelSeries.repaint();
			return;
		}

		Ejercicio ejercicio = listaEjercicios.get(indiceEjercicio);
		ArrayList<Serie> series = ejercicio.getSeries();
		for (int j = 0; j < series.size(); j++) {
			cola.add(series.get(j));
			colaDescansos.add(ejercicio.getDescanso());
		}

		// Índice fijo de imagen: 1ª serie del workout → imagen1, 2ª → imagen2, …
		int indiceImagenBase = 1;
		for (int e = 0; e < indiceEjercicio; e++) {
			indiceImagenBase += listaEjercicios.get(e).getSeries().size();
		}

		int x = 10;
		for (int i = 0; i < cola.size(); i++) {
			VSerie tarjeta = new VSerie(cola.get(i), indiceImagenBase + i);
			tarjeta.setLocation(x, 10);
			panelSeries.add(tarjeta);
			tarjetas.add(tarjeta);
			x += 175;
		}
		panelSeries.setPreferredSize(new java.awt.Dimension(Math.max(x + 10, 900), 230));
		panelSeries.revalidate();
		panelSeries.repaint();

		String nombreEj = ejercicio.getNombre() == null ? "" : ejercicio.getNombre();
		String descEj = ejercicio.getDescripcion() == null ? "" : ejercicio.getDescripcion();
		lblNombreWorkout.setText(workout.getNombre());
		lblNombreEjercicio.setText(nombreEj);
		lblDescEjercicio.setText(descEj);
		actualizarCabecera();
	}

	private void actualizarCabecera() {
		if (indiceEjercicio < 0 || indiceEjercicio >= listaEjercicios.size()) {
			return;
		}
		Ejercicio ejercicio = listaEjercicios.get(indiceEjercicio);
		String nombreEj = ejercicio.getNombre() == null ? "" : ejercicio.getNombre();
		String descEj = ejercicio.getDescripcion() == null ? "" : ejercicio.getDescripcion();
		lblNombreEjercicio.setText(nombreEj);
		lblDescEjercicio.setText(descEj);
	}

	private void resaltarTarjetaActual() {
		for (int i = 0; i < tarjetas.size(); i++) {
			if (i == indiceActual) {
				tarjetas.get(i).setBorder(BorderFactory.createLineBorder(
						java.awt.Color.DARK_GRAY, 2));
			} else {
				tarjetas.get(i).setBorder(BorderFactory.createLineBorder(
						java.awt.Color.GRAY, 1));
			}
		}
	}

	// true = workout al 100 %; false = Salir con % parcial
	private void finalizarWorkout(boolean completado) {
		if (terminado) {
			return;
		}
		terminado = true;
		if (hilo != null) {
			hilo.detener();
		}
		if (workout == null) {
			dispose();
			return;
		}

		int totalEjercicios = listaEjercicios.size();
		if (completado && totalEjercicios > 0) {
			ejerciciosCompletados = totalEjercicios;
		}
		double porcentaje = 0;
		if (totalEjercicios > 0) {
			porcentaje = (ejerciciosCompletados * 100.0) / totalEjercicios;
		}

		int nivelAntes = 0;
		Usuario usuario = Controlador.getUsuarioActual();
		if (usuario != null) {
			nivelAntes = usuario.getNivel();
		}

		int resultadoNivel = 0;
		try {
			resultadoNivel = controlador.guardarWorkoutRealizado(workout, segundosWorkout, porcentaje);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(contentPane,
					"No se pudo guardar el progreso: " + ex.getMessage());
		}

		boolean subioNivel = resultadoNivel != 0;
		int nivelNuevo = nivelAntes;
		if (resultadoNivel > 0) {
			nivelNuevo = resultadoNivel;
		} else if (resultadoNivel == -1) {
			nivelNuevo = 11;
		}

		boolean alCien = porcentaje >= 100.0;
		Workout siguiente = alCien ? obtenerSiguienteWorkout() : null;
		boolean haySiguiente = siguiente != null && workout.getNivel() < 10
				&& resultadoNivel != -1;
		VResumen resumen = new VResumen(this, workout.getNombre(),
				formatear(segundosWorkout),
				String.format("%.0f %%", porcentaje),
				ejerciciosCompletados + "/" + totalEjercicios,
				subioNivel,
				nivelNuevo,
				haySiguiente,
				alCien);
		resumen.setVisible(true);

		if (resumen.getAccion() == VResumen.Accion.CONTINUAR && siguiente != null) {
			VEjercicios siguienteVentana = new VEjercicios(siguiente);
			siguienteVentana.setVisible(true);
		} else {
			VWorkouts ventana = new VWorkouts();
			ventana.setVisible(true);
		}
		dispose();
	}

	private Workout obtenerSiguienteWorkout() {
		try {
			ArrayList<Workout> lista = controlador.obtenerWorkouts(null);
			for (int i = 0; i < lista.size(); i++) {
				Workout w = lista.get(i);
				boolean mismo = false;
				if (workout.getId() != null && w.getId() != null) {
					mismo = workout.getId().equals(w.getId());
				} else if (workout.getNombre() != null) {
					mismo = workout.getNombre().equals(w.getNombre());
				}
				if (mismo && i + 1 < lista.size()) {
					return lista.get(i + 1);
				}
			}
		} catch (Exception ex) {
			// sin siguiente
		}
		return null;
	}

	private String formatear(int segundos) {
		int min = segundos / 60;
		int seg = segundos % 60;
		return String.format("%02d:%02d", min, seg);
	}
}
