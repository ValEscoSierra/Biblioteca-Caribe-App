package com.pacificos.gestor;

import com.pacificos.actor.ActorGeneral;
import com.pacificos.actor.InterfazActor;
import com.pacificos.comunicacion.Colores;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class ImplGest extends java.rmi.server.UnicastRemoteObject implements InterfazServidor {

    protected ImplGest(String name, int puerto, int tiempoFallo) throws RemoteException {
        super();
        try{
            //Registro del objeto remoto por su nombre
            Registry r = LocateRegistry.createRegistry(puerto);
            System.out.println(Colores.ANSI_PURPLE + "Rebind Object " + name + Colores.ANSI_RESET);
            Naming.rebind(name, this);
            if(puerto == 1099){
                destroyAfterDelay(tiempoFallo);
            }
        }catch (Exception e){
            //Manejo de excepciones
            System.out.println("Excepcion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void destroyAfterDelay(int tiempoFallo) {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(this::destroy, tiempoFallo, TimeUnit.SECONDS);
        executorService.shutdown();
    }

    private void destroy() {
        try {
            UnicastRemoteObject.unexportObject(this, true);
            System.out.println(Colores.ANSI_RED + "================================================");
            System.out.println("" +
                            "███████╗██████╗ ██████╗  ██████╗ ██████╗         ██╗" + "\n" +
                            "██╔════╝██╔══██╗██╔══██╗██╔═══██╗██╔══██╗    ██╗██╔╝" + "\n" +
                            "█████╗  ██████╔╝██████╔╝██║   ██║██████╔╝    ╚═╝██║" + "\n" +
                            "██╔══╝  ██╔══██╗██╔══██╗██║   ██║██╔══██╗    ██╗██║" + "\n" +
                            "███████╗██║  ██║██║  ██║╚██████╔╝██║  ██║    ╚═╝╚██╗" + "\n" +
                            "╚══════╝╚═╝  ╚═╝╚═╝  ╚═╝ ╚═════╝ ╚═╝  ╚═╝        ╚═╝");
        } catch (NoSuchObjectException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean devolverLibro(int codCliente, String codLibro) throws RemoteException {
        //Imprime la operación que se recibe
        imprimirDevolucionLibro(codCliente,codLibro);

        //Publica la operación para los actores
        publicarMensaje("dev", codCliente, codLibro);

        //Retorna verdadero al PS
        return true;
    }

    @Override
    public boolean renovarLibro(int codCliente, String codLibro) throws RemoteException, InterruptedException {
        //Imprime la operación que se recibe
        imprimirRenovarLibro(codCliente, codLibro);

        //Publica la operación para los actores
        publicarMensaje("ren", codCliente, codLibro);

        //Retorna verdadero al PS
        return true;
    }

    @Override
    public boolean solicitudPrestamo(int codCliente, String codLibro) throws RemoteException, MalformedURLException, NotBoundException, InterruptedException {
        //Imprimir la operación que se recibe
        imprimirPrestamoLibro(codCliente, codLibro);

        //Conexión con el actor prestamo
        InterfazActor interfazActor = (InterfazActor) Naming.lookup("rmi://localhost:" + 1093 + "/ActorPrestamo");

        //Llama al actor de prestamo para ejecutar el prestamo
        boolean exito = interfazActor.solicitudPrestamo(codCliente, codLibro);

        //

        //Imprimir recibo de operacion
        imprimirReciboPrestamo(exito, codCliente, codLibro);


        Thread.sleep(2000);
        //Retornar resultado de la operacion
        return exito;
    }

    public static  void imprimirDevolucionLibro(int codCliente, String codLibro){
        System.out.println(Colores.ANSI_YELLOW + "PETICION RECIBIDA!");
        System.out.println(Colores.ANSI_YELLOW + "Tipo de peticion: " + Colores.ANSI_RESET + "Devolucion" + Colores.ANSI_RESET );
        System.out.println(Colores.ANSI_YELLOW + "Codigo del libro: "+ Colores.ANSI_RESET + codLibro+ Colores.ANSI_RESET);
        System.out.println(Colores.ANSI_YELLOW + "Codigo de cliente: " + Colores.ANSI_RESET +codCliente + Colores.ANSI_RESET);
        System.out.println(Colores.ANSI_PURPLE + "=========================================================" + Colores.ANSI_RESET);
    }

    public static void imprimirRenovarLibro(int codCliente, String codLibro){
        System.out.println(Colores.ANSI_CYAN + "PETICION RECIBIDA!");
        System.out.println(Colores.ANSI_CYAN + "Tipo de peticion: " + Colores.ANSI_RESET + "Renovacion" + Colores.ANSI_RESET );
        System.out.println(Colores.ANSI_CYAN + "Codigo del libro: "+ Colores.ANSI_RESET + codLibro + Colores.ANSI_RESET);
        System.out.println(Colores.ANSI_CYAN + "Codigo de cliente: " + Colores.ANSI_RESET +  codCliente + Colores.ANSI_RESET);
        System.out.println(Colores.ANSI_PURPLE + "=========================================================" + Colores.ANSI_RESET);
    }

    public static void imprimirPrestamoLibro(int codCliente, String codLibro){
        System.out.println(Colores.ANSI_GREEN + "PETICION RECIBIDA!");
        System.out.println(Colores.ANSI_GREEN + "Tipo de peticion: " + Colores.ANSI_RESET + "Prestamo" + Colores.ANSI_RESET );
        System.out.println(Colores.ANSI_GREEN + "Codigo del libro: "+ Colores.ANSI_RESET + codLibro + Colores.ANSI_RESET);
        System.out.println(Colores.ANSI_GREEN + "Codigo de cliente: " + Colores.ANSI_RESET +  codCliente + Colores.ANSI_RESET);
        System.out.println(Colores.ANSI_PURPLE + "=========================================================" + Colores.ANSI_RESET);
    }

    public static void publicarMensaje(String topico, int codCliente, String codLibro){
        try (ZContext context = new ZContext()) {
            ZMQ.Socket publisher = context.createSocket(ZMQ.PUB);
            publisher.connect("tcp://localhost:5556");

            Thread.sleep(3000);
            String message = topico + ";" + codCliente + ";" + codLibro;
            publisher.send(message.getBytes(ZMQ.CHARSET), 0);
            imprimirPublicacion(message);

            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void imprimirPublicacion(String message){
        String[] piezas = message.split(";");
        System.out.println(Colores.ANSI_PURPLE + "PUBLICACION REALIZADA!");
        String topico;
        if(piezas[0].equals("dev")){
            topico = "Devolucion";
        }
        else{
            topico = "Renovacion";
        }
        System.out.println(Colores.ANSI_PURPLE + "Topico de pub: " + Colores.ANSI_RESET + topico);
        System.out.println(Colores.ANSI_PURPLE + "Codigo del cliente: " + Colores.ANSI_RESET + piezas[1]);
        System.out.println(Colores.ANSI_PURPLE + "Codigo del libro: " + Colores.ANSI_RESET + piezas[2]);
        System.out.println(Colores.ANSI_PURPLE + "=========================================================" + Colores.ANSI_RESET);
    }

    public static void imprimirReciboPrestamo(boolean exito, int codCliente, String codLibro){
        System.out.println(Colores.ANSI_PURPLE + "PRESTAMO FINALIZADO!");
        if(exito){
            System.out.println(Colores.ANSI_PURPLE + "Resultado: " + Colores.ANSI_RESET + "Exitoso");
            System.out.println(Colores.ANSI_PURPLE + "Codigo del cliente: " + Colores.ANSI_RESET + codCliente);
            System.out.println(Colores.ANSI_PURPLE + "Codigo del libro: " + Colores.ANSI_RESET + codLibro);
            System.out.println(Colores.ANSI_PURPLE + "=========================================================" + Colores.ANSI_RESET);
        }
        else{
            System.out.println(Colores.ANSI_PURPLE + "Resultado: " + Colores.ANSI_RESET + "Fallido");
            System.out.println(Colores.ANSI_PURPLE + "Codigo del cliente: " + Colores.ANSI_RESET + codCliente);
            System.out.println(Colores.ANSI_PURPLE + "Codigo del libro: " + Colores.ANSI_RESET + codLibro);
            System.out.println(Colores.ANSI_PURPLE + "=========================================================" + Colores.ANSI_RESET);
        }
    }
}