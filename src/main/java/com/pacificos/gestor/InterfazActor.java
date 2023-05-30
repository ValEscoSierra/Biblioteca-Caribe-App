package com.pacificos.gestor;

public interface InterfazActor extends java.rmi.Remote{

    public boolean solicitudPrestamo(int codCliente, String CodigoLibtro) throws java.rmi.RemoteException;
}
