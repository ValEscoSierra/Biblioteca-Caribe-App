package com.pacificos.actor;

import com.pacificos.comunicacion.Colores;
import com.pacificos.escritor.InterfazEscritor;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ActorPrestamoImpl extends UnicastRemoteObject implements InterfazActor {

    protected ActorPrestamoImpl(String name, int puerto) throws RemoteException {
        super();
        try{
            //Registro del objeto remoto por su nombre
            Registry r = LocateRegistry.createRegistry(puerto);
            System.out.println(Colores.ANSI_CYAN + "Rebind Object " + name + Colores.ANSI_RESET);
            Naming.rebind(name, this);
        }catch (Exception e){
            //Manejo de excepciones
            System.out.println("Excepcion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean solicitudPrestamo(int codCliente, String CodigoLibtro) throws RemoteException, MalformedURLException, NotBoundException, InterruptedException {
        //Imprimir recepción del prestamo
        imprimirReciboMensaje(codCliente, CodigoLibtro);

        //Enviar petición a través del escritor
        InterfazEscritor interfazEscritor = (InterfazEscritor) Naming.lookup("rmi://localhost:1096/Escritor");
        boolean exito = interfazEscritor.entrar(103, 'P', codCliente, CodigoLibtro);
        interfazEscritor.salir(103);

        //Retornar resultado de la petición
        return exito;
    }

    public static void imprimirReciboMensaje(int codCliente, String codLibro) throws MalformedURLException, NotBoundException, RemoteException, InterruptedException {
        System.out.println(Colores.ANSI_CYAN + "NUEVA SOLICITUD RECIBIDA!");
        System.out.println(Colores.ANSI_CYAN + "Tipo de mensaje: " + Colores.ANSI_RESET + "Prestamo");
        System.out.println(Colores.ANSI_CYAN + "Codigo del cliente: " + Colores.ANSI_RESET + codCliente);
        System.out.println(Colores.ANSI_CYAN + "Codigo del libro: " + Colores.ANSI_RESET + codLibro);
        System.out.println(Colores.ANSI_CYAN + "===========================================================" + Colores.ANSI_RESET);
    }
}
