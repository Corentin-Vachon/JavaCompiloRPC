package rpc;
import java.io.Writer;
import java.io.Reader;
import java.io.File;
import java.io.*;
import java.util.*;
import java.nio.file.*;

public class MonCompilo {

    public static void main(String args[]) throws IOException {

        String link = args[0];
        String port = args[1];

        String returnValue = ""; //
        String type = "";
        String methodeName = "";
        String param = "";
        String MatlabType = "";

        List<String> tabInput = new ArrayList<String>();
        // System.out.println(link);
        String classInt = link.substring(0,link.lastIndexOf("Ifc")); // Récupère le nom de l"interface
        // System.out.println(classInt);
        // new File("./tmp/").mkdir();

        String linkStub =  "./"+classInt+"Stub.java";
        String linkSkel =  "./"+classInt+"Skel.java";

        File file = new File(link); 
        File fileStub = new File(linkStub); 
        File fileSkel = new File(linkSkel); 

        BufferedReader br = new BufferedReader(new FileReader(file));  // Créé les buffers reader et writer pour pouvoir lire / écrire dans des fichiers
        BufferedWriter bw = new BufferedWriter(new FileWriter(fileStub));
        BufferedWriter bwSkel = new BufferedWriter(new FileWriter(fileSkel));

        tabInput = getTabInput(tabInput, br); // créé un tableau ou chaque élément correspond a une ligne de l'interface donnée en entrée
        String packageName = getPackage(tabInput); // récupère le nom du package

        if (packageName != "")  // Précise le nom du package si il y en a un
        {
            bw.write("package " + packageName +";\n");
            bwSkel.write("package " + packageName +";\n");
        }

        bw.write("\npublic class " + classInt + "Stub implements " + classInt + "Ifc\n{ \n\n"); // Créé la déclaration de la classe Stub
        StubWriter(bw, port);
        
        int i = 0; 
        while (i < tabInput.size()) // Pour le prototype de la fonctione dans l'interface, cette boucle récupère le nom du paramètre, le type, la type retournée et le nom de la méthode
        {
            String tmp = tabInput.get(i);
            if (tmp.contains("(") )
            {
                if (tmp.contains("(") && !tmp.contains("/"))
                {
                    param = getPrototype(tmp)[1];
                    type = getPrototype(tmp)[2];
                    returnValue = getTypeReturn(tmp);
                    methodeName = getMethodeName(tmp);
                    bw.write("\t"+getPrototype(tmp)[0] + " {\n");
                    bw.write("\t\t"+getCalcul(param, type, returnValue, methodeName));
                    getMethodeName(tmp);
                }
                MatlabType = "int";
                if (tmp.contains("//"))
                {
                    MatlabType = getPrototype(tmp)[1];
                }
            }
            i = i + 1;
        }

        constructorStubWriter( bw, classInt, MatlabType); // écrit les premières ligne dans le main de Stub
        bwSkel.write("\n\npublic class " + classInt + "Skel\n{ \n\n"); // Créé la déclaration de la classe
        bw.write("\n\n}");
        editSkel(bwSkel, port, methodeName, MatlabType, classInt);
        bw.close(); // ferme les connexions
        bwSkel.close();

    } 

    public static List<String> getTabInput(List<String> tabInput, BufferedReader br) throws IOException { // Créer à partir de l'interface un tableau ou chaque index donne accès à une ligne de l'interface
        String st; 
        while ((st = br.readLine()) != null) 
        {
            if (st.contains("(") && st.contains("/") == false)
            {
                // System.out.println(st);
            }
            tabInput.add(st);
        }
        return(tabInput);
    }

    public static String getPackage(List<String> tabInput) { // Récupère le nom du package
        int i = 0; 
        String packageName = "";
        while (i < tabInput.size())
        {
            if (tabInput.get(i).contains("package"))
            {
                packageName = tabInput.get(i).substring(tabInput.get(i).lastIndexOf("package")+8,tabInput.get(i).lastIndexOf(";"));
                return (packageName);
            }
            i = i + 1;

        }
        return (null);
    }

    public static String getMethodeName(String chaine) // Retourne le nom d'une méthode pour une ligne donnée
    {
        String tmp = chaine.substring(0,chaine.lastIndexOf("("));
        String methode = tmp.substring(tmp.lastIndexOf(" ")+1, tmp.length());
        return(methode);
    }

