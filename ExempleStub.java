package rpc;

public class ExempleStub implements ExempleIfc
{ 

	private java.net.Socket s = new java.net.Socket("localhost",1234); 
	private java.io.DataOutputStream dos = new java.io.DataOutputStream(s.getOutputStream()); 
	private java.io.ObjectInputStream ois = new java.io.ObjectInputStream(s.getInputStream());
	 public String calcul(int val) throws Exception {
		this.dos.writeUTF("calcul");
		this.dos.writeInt(val);
		return (String)ois.readObject();
	}
	public ExempleStub(int taille) throws Exception {
		this.dos.writeUTF("constructeur");
		this.dos.writeInt(taille);
	}



}