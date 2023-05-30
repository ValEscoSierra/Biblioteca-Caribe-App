package com.pacificos.escritor;
import com.pacificos.comunicacion.Colores;
import org.apache.commons.cli.*;

public class Escritor {
    static EscritorImpl escritorImpl;
    static String archivoLibros;
    static String archivoPrestamos;
    static String puerto;
    public static void main(String[] args) throws InterruptedException, ParseException{
        imprimirPantallaInicial();
        //Crear las diferentes banderas de argumentos
        Options options = new Options();
        options.addOption("l", "archivoLibros", true, "Archivo de libros de la base de datos");
        options.addOption("p", "archivoPrestamos", true, "Archivo de libros de la base de datos");
        options.addOption("d", "direccion", true, "Direccion del escritor");

        //Leer y asignar argumentos
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        archivoLibros = cmd.getOptionValue("l");
        puerto = cmd.getOptionValue("d");
        archivoPrestamos=cmd.getOptionValue("p");

        try {
            EscritorImpl miEscritor = new EscritorImpl("rmi://localhost:" + puerto + "/Escritor", archivoLibros, archivoPrestamos, Integer.parseInt(puerto));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void imprimirPantallaInicial() throws InterruptedException {
        System.out.println();
        System.out.println();
        System.out.println(Colores.ANSI_BLUE + "==============================================================" + Colores.ANSI_RESET);
        System.out.println("" + Colores.ANSI_BLUE +
                "███████╗███████╗ ██████╗██████╗ ██╗████████╗ ██████╗ ██████╗ \n" +
                "██╔════╝██╔════╝██╔════╝██╔══██╗██║╚══██╔══╝██╔═══██╗██╔══██╗\n" +
                "█████╗  ███████╗██║     ██████╔╝██║   ██║   ██║   ██║██████╔╝\n" +
                "██╔══╝  ╚════██║██║     ██╔══██╗██║   ██║   ██║   ██║██╔══██╗\n" +
                "███████╗███████║╚██████╗██║  ██║██║   ██║   ╚██████╔╝██║  ██║\n" +
                "╚══════╝╚══════╝ ╚═════╝╚═╝  ╚═╝╚═╝   ╚═╝    ╚═════╝ ╚═╝  ╚═╝");
        System.out.println(Colores.ANSI_BLUE + "==============================================================" + Colores.ANSI_RESET);
        Thread.sleep(3000);
    }
}
