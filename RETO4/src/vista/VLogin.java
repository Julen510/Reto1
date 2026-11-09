package vista;

import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import controlador.Controlador;
import modelo.CnnFirebase;
import modelo.Usuario;

public class VLogin extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JLabel lblFoto;
	private JTextField txtEmail;
	private JPasswordField pasContrasena;
	private JComboBox<String> cboConexion;
	private Controlador controlador = new Controlador();

	public VLogin() {
		setTitle("Gimnasio");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 620, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblTitulo = new JLabel("GIMNASIO");
		lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitulo.setFont(new Font("Tahoma", Font.BOLD, 24));
		lblTitulo.setBounds(150, 20, 300, 35);
		contentPane.add(lblTitulo);

		lblFoto = new JLabel("Foto");
		lblFoto.setHorizontalAlignment(SwingConstants.CENTER);
		lblFoto.setBounds(200, 70, 200, 130);
		contentPane.add(lblFoto);

		JLabel lblEmail = new JLabel("Email");
		lblEmail.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblEmail.setBounds(140, 230, 110, 28);
		contentPane.add(lblEmail);

		txtEmail = new JTextField();
		txtEmail.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtEmail.setBounds(260, 230, 200, 28);
		contentPane.add(txtEmail);
		txtEmail.setColumns(10);

		JLabel lblContrasena = new JLabel("Contraseña");
		lblContrasena.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblContrasena.setBounds(140, 280, 110, 28);
		contentPane.add(lblContrasena);

		pasContrasena = new JPasswordField();
		pasContrasena.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pasContrasena.setBounds(260, 280, 200, 28);
		contentPane.add(pasContrasena);

		JLabel lblConexion = new JLabel("Conexion");
		lblConexion.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblConexion.setBounds(140, 330, 110, 28);
		contentPane.add(lblConexion);

		cboConexion = new JComboBox<>();
		cboConexion.setModel(new DefaultComboBoxModel<>(new String[] { "Activada", "Desactivada" }));
		cboConexion.setFont(new Font("Tahoma", Font.PLAIN, 14));
		cboConexion.setBounds(260, 330, 200, 28);
		cboConexion.setSelectedItem("Activada");
		contentPane.add(cboConexion);

		JButton btnLogin = new JButton("Aceptar");
		btnLogin.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnLogin.setBounds(160, 400, 120, 36);
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (txtEmail.getText().trim().isEmpty()
						|| String.valueOf(pasContrasena.getPassword()).isEmpty()) {
					JOptionPane.showMessageDialog(contentPane, "Debe rellenar los datos");
					return;
				}

				aplicarModoConexion();

				String email = txtEmail.getText().trim();
				String contrasena = String.valueOf(pasContrasena.getPassword());

				try {
					boolean conectado = controlador.hayConexion();
					Usuario usuario = controlador.loginUsuario(email, contrasena);

					if (conectado) {
						JOptionPane.showMessageDialog(contentPane,
								"Se conectó con Firebase", "Conexión",
								JOptionPane.INFORMATION_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(contentPane,
								"Sin conexion", "Aviso",
								JOptionPane.WARNING_MESSAGE);
					}

					if (usuario == null) {
						JOptionPane.showMessageDialog(contentPane,
								"Usuario y/o contraseña incorrectos");
					} else {
						VWorkouts workouts = new VWorkouts();
						workouts.setVisible(true);
						dispose();
					}
				} catch (Exception ex) {
					String msg = ex.getMessage();
					if (msg != null && msg.contains("no está registrado")) {
						JOptionPane.showMessageDialog(contentPane, msg);
					} else {
						JOptionPane.showMessageDialog(contentPane,
								"Error al iniciar sesión: " + msg);
					}
				}
			}
		});
		contentPane.add(btnLogin);

		JButton btnRegistro = new JButton("Registrarse");
		btnRegistro.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnRegistro.setBounds(320, 400, 140, 36);
		btnRegistro.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				aplicarModoConexion();
				if (!controlador.hayConexion()) {
					JOptionPane.showMessageDialog(contentPane,
							"No puedes registrarte sin conexión",
							"Aviso", JOptionPane.WARNING_MESSAGE);
					return;
				}
				VRegistro registro = new VRegistro();
				registro.setVisible(true);
				dispose();
			}
		});
		contentPane.add(btnRegistro);

		addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				cargarFoto();
			}
		});
	}

	private void aplicarModoConexion() {
		String sel = (String) cboConexion.getSelectedItem();
		boolean desactivado = "Desactivada".equals(sel);
		CnnFirebase.setForzarDesconectado(desactivado);
	}

	private void cargarFoto() {
		File foto = new File("imagenes/logo.png");
		if (!foto.exists()) {
			foto = new File("imagenes/imagen1.png");
		}
		if (foto.exists()) {
			ImageIcon icono = new ImageIcon(foto.getAbsolutePath());
			Image imagen = icono.getImage().getScaledInstance(200, 130, Image.SCALE_SMOOTH);
			lblFoto.setIcon(new ImageIcon(imagen));
			lblFoto.setText("Foto");
		}
	}
}
