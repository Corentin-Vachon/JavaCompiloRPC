package rpc;


public class ExempleSkel
{ 

	 public static void main(String [] arg) throws Exception {
		Exemple m = null;
		java.net.ServerSocket sos = new java.net.ServerSocket(1234);
		java.net.Socket s = sos.accept();
		java.io.DataInputStream dis = new java.io.DataInputStream(s.getInputStream());
		java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(s.getOutputStream());
		String fonction = dis.readUTF();
		if (fonction.equals("constructeur")) {
			m = new Exemple(dis.readInt());
		}
		fonction = dis.readUTF();
		if (fonction.equals("calcul")) {
			oos.writeObject(m.calcul(dis.readInt()));
		}
	}
}