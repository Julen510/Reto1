package vista;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

public class VRegistro extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtNombre;
	private JTextField txtApellidos;
	private JTextField txtEmail;
	private JPasswordField txtContrasena;
	private JTextField txtFechaNacimiento;
	private Controlador controlador = new Controlador();

	public VRegistro() {
		setTitle("REGISTRO");
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
				VLogin login = new VLogin();
				login.setVisible(true);
				dispose();
			}
		});
		contentPane.add(btnAtras);

		JLabel lblRegistro = new JLabel("REGISTRO");
		lblRegistro.setHorizontalAlignment(SwingConstants.CENTER);
		lblRegistro.setFont(new Font("Tahoma", Font.BOLD, 24));
		lblRegistro.setBounds(160, 25, 300, 36);
		contentPane.add(lblRegistro);

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

		JTextField txtNivel = new JTextField("0");
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

				try {
					if (!controlador.hayConexion()) {
						JOptionPane.showMessageDialog(contentPane,
								"Sin conexión a Firebase no se pueden registrar usuarios nuevos",
								"Aviso", JOptionPane.WARNING_MESSAGE);
						return;
					}
					boolean guardado = controlador.registrarUsuario(nombre, apellidos, email,
							contrasena, fechaNacimiento);
					if (guardado) {
						JOptionPane.showMessageDialog(contentPane,
								"Usuario registrado en Firebase", "Registro",
								JOptionPane.INFORMATION_MESSAGE);
						VLogin login = new VLogin();
						login.setVisible(true);
						dispose();
					} else {
						JOptionPane.showMessageDialog(contentPane,
								"Ese usuario ya está registrado");
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(contentPane,
							"Error al registrar: "
									+ (ex.getMessage() != null ? ex.getMessage() : ex.toString()));
				}
			}
		});
		contentPane.add(btnGuardar);
	}
}
