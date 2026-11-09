package vista;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import modelo.Serie;

public class VSerie extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final int TOTAL_IMAGENES = 83;

	private JLabel lblImagen;
	private JLabel lblNombre;
	private JLabel lblRepeticiones;
	private JLabel lblTemporizador;
	private Serie serie;
	private boolean terminada;
	private int numeroImagen = 1;
	private int totalRepeticiones = 1;

	public VSerie() {
		inicializarComponentes();
	}

	public VSerie(Serie serie, int numeroImagen) {
		this.serie = serie;
		this.numeroImagen = ((numeroImagen - 1) % TOTAL_IMAGENES) + 1;
		if (this.numeroImagen < 1) {
			this.numeroImagen = 1;
		}
		if (serie != null && serie.getRepeticiones() > 0) {
			this.totalRepeticiones = serie.getRepeticiones();
		}
		inicializarComponentes();
		aplicarDatosSerie();
	}

	private void inicializarComponentes() {
		setLayout(null);
		setBorder(BorderFactory.createLineBorder(Color.GRAY));
		setBackground(Color.WHITE);
		setSize(160, 210);

		lblImagen = new JLabel("Sin foto", SwingConstants.CENTER);
		lblImagen.setBounds(10, 10, 140, 90);
		lblImagen.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		add(lblImagen);

		lblNombre = new JLabel("Serie", SwingConstants.LEFT);
		lblNombre.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblNombre.setBounds(10, 105, 140, 18);
		add(lblNombre);

		lblRepeticiones = new JLabel("Repeticiones: 0/0", SwingConstants.LEFT);
		lblRepeticiones.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblRepeticiones.setBounds(10, 125, 140, 18);
		add(lblRepeticiones);

		lblTemporizador = new JLabel("00:00", SwingConstants.LEFT);
		lblTemporizador.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblTemporizador.setBounds(10, 155, 140, 30);
		add(lblTemporizador);
	}

	private void aplicarDatosSerie() {
		if (serie != null && serie.getNombre() != null) {
			lblNombre.setText(serie.getNombre());
		}
		lblRepeticiones.setText("Repeticiones: 0/" + totalRepeticiones);
		if (serie != null) {
			lblTemporizador.setText(formatear(serie.getDuracion()));
		}
		cargarFoto();
	}

	private void cargarFoto() {
		File archivo = new File("imagenes/imagen" + numeroImagen + ".png");
		if (archivo.exists()) {
			ImageIcon icono = new ImageIcon(archivo.getAbsolutePath());
			Image img = icono.getImage().getScaledInstance(140, 90, Image.SCALE_SMOOTH);
			lblImagen.setIcon(new ImageIcon(img));
			lblImagen.setText("");
		}
	}

	public void actualizarCuenta(int segundosRestantes) {
		if (terminada) {
			return;
		}
		lblTemporizador.setText(formatear(segundosRestantes));
	}

	public void actualizarRepeticiones(int actual) {
		if (actual < 0) {
			actual = 0;
		}
		if (actual > totalRepeticiones) {
			actual = totalRepeticiones;
		}
		lblRepeticiones.setText("Repeticiones: " + actual + "/" + totalRepeticiones);
	}

	public void marcarTerminada() {
		terminada = true;
		actualizarRepeticiones(totalRepeticiones);
		lblTemporizador.setText("Terminado");
		lblTemporizador.setForeground(Color.BLACK);
	}

	public Serie getSerie() {
		return serie;
	}

	private String formatear(int segundos) {
		int min = segundos / 60;
		int seg = segundos % 60;
		return String.format("%02d:%02d", min, seg);
	}
}
