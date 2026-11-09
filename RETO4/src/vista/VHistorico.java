package vista;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import controlador.Controlador;
import modelo.Historico;

public class VHistorico extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private DefaultTableModel modeloTabla;
	private Controlador controlador = new Controlador();

	public VHistorico() {
		setTitle("Gimnasio / Workouts / Historico");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 50, 920, 560);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JButton btnAtras = new JButton("Atrás");
		btnAtras.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnAtras.setBounds(20, 18, 100, 36);
		btnAtras.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				VWorkouts workouts = new VWorkouts();
				workouts.setVisible(true);
				dispose();
			}
		});
		contentPane.add(btnAtras);

		JLabel lblTitulo = new JLabel("Históricos");
		lblTitulo.setFont(new Font("Tahoma", Font.BOLD, 24));
		lblTitulo.setBounds(140, 18, 300, 36);
		contentPane.add(lblTitulo);

		modeloTabla = new DefaultTableModel(new Object[] {
				"Nombre Workout", "Nivel", "Tiempo total", "Tiempo previsto", "Fecha",
				"% Ejercicios completados"
		}, 0) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		JTable tabla = new JTable(modeloTabla);
		tabla.setFont(new Font("Tahoma", Font.PLAIN, 14));
		tabla.setRowSelectionAllowed(false);
		tabla.setColumnSelectionAllowed(false);
		tabla.setCellSelectionEnabled(false);
		tabla.setFocusable(false);
		JScrollPane scroll = new JScrollPane(tabla);
		scroll.setBounds(20, 75, 860, 400);
		contentPane.add(scroll);

		addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				cargarHistorico();
			}
		});
	}

	private void cargarHistorico() {
		modeloTabla.setRowCount(0);
		DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
		try {
			ArrayList<Historico> lista = controlador.obtenerHistorico();
			for (int i = 0; i < lista.size(); i++) {
				Historico h = lista.get(i);
				String fechaTxt = "";
				if (h.getFecha() != null) {
					fechaTxt = h.getFecha().format(formato);
				}
				modeloTabla.addRow(new Object[] {
						h.getNombreWorkout(),
						h.getNivel(),
						formatear(h.getTiempoTotalSegundos()),
						formatear(h.getTiempoPrevistoSegundos()),
						fechaTxt,
						String.format("%.0f %%", h.getPorcentajeCompletado())
				});
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(contentPane,
					"Error al cargar el histórico: " + ex.getMessage());
		}
	}

	private String formatear(int segundos) {
		int min = segundos / 60;
		int seg = segundos % 60;
		return String.format("%02d:%02d", min, seg);
	}
}
