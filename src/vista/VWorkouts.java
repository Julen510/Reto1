package vista;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import controlador.Controlador;
import modelo.Ejercicio;
import modelo.Usuario;
import modelo.Workout;

public class VWorkouts extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JLabel lblUsuarioNivel;
	private JLabel lblEjercicios;
	private JComboBox<String> cboNivel;
	private JTable tablaWorkouts;
	private DefaultTableModel modeloWorkouts;
	private DefaultTableModel modeloEjercicios;
	private ArrayList<Workout> workoutsVisibles = new ArrayList<>();
	private Workout workoutSeleccionado;
	private Controlador controlador = new Controlador();
	private boolean cargandoCombo;

	public VWorkouts() {
		setTitle("Gimnasio / Workouts");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(80, 40, 920, 680);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JButton btnAtras = new JButton("Atrás");
		btnAtras.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnAtras.setBounds(20, 18, 100, 36);
		btnAtras.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Controlador.cerrarSesion();
				VLogin login = new VLogin();
				login.setVisible(true);
				dispose();
			}
		});
		contentPane.add(btnAtras);

		lblUsuarioNivel = new JLabel("Usuario");
		lblUsuarioNivel.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblUsuarioNivel.setBounds(140, 18, 460, 36);
		contentPane.add(lblUsuarioNivel);

		JLabel lblFiltro = new JLabel("Nivel:");
		lblFiltro.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblFiltro.setBounds(620, 20, 55, 28);
		contentPane.add(lblFiltro);

		cboNivel = new JComboBox<>();
		cboNivel.setModel(new DefaultComboBoxModel<>(new String[] { "Todos" }));
		cboNivel.setFont(new Font("Tahoma", Font.PLAIN, 14));
		cboNivel.setBounds(680, 20, 200, 28);
		cboNivel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!cargandoCombo) {
					cargarTablaWorkouts();
				}
			}
		});
		contentPane.add(cboNivel);

		JLabel lblWorkouts = new JLabel("Workouts disponibles");
		lblWorkouts.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblWorkouts.setBounds(20, 70, 320, 24);
		contentPane.add(lblWorkouts);

		modeloWorkouts = new DefaultTableModel(
				new Object[] { "Nivel", "Nombre", "Descripcion", "Video" }, 0) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		tablaWorkouts = new JTable(modeloWorkouts);
		tablaWorkouts.setFont(new Font("Tahoma", Font.PLAIN, 14));
		tablaWorkouts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tablaWorkouts.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					mostrarEjerciciosSeleccionados();
				}
			}
		});
		JScrollPane scrollWorkouts = new JScrollPane(tablaWorkouts);
		scrollWorkouts.setBounds(20, 100, 860, 220);
		contentPane.add(scrollWorkouts);

		lblEjercicios = new JLabel("Ejercicios del workout");
		lblEjercicios.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblEjercicios.setBounds(20, 335, 600, 24);
		contentPane.add(lblEjercicios);

		modeloEjercicios = new DefaultTableModel(
				new Object[] { "Nombre", "Descripcion", "Duracion" }, 0) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		JTable tablaEjercicios = new JTable(modeloEjercicios);
		tablaEjercicios.setFont(new Font("Tahoma", Font.PLAIN, 14));
		tablaEjercicios.setRowSelectionAllowed(false);
		tablaEjercicios.setColumnSelectionAllowed(false);
		tablaEjercicios.setCellSelectionEnabled(false);
		tablaEjercicios.setFocusable(false);
		JScrollPane scrollEjercicios = new JScrollPane(tablaEjercicios);
		scrollEjercicios.setBounds(20, 365, 860, 180);
		contentPane.add(scrollEjercicios);

		JButton btnPerfil = new JButton("Perfil");
		btnPerfil.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnPerfil.setBounds(20, 570, 110, 36);
		btnPerfil.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				VPerfil perfil = new VPerfil();
				perfil.setVisible(true);
				dispose();
			}
		});
		contentPane.add(btnPerfil);

		JButton btnHistorico = new JButton("Histórico");
		btnHistorico.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnHistorico.setBounds(145, 570, 120, 36);
		btnHistorico.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				VHistorico historico = new VHistorico();
				historico.setVisible(true);
				dispose();
			}
		});
		contentPane.add(btnHistorico);

		JButton btnComenzar = new JButton("Comenzar");
		btnComenzar.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnComenzar.setBounds(740, 570, 140, 36);
		btnComenzar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				comenzarWorkout();
			}
		});
		contentPane.add(btnComenzar);

		addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				cargarTablaWorkouts();
			}
		});
	}

	private void comenzarWorkout() {
		if (workoutSeleccionado == null) {
			JOptionPane.showMessageDialog(contentPane, "Seleccione un workout");
			return;
		}
		try {
			// Releer de Firebase por si el workout cambió online tras el login
			Workout workoutLeido = controlador.obtenerWorkoutPorId(workoutSeleccionado.getId());
			if (workoutLeido != null) {
				workoutSeleccionado = workoutLeido;
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(contentPane,
					"No se pudo actualizar el workout: " + ex.getMessage());
		}
		VEjercicios ejercicios = new VEjercicios(workoutSeleccionado);
		ejercicios.setVisible(true);
		dispose();
	}

	private void cargarNivelesCombo() {
		cargandoCombo = true;
		ArrayList<String> opciones = new ArrayList<>();
		opciones.add("Todos");
		// Solo hasta el nivel del usuario (nunca niveles superiores)
		int maxNivel = 0;
		Usuario u = Controlador.getUsuarioActual();
		if (u != null) {
			maxNivel = u.getNivel();
		}
		for (int n = 0; n <= maxNivel; n++) {
			opciones.add("Nivel " + n);
		}
		cboNivel.setModel(new DefaultComboBoxModel<>(opciones.toArray(new String[0])));
		cboNivel.setSelectedItem("Todos");
		cargandoCombo = false;
	}

	private Integer obtenerFiltroNivel() {
		String seleccionado = (String) cboNivel.getSelectedItem();
		if (seleccionado == null || "Todos".equals(seleccionado)) {
			return null;
		}
		return Integer.parseInt(seleccionado.replace("Nivel ", ""));
	}

	private String nombreUsuarioLogueado() {
		Usuario u = Controlador.getUsuarioActual();
		if (u == null) {
			return "Usuario";
		}
		String nombre = u.getNombre();
		if (nombre == null || nombre.equals("")) {
			nombre = "Usuario";
		}
		return nombre;
	}

	private void cargarTablaWorkouts() {
		modeloWorkouts.setRowCount(0);
		modeloEjercicios.setRowCount(0);
		workoutsVisibles.clear();
		workoutSeleccionado = null;

		try {
			Integer filtro = obtenerFiltroNivel();
			ArrayList<Workout> lista = controlador.obtenerWorkouts(null);
			lblUsuarioNivel.setText(nombreUsuarioLogueado());
			if (filtro == null) {
				cargarNivelesCombo();
				workoutsVisibles = lista;
			} else {
				workoutsVisibles = controlador.obtenerWorkouts(filtro);
			}

			for (int i = 0; i < workoutsVisibles.size(); i++) {
				Workout workout = workoutsVisibles.get(i);
				modeloWorkouts.addRow(new Object[] {
						workout.getNivel(),
						workout.getNombre(),
						workout.getDescripcion(),
						workout.getVideo()
				});
			}
			if (!workoutsVisibles.isEmpty()) {
				tablaWorkouts.setRowSelectionInterval(0, 0);
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(contentPane,
					"Error al cargar workouts: " + ex.getMessage());
		}
	}

	private void mostrarEjerciciosSeleccionados() {
		modeloEjercicios.setRowCount(0);
		workoutSeleccionado = null;
		lblEjercicios.setText("Ejercicios del workout");

		int fila = tablaWorkouts.getSelectedRow();
		if (fila < 0 || fila >= workoutsVisibles.size()) {
			return;
		}

		workoutSeleccionado = workoutsVisibles.get(fila);
		String nombreWorkout = workoutSeleccionado.getNombre();
		if (nombreWorkout == null || nombreWorkout.isEmpty()) {
			nombreWorkout = "workout";
		}
		lblEjercicios.setText("Ejercicios del " + nombreWorkout);

		ArrayList<Ejercicio> ejercicios = workoutSeleccionado.getEjercicios();
		if (ejercicios == null) {
			return;
		}
		for (int i = 0; i < ejercicios.size(); i++) {
			Ejercicio ejercicio = ejercicios.get(i);
			modeloEjercicios.addRow(new Object[] {
					ejercicio.getNombre(),
					ejercicio.getDescripcion(),
					formatearTiempo(ejercicio.getDuracionPrevista())
			});
		}
	}

	private String formatearTiempo(int segundos) {
		int min = segundos / 60;
		int seg = segundos % 60;
		return String.format("%02d:%02d", min, seg);
	}
}