    public static String getTypeReturn(String chaine)  // Retourne le type renvoyé par une méthode pour une ligne donnée
    {
        String tmp = chaine.substring(0,chaine.lastIndexOf("("));
        String tmp2 = tmp.substring(0, tmp.lastIndexOf(" "));
        String type = tmp2.substring(tmp2.lastIndexOf(" ")+1, tmp2.length());
        return(type);
    }
 
    public static String getCalcul(String type, String arg, String returnValue, String methodeName) throws IOException { // Rédige le code de la méthode calcul du Skel
        String typeMaj = setMajuscule(type);
        return("this.dos.writeUTF(\""+methodeName+"\");\n\t\tthis.dos.write"+typeMaj+"("+arg+");\n\t\treturn ("+returnValue+")ois.readObject();\n\t}");
    }

    public static void constructorStubWriter(BufferedWriter bw, String name, String MatlabType) throws IOException { // construit le constructeur du Stub
        String constructor = "\n\tpublic "+name+"Stub("+MatlabType+" taille) throws Exception {\n\t\tthis.dos.writeUTF(\"constructeur\");\n\t\tthis.dos.write"+setMajuscule(MatlabType)+"(taille);\n\t}\n\n";
        bw.write(constructor);
    }   

    public static void StubWriter(BufferedWriter bw, String port) throws IOException {  // écrit les premières lignes du stub, les lignes permettant la connexion entre le stub et le skel
        bw.write("\tprivate java.net.Socket s = new java.net.Socket(\"localhost\","+port+"); \n\tprivate java.io.DataOutputStream dos = new java.io.DataOutputStream(s.getOutputStream()); \n\tprivate java.io.ObjectInputStream ois = new java.io.ObjectInputStream(s.getInputStream());\n" ); 

    }

    public static String[] getPrototype(String text) { // Retourne une liste de 3 éléments, le prototype, le type et l'argument d'une ligne contentant une méthode
        String param = text.substring(text.lastIndexOf("(") + 1  , text.lastIndexOf(")"));
        String arg = param.substring(param.lastIndexOf(" ") + 1,param.length());
        String type = param.substring(0, param.lastIndexOf(" "));
        String prototype = text.substring(3, text.lastIndexOf("(") + 1) + type + " " + arg + text.substring(text.lastIndexOf(")"), text.length()-1);
        String[] prototypeArray = {prototype, type, arg};
        // System.out.println(type);
        return (prototypeArray);
    }

    public static String setMajuscule(String chaine) // Renvoie la chaine donnée en paramètre avec une majuscule
    {   
        char[] char_table = chaine.toCharArray();
        char_table[0]=Character.toUpperCase(char_table[0]);
        chaine = new String(char_table);
        return(chaine);
    }

    public static void editSkel(BufferedWriter bw,String port, String methodeName, String typeMatlab, String nameClass) throws IOException { // créé le code de la classe skeleton
        // System.out.println(methodeName);
        String esp = "\n\t\t";
        bw.write("\t public static void main(String [] arg) throws Exception {");
        bw.write(esp + " "+nameClass +" m = null;");
        bw.write(esp + "java.net.ServerSocket sos = new java.net.ServerSocket("+port+");");
        bw.write(esp + "java.net.Socket s = sos.accept();");

        bw.write (esp + "java.io.DataInputStream dis = new java.io.DataInputStream(s.getInputStream());");
        bw.write (esp + "java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(s.getOutputStream());");

        bw.write (esp + "String fonction = dis.readUTF();");
        bw.write (esp + "if (fonction.equals(\"constructeur\")) {");
        bw.write (esp + "\tm = new "+nameClass+"(dis.read"+setMajuscule(typeMatlab)+"());");
        bw.write(esp + "}" );

        bw.write(esp + "fonction = dis.readUTF();");
        bw.write(esp + "if (fonction.equals(\""+methodeName+"\")) {");
        bw.write(esp + "\toos.writeObject(m."+methodeName+"(dis.read"+setMajuscule(typeMatlab+"()));"));
        bw.write(esp + "}");
        bw.write("\n\t}");
        bw.write("\n}");
    }

}