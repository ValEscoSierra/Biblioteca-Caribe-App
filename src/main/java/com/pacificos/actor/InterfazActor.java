package com.pacificos.actor;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;

public interface InterfazActor extends java.rmi.Remote{

    public boolean solicitudPrestamo(int codCliente, String CodigoLibtro) throws java.rmi.RemoteException, MalformedURLException, NotBoundException, InterruptedException;
}
