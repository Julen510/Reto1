package modelo;

import java.io.FileInputStream;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;

public class CnnFirebase {

	private static final String ID_PROYECTO = "gimnasio2-d5e66";
	private static final String FICHERO_CREDENCIALES = "config.json";

	// combo del login: Activada / Desactivada
	private static boolean forzarDesconectado = false;

	public static void setForzarDesconectado(boolean desconectado) {
		forzarDesconectado = desconectado;
	}

	public static void main(String[] args) {
		try {
			Firestore db = conectar();
			System.out.println("conectado");
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Firestore conectar() {
		if (forzarDesconectado) {
			return null;
		}

		System.setProperty("javax.net.ssl.trustStoreType", "Windows-ROOT");

		try {
			FileInputStream credenciales = new FileInputStream(FICHERO_CREDENCIALES);
			FirestoreOptions opciones = FirestoreOptions.getDefaultInstance().toBuilder()
					.setProjectId(ID_PROYECTO)
					.setCredentials(GoogleCredentials.fromStream(credenciales))
					.build();
			credenciales.close();
			return opciones.getService();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
