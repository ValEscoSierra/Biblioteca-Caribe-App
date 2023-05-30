package com.pacificos.gestor;
import com.pacificos.comunicacion.Colores;
import org.apache.commons.cli.*;


public class GestorCarga {
    static ImplGest miGestor;

    private static final boolean detenerse = false;

    public static void main(String[] args) throws InterruptedException, ParseException {
        imprimirPantallaInicial();

        //Crear las diferentes banderas de argumentos
        Options options = new Options();
        options.addOption("p", "puerto", true, "Puerto del gestor");
        options.addOption("t", "tiempo", true, "Tiempo de fallo");

        //Leer y asignar argumentos
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        int puerto = Integer.parseInt(cmd.getOptionValue("p"));
        int tiempoFallo = Integer.parseInt(cmd.getOptionValue("t"));

        try {
            miGestor = new ImplGest("rmi://localhost:" + puerto + "/GestorCarga", puerto, tiempoFallo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void imprimirPantallaInicial() throws InterruptedException{
        System.out.println();
        System.out.println();
        System.out.println(Colores.ANSI_PURPLE + "=========================================================" + Colores.ANSI_RESET);
        System.out.println(" " + Colores.ANSI_PURPLE +
                "██████╗ ███████╗███████╗████████╗ ██████╗ ██████╗ \n" +
                "██╔════╝ ██╔════╝██╔════╝╚══██╔══╝██╔═══██╗██╔══██╗\n" +
                "██║  ███╗█████╗  ███████╗   ██║   ██║   ██║██████╔╝\n" +
                "██║   ██║██╔══╝  ╚════██║   ██║   ██║   ██║██╔══██╗\n" +
                "╚██████╔╝███████╗███████║   ██║   ╚██████╔╝██║  ██║\n" +
                " ╚═════╝ ╚══════╝╚══════╝   ╚═╝    ╚═════╝ ╚═╝  ╚═╝");
        System.out.println(Colores.ANSI_PURPLE + "=========================================================" + Colores.ANSI_RESET);
        Thread.sleep(3000);
    }
}