package com.pacificos.actor;
import com.pacificos.comunicacion.Colores;
import com.pacificos.gestor.ImplGest;
import org.apache.commons.cli.*;

public class ActorPrestamo {
    static ActorPrestamoImpl miActor;

    public static void main(String[] args) throws InterruptedException, ParseException {
        imprimirPantallaInicial();

        Options options = new Options();
        options.addOption("p", "Puerto", true, "Puerto del gestor");

        //Leer y asignar argumentos
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        int puerto = Integer.parseInt(cmd.getOptionValue("p"));

        try {
            miActor = new ActorPrestamoImpl("rmi://localhost:" + puerto + "/ActorPrestamo", puerto);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void imprimirPantallaInicial() throws InterruptedException {
        System.out.println();
        System.out.println();
        System.out.println(Colores.ANSI_CYAN + "===========================================================" + Colores.ANSI_RESET);
        System.out.println("" + Colores.ANSI_CYAN +
                " █████╗  ██████╗████████╗ ██████╗ ██████╗     ██████╗ \n" +
                "██╔══██╗██╔════╝╚══██╔══╝██╔═══██╗██╔══██╗    ██╔══██╗\n" +
                "███████║██║        ██║   ██║   ██║██████╔╝    ██████╔╝\n" +
                "██╔══██║██║        ██║   ██║   ██║██╔══██╗    ██╔═══╝ \n" +
                "██║  ██║╚██████╗   ██║   ╚██████╔╝██║  ██║    ██║     \n" +
                "╚═╝  ╚═╝ ╚═════╝   ╚═╝    ╚═════╝ ╚═╝  ╚═╝    ╚═╝     ");
        System.out.println(Colores.ANSI_CYAN + "===========================================================" + Colores.ANSI_RESET);
    }

}

