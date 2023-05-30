package com.pacificos.broker;

import com.pacificos.comunicacion.Colores;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.awt.*;

public class Broker {
    public static void main(String[] args) throws InterruptedException {
        //Imprimir Banner
        imprimirBroker();
        //Imprimir informacion del banner
        imprimirInfoBroker();
        try (ZContext context = new ZContext()) {

            // Crear sockets frontend y backend
            ZMQ.Socket frontend = context.createSocket(SocketType.XSUB);
            ZMQ.Socket backend = context.createSocket(SocketType.XPUB);

            // Enlazar los sockets a sus respectivos puertos
            frontend.bind("tcp://localhost:5556"); // Publicadores se conectan aquí
            backend.bind("tcp://localhost:5557"); // Suscriptores se conectan aquí

            // Iniciar el proxy de broker
            ZMQ.proxy(frontend, backend, null);
        }

    }
    public static void imprimirInfoBroker(){
        System.out.println(Colores.ANSI_RED + "El broke se inicializa como proxy para redireccionar las solicitudes de varios cliente");
        System.out.println(Colores.ANSI_RED + "El puerto de conexion para los publicadores es el puerto 5556");
        System.out.println(Colores.ANSI_RED + "El puerto de conexion para los publicadores es el puerto 5557");
    }

    public static void imprimirBroker() throws InterruptedException{
        System.out.println(Colores.ANSI_RED + "==========================================================================================" + Colores.ANSI_RESET);
        System.out.println("" + Colores.ANSI_RED +
                "██████╗ ██████╗  ██████╗ ██╗  ██╗███████╗██████╗ \n" +
                "██╔══██╗██╔══██╗██╔═══██╗██║ ██╔╝██╔════╝██╔══██╗\n" +
                "██████╔╝██████╔╝██║   ██║█████╔╝ █████╗  ██████╔╝\n" +
                "██╔══██╗██╔══██╗██║   ██║██╔═██╗ ██╔══╝  ██╔══██╗\n" +
                "██████╔╝██║  ██║╚██████╔╝██║  ██╗███████╗██║  ██║\n" +
                "╚═════╝ ╚═╝  ╚═╝ ╚═════╝ ╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝");
        System.out.println(Colores.ANSI_RED + "==========================================================================================" + Colores.ANSI_RESET);
    }
}
