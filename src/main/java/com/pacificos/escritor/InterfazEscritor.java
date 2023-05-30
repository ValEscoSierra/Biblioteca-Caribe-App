package com.pacificos.escritor;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
public interface InterfazEscritor extends java.rmi.Remote {
    public boolean escribirDevolucion(int codCliente, String codLibro) throws RemoteException;

    public boolean escribirRenovacion(int codCliente, String codLibro) throws RemoteException;

    public boolean escribirPrestamo(int codCliente, String codLibro) throws RemoteException;

    public boolean entrar(int processId, char tipoOperacion, int codCliente, String codLibro) throws RemoteException, MalformedURLException, NotBoundException;

    public boolean solicitudOperacion(char tipoOperacion, int codCliente, String codLibro) throws RemoteException, MalformedURLException, NotBoundException;

    public void salir(int processId) throws RemoteException;
}
