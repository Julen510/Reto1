package vista;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import controlador.Controlador;
import modelo.Usuario;

public class VPerfil extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtNombre;
	private JTextField txtApellidos;
	private JTextField txtEmail;
	private JPasswordField txtContrasena;
	private JTextField txtFechaNacimiento;
	private JTextField txtNivel;
	private Controlador controlador = new Controlador();

	public VPerfil() {
		setTitle("PERFIL");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 80, 620, 580);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JButton btnAtras = new JButton("Atrás");
		btnAtras.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnAtras.setBounds(30, 25, 100, 36);
		btnAtras.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				VWorkouts workouts = new VWorkouts();
				workouts.setVisible(true);
				dispose();
			}
		});
		contentPane.add(btnAtras);

		JLabel lblTitulo = new JLabel("PERFIL");
		lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitulo.setFont(new Font("Tahoma", Font.BOLD, 24));
		lblTitulo.setBounds(160, 25, 300, 36);
		contentPane.add(lblTitulo);

		JLabel lblNombre = new JLabel("Nombre");
		lblNombre.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblNombre.setBounds(80, 100, 160, 28);
		contentPane.add(lblNombre);

		txtNombre = new JTextField();
		txtNombre.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtNombre.setBounds(280, 100, 220, 28);
		contentPane.add(txtNombre);

		JLabel lblApellidos = new JLabel("Apellidos");
		lblApellidos.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblApellidos.setBounds(80, 150, 160, 28);
		contentPane.add(lblApellidos);

		txtApellidos = new JTextField();
		txtApellidos.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtApellidos.setBounds(280, 150, 220, 28);
		contentPane.add(txtApellidos);

		JLabel lblEmail = new JLabel("Email");
		lblEmail.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblEmail.setBounds(80, 200, 160, 28);
		contentPane.add(lblEmail);

		txtEmail = new JTextField();
		txtEmail.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtEmail.setBounds(280, 200, 220, 28);
		contentPane.add(txtEmail);

		JLabel lblContrasena = new JLabel("Contraseña");
		lblContrasena.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblContrasena.setBounds(80, 250, 160, 28);
		contentPane.add(lblContrasena);

		txtContrasena = new JPasswordField();
		txtContrasena.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtContrasena.setBounds(280, 250, 220, 28);
		contentPane.add(txtContrasena);

		JLabel lblFechaNacimiento = new JLabel("Fecha de Nacimiento");
		lblFechaNacimiento.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblFechaNacimiento.setBounds(80, 300, 180, 28);
		contentPane.add(lblFechaNacimiento);

		txtFechaNacimiento = new JTextField();
		txtFechaNacimiento.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtFechaNacimiento.setBounds(280, 300, 220, 28);
		contentPane.add(txtFechaNacimiento);

		JLabel lblNivel = new JLabel("Nivel");
		lblNivel.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblNivel.setBounds(80, 350, 160, 28);
		contentPane.add(lblNivel);

		txtNivel = new JTextField("0");
		txtNivel.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtNivel.setBounds(280, 350, 220, 28);
		txtNivel.setEditable(false);
		txtNivel.setEnabled(false);
		contentPane.add(txtNivel);

		JButton btnGuardar = new JButton("Guardar");
		btnGuardar.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnGuardar.setBounds(80, 430, 120, 36);
		btnGuardar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String nombre = txtNombre.getText().trim();
				String apellidos = txtApellidos.getText().trim();
				String email = txtEmail.getText().trim();
				String contrasena = String.valueOf(txtContrasena.getPassword());
				String fechaNacimiento = txtFechaNacimiento.getText();

				if (nombre.isEmpty() || apellidos.isEmpty() || email.isEmpty()
						|| contrasena.isEmpty()) {
					JOptionPane.showMessageDialog(contentPane, "Debe rellenar todos los campos");
					return;
				}

				if (!controlador.hayConexion()) {
					JOptionPane.showMessageDialog(contentPane,
							"Sin conexión no se pueden guardar cambios del perfil (solo consulta)",
							"Aviso", JOptionPane.WARNING_MESSAGE);
					return;
				}

				try {
					boolean ok = controlador.actualizarPerfil(nombre, apellidos, email,
							contrasena, fechaNacimiento);
					if (ok) {
						JOptionPane.showMessageDialog(contentPane, "Perfil guardado (Firebase)");
						VWorkouts workouts = new VWorkouts();
						workouts.setVisible(true);
						dispose();
					} else {
						JOptionPane.showMessageDialog(contentPane,
								"No se pudo actualizar. Compruebe que el email no esté en uso.");
					}
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(contentPane, ex.getMessage());
				}
			}
		});
		contentPane.add(btnGuardar);

		addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				cargarPerfil();
			}
		});
	}

	private void cargarPerfil() {
		Usuario usuario = null;
		try {
			usuario = controlador.consultarPerfil();
		} catch (Exception ex) {
			usuario = Controlador.getUsuarioActual();
		}
		if (usuario == null) {
			return;
		}
		txtNombre.setText(usuario.getNombre() == null ? "" : usuario.getNombre());
		txtApellidos.setText(usuario.getApellidos() == null ? "" : usuario.getApellidos());
		txtEmail.setText(usuario.getEmail() == null ? "" : usuario.getEmail());
		txtContrasena.setText(usuario.getContrasena() == null ? "" : usuario.getContrasena());
		txtFechaNacimiento.setText(
				usuario.getFechaNacimiento() == null ? "" : usuario.getFechaNacimiento());
		txtNivel.setText(String.valueOf(usuario.getNivel()));
	}
}
