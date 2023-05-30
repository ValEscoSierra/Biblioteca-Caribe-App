package com.pacificos.gestor;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public interface InterfazServidor extends java.rmi.Remote {
    public boolean devolverLibro(int codCliente, String codLibro) throws RemoteException;

    public boolean renovarLibro(int codCliente, String codLibro) throws RemoteException, InterruptedException;

    public boolean solicitudPrestamo(int codCliente, String codLibro) throws RemoteException, MalformedURLException, NotBoundException, InterruptedException;
}
