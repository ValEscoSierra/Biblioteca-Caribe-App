package com.pacificos.actor;
import com.pacificos.comunicacion.Colores;
import com.pacificos.comunicacion.InfoSolicitud;
import com.pacificos.escritor.InterfazEscritor;
import org.apache.commons.cli.*;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class ActorGeneral {
    InfoSolicitud infoSolicitud;
    static String tipoActor;
    static int idActor;

    public static void main(String[] args) throws InterruptedException, ParseException, MalformedURLException, NotBoundException, RemoteException {
        //Crear las diferentes banderas de argumentos
        Options options = new Options();
        options.addOption("t", "tipoActor", true, "Tipo de actor (D o R)");
        options.addOption("i", "idActor", true, "Id del actor");

        //Leer y asignar argumentos
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        tipoActor = cmd.getOptionValue("t");
        idActor = Integer.parseInt(cmd.getOptionValue("i"));

        //Imprimir inicializacion del actor
        imprimirPantallaInicial(tipoActor);

        //Inicializar actor con su tipo
        inicializarActor(tipoActor);
    }

    public static void inicializarActor(String tipoActor) throws MalformedURLException, NotBoundException, RemoteException {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket subscriber = context.createSocket(ZMQ.SUB);
            subscriber.connect("tcp://localhost:5557");
            subscriber.subscribe(ZMQ.SUBSCRIPTION_ALL);

            while (!Thread.currentThread().isInterrupted()) {
                byte[] messageBytes = subscriber.recv(0);
                if (messageBytes != null) {
                    String message = new String(messageBytes, ZMQ.CHARSET);
                    if(message.startsWith(tipoActor)){
                        imprimirReciboMensaje(message);
                    }
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void imprimirPantallaInicial(String tipoActor) throws InterruptedException {
        if(tipoActor.equals("ren")){
            System.out.println();
            System.out.println(Colores.ANSI_YELLOW + "===========================================================");
            System.out.println("" +
                    " █████╗  ██████╗████████╗ ██████╗ ██████╗     ██████╗ \n" +
                    "██╔══██╗██╔════╝╚══██╔══╝██╔═══██╗██╔══██╗    ██╔══██╗\n" +
                    "███████║██║        ██║   ██║   ██║██████╔╝    ██████╔╝\n" +
                    "██╔══██║██║        ██║   ██║   ██║██╔══██╗    ██╔══██╗\n" +
                    "██║  ██║╚██████╗   ██║   ╚██████╔╝██║  ██║    ██║  ██║\n" +
                    "╚═╝  ╚═╝ ╚═════╝   ╚═╝    ╚═════╝ ╚═╝  ╚═╝    ╚═╝  ╚═╝");
            System.out.println(Colores.ANSI_YELLOW + "===========================================================" + Colores.ANSI_RESET);
        }
        else{
            System.out.println();
            System.out.println(Colores.ANSI_YELLOW + "===========================================================");
            System.out.println("" +
                    " █████╗  ██████╗████████╗ ██████╗ ██████╗     ██████╗ \n" +
                    "██╔══██╗██╔════╝╚══██╔══╝██╔═══██╗██╔══██╗    ██╔══██╗\n" +
                    "███████║██║        ██║   ██║   ██║██████╔╝    ██║  ██║\n" +
                    "██╔══██║██║        ██║   ██║   ██║██╔══██╗    ██║  ██║\n" +
                    "██║  ██║╚██████╗   ██║   ╚██████╔╝██║  ██║    ██████╔╝\n" +
                    "╚═╝  ╚═╝ ╚═════╝   ╚═╝    ╚═════╝ ╚═╝  ╚═╝    ╚═════╝ ");
            System.out.println(Colores.ANSI_YELLOW + "===========================================================" + Colores.ANSI_RESET);
        }
    }

    public static void imprimirReciboMensaje(String mensaje) throws MalformedURLException, NotBoundException, RemoteException, InterruptedException {
        System.out.println(Colores.ANSI_YELLOW + "NUEVO MENSAJE RECIBIDO!");
        String[] piezas = mensaje.split(";");
        String topico;
        char tipo;
        if(piezas[0].equals("dev")){
            topico = "Devolucion";
            tipo = 'D';
        }
        else{
            topico = "Renovacion";
            tipo = 'R';
        }
        System.out.println(Colores.ANSI_YELLOW + "Tipo de mensaje: " + Colores.ANSI_RESET + topico);
        System.out.println(Colores.ANSI_YELLOW + "Codigo del cliente: " + Colores.ANSI_RESET + piezas[1]);
        System.out.println(Colores.ANSI_YELLOW + "Codigo del libro: " + Colores.ANSI_RESET + piezas[2]);
        System.out.println(Colores.ANSI_YELLOW + "===========================================================" + Colores.ANSI_RESET);
        InterfazEscritor intCoordi = (InterfazEscritor) Naming.lookup("rmi://localhost:1096/Escritor");
        boolean exito = intCoordi.entrar(idActor, tipo, Integer.parseInt(piezas[1]), piezas[2]);
        intCoordi.salir(idActor);
    }
}
