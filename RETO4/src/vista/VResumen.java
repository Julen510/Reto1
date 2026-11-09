package vista;

import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class VResumen extends JDialog {

	private static final long serialVersionUID = 1L;

	public enum Accion {
		TERMINAR, CONTINUAR
	}

	private Accion accion = Accion.TERMINAR;
	private JLabel lblTitulo;
	private JLabel lblNombre;
	private JLabel lblNivel;
	private JLabel lblTiempo;
	private JLabel lblPorcentaje;
	private JLabel lblSeries;
	private JButton btnSiguiente;

	public VResumen() {
		super((Frame) null, "Resumen workout", true);
		inicializarComponentes();
	}

	public VResumen(Frame padre, String nombreWorkout, String tiempoTotal,
			String porcentaje, String seriesCompletadas, boolean subioNivel, int nivelNuevo,
			boolean haySiguiente, boolean completado) {
		super(padre, "Resumen workout", true);
		inicializarComponentes();
		setLocationRelativeTo(padre);
		aplicarDatos(nombreWorkout, tiempoTotal, porcentaje, seriesCompletadas,
				subioNivel, nivelNuevo, haySiguiente, completado);
	}

	private void inicializarComponentes() {
		setSize(460, 340);
		setLayout(null);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		lblTitulo = new JLabel("Workout completado");
		lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitulo.setFont(new Font("Tahoma", Font.BOLD, 24));
		lblTitulo.setBounds(20, 15, 400, 30);
		add(lblTitulo);

		lblNombre = new JLabel("Workout");
		lblNombre.setHorizontalAlignment(SwingConstants.CENTER);
		lblNombre.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblNombre.setBounds(20, 50, 400, 25);
		add(lblNombre);

		lblNivel = new JLabel("Nivel");
		lblNivel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNivel.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblNivel.setBounds(20, 90, 400, 25);
		lblNivel.setVisible(false);
		add(lblNivel);

		lblTiempo = new JLabel("Tiempo total:");
		lblTiempo.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblTiempo.setBounds(60, 90, 340, 25);
		add(lblTiempo);

		lblPorcentaje = new JLabel("Porcentaje:");
		lblPorcentaje.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblPorcentaje.setBounds(60, 120, 340, 25);
		add(lblPorcentaje);

		lblSeries = new JLabel("Ejercicios completados:");
		lblSeries.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblSeries.setBounds(60, 150, 340, 25);
		add(lblSeries);

		JButton btnTerminar = new JButton("Terminar");
		btnTerminar.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnTerminar.setBounds(40, 250, 160, 36);
		btnTerminar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				accion = Accion.TERMINAR;
				dispose();
			}
		});
		add(btnTerminar);

		btnSiguiente = new JButton("Siguiente");
		btnSiguiente.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnSiguiente.setBounds(240, 250, 160, 36);
		btnSiguiente.setEnabled(false);
		btnSiguiente.setToolTipText("Completa el workout al 100 % para continuar");
		btnSiguiente.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				accion = Accion.CONTINUAR;
				dispose();
			}
		});
		add(btnSiguiente);
	}

	private void aplicarDatos(String nombreWorkout, String tiempoTotal, String porcentaje,
			String seriesCompletadas, boolean subioNivel, int nivelNuevo,
			boolean haySiguiente, boolean completado) {
		lblTitulo.setText(completado ? "Workout completado" : "Workout terminado");
		if (nombreWorkout != null) {
			lblNombre.setText(nombreWorkout);
		}

		int y = 90;
		if (subioNivel) {
			lblNivel.setVisible(true);
			lblNivel.setBounds(20, y, 400, 25);
			lblNivel.setText(nivelNuevo > 10
					? "Nivel máximo alcanzado"
					: ("Has subido al nivel " + nivelNuevo));
			y += 35;
		} else {
			lblNivel.setVisible(false);
		}

		lblTiempo.setBounds(60, y, 340, 25);
		lblTiempo.setText("Tiempo total: " + tiempoTotal);
		y += 30;

		lblPorcentaje.setBounds(60, y, 340, 25);
		lblPorcentaje.setText("Porcentaje: " + porcentaje);
		y += 30;

		lblSeries.setBounds(60, y, 340, 25);
		lblSeries.setText("Ejercicios completados: " + seriesCompletadas);

		boolean puedeSeguir = completado && haySiguiente;
		btnSiguiente.setEnabled(puedeSeguir);
		btnSiguiente.setToolTipText(puedeSeguir
				? "Seguir con el siguiente workout"
				: (completado ? "No hay más workouts disponibles"
						: "Completa el workout al 100 % para continuar"));
	}

	public Accion getAccion() {
		return accion;
	}
}
