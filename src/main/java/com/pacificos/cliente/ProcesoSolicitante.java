package com.pacificos.cliente;
import com.pacificos.comunicacion.Colores;
import com.pacificos.gestor.InterfazServidor;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import org.apache.commons.cli.*;
import java.util.concurrent.*;

public class ProcesoSolicitante {
    static int codCliente;
    static private ExecutorService executor;
    static ArrayList<Peticion> listaPeticiones = new ArrayList<>();

    static ArrayList<Double> listaTiemposRespuesta = new ArrayList<>();

    //MAIN DEL PROCESO:
    public static void main(String[] args) throws InterruptedException, ParseException, IOException  {
        //Imprimir tipo de proceso
        imprimirPantallaInicial();
        executor = Executors.newSingleThreadExecutor();

        //Crear las diferentes banderas de argumentos
        Options options = new Options();
        options.addOption("i", "idCliente", true, "Id del cliente");
        options.addOption("a", "archivo", true, "Archivo de peticiones");

        //Leer y asignar argumentos
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        codCliente = Integer.parseInt(cmd.getOptionValue("i"));
        String archivoLeer = cmd.getOptionValue("a");

        //Imprimir datos iniciales (ARGUMENTOS)
        imprimirDatosIniciales(archivoLeer);

        //Lee peticiones del archivo
        leerPeticiones(archivoLeer);

        //Imprimir peticiones cargadas
        imprimirLecturaPeticiones();

        //Envia peticiones al gestor
        try {
            InterfazServidor intServidor1 = (InterfazServidor) Naming.lookup("rmi://localhost:1099/GestorCarga");
            InterfazServidor intServidor2 = (InterfazServidor) Naming.lookup("rmi://localhost:1100/GestorCarga");
            enviarPeticiones(intServidor1, intServidor2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        //Imprimir pantalla final
        imprimirPantallaFinal();
    }

    //OPERACIONES LÓGICAS:
    public static void leerPeticiones(String archivo) throws IOException {
        // Crear un objeto FileReader con la ruta del archivo
        FileReader fileReader = new FileReader(archivo);

        // Crear un objeto BufferedReader para leer el contenido del archivo
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        try {
            String line;
            // Leer el archivo línea por línea
            while ((line = bufferedReader.readLine()) != null) {
                //Crear nueva peticion
                Peticion pet = new Peticion();

                // Procesar la línea leída
                String[] datos = line.split(";");

                //Llenar datos de peticion

                pet.setCodCliente(codCliente);
                pet.setTipo(datos[0]);
                pet.setCodLibro(datos[1]);
                pet.setSede(Integer.parseInt(datos[2]));
                
                //Añadir a las peticiones por hacer
                listaPeticiones.add(pet);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // Cerrar el BufferedReader y el FileReader para liberar recursos
            bufferedReader.close();
            fileReader.close();
        }
    }

    public static void enviarPeticiones(InterfazServidor intServidor1, InterfazServidor intServidor2) throws RemoteException, InterruptedException, MalformedURLException, NotBoundException {
        for (Peticion pet : listaPeticiones) {
            //Imprimir peticion realizada:
            imprimirEnvioPeticion(pet);

            //Enviar peticion de tipo devolucion
            if(pet.getTipo().equals("D")){
                InterfazServidor finalIntServidor = intServidor1;
                Future<?> future = executor.submit(() -> {
                    try {
                        // Hacer la solicitud de renovacion al objeto remoto
                        boolean devuelto = enviarDevolucion(pet, finalIntServidor, intServidor2);
                        imprimirReciboDevolucion(devuelto);
                    } catch (RemoteException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
                intServidor1 = consultarCambioDeGestor(pet, intServidor1, intServidor2);
            }

            //Enviar peticion de tipo renovacion
            else if(pet.getTipo().equals("R")) {
                InterfazServidor finalIntServidor1 = intServidor1;
                Future<?> future = executor.submit(() -> {
                    try {
                        // Hacer la solicitud de renovacion al objeto remoto
                        boolean renovado = enviarRenovacion(pet, finalIntServidor1, intServidor2);
                        imprimirReciboRenovacion(renovado);
                    } catch (RemoteException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
                intServidor1 = consultarCambioDeGestor(pet, intServidor1, intServidor2);
            }

            //Enbiar peticion de tipo prestamo
            else if (pet.getTipo().equals("P")) {
                InterfazServidor finalIntServidor2 = intServidor1;
                Future<?> future = executor.submit(() -> {
                    try {
                        // Hacer la solicitud de prestamo al objeto remoto
                        boolean prestado = enviarPrestamo(pet, finalIntServidor2, intServidor2);
                        imprimirReciboPrestamo(prestado);
                    } catch (RemoteException | MalformedURLException | NotBoundException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
                intServidor1 = consultarCambioDeGestor(pet, intServidor1, intServidor2);
            }

            Thread.sleep(5000);
        }
    }

    public static boolean enviarDevolucion(Peticion pet, InterfazServidor intServ1, InterfazServidor intServ2) throws RemoteException {
        boolean devuelto = false;
        if(pet.getSede() == 1){
            devuelto = intServ1.devolverLibro(pet.getCodCliente(), pet.getCodLibro());
        }
        else if(pet.getSede() == 2){
            devuelto = intServ2.devolverLibro(pet.getCodCliente(), pet.getCodLibro());
        }
        return devuelto;
    }

    public static boolean enviarRenovacion(Peticion pet, InterfazServidor intServ1, InterfazServidor intServ2) throws RemoteException, InterruptedException {
        boolean renovado = false;
        if(pet.getSede() == 1){
            renovado = intServ1.renovarLibro(pet.getCodCliente(), pet.getCodLibro());
        }
        else if(pet.getSede() == 2){
            renovado = intServ2.renovarLibro(pet.getCodCliente(), pet.getCodLibro());
        }
        return renovado;
    }

    public static boolean enviarPrestamo(Peticion pet, InterfazServidor intServ1, InterfazServidor intServ2) throws RemoteException, MalformedURLException, NotBoundException, InterruptedException {
        boolean prestado = false;
        if(pet.getSede() == 1){
            double tiempoInicio = System.currentTimeMillis();
            prestado = intServ1.solicitudPrestamo(pet.getCodCliente(), pet.getCodLibro());
            double tiempoFinal = System.currentTimeMillis();
            double tiempoRespuesta = tiempoFinal - tiempoInicio;
            escribirTiempoRespuesta(tiempoRespuesta - 2000);
        }
        else if(pet.getSede() == 2){
            double tiempoInicio = System.currentTimeMillis();
            prestado = intServ2.solicitudPrestamo(pet.getCodCliente(), pet.getCodLibro());
            double tiempoFinal = System.currentTimeMillis();
            double tiempoRespuesta = tiempoFinal - tiempoInicio;
            escribirTiempoRespuesta(tiempoRespuesta - 2000);
        }
        return prestado;
    }

    public static void escribirTiempoRespuesta(double tiempo){
        String rutaArchivo = "Requerimientos/TiemposRespuesta.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(rutaArchivo, true))) {
            String tiempoStr = Double.toString(tiempo);
            writer.write(tiempoStr + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static InterfazServidor consultarCambioDeGestor(Peticion pet, InterfazServidor intServidor1, InterfazServidor intServidor2) throws InterruptedException, NotBoundException, MalformedURLException, RemoteException {
        boolean gestorEjecutandose = isPortInUse("localhost", 1099);
        if(!gestorEjecutandose){
            System.out.println(Colores.ANSI_GREEN + "El servidor no respondió en el tiempo esperado. Intentando conectar al servidor de respaldo..." + Colores.ANSI_RESET);
            intServidor1 = (InterfazServidor) Naming.lookup("rmi://localhost:1101/GestorCarga");
            imprimirEnvioPeticion(pet);
            switch (pet.getTipo()) {
                case "D":
                    boolean renovado = intServidor1.devolverLibro(pet.getCodCliente(), pet.getCodLibro());
                    imprimirReciboRenovacion(renovado);
                    break;
                case "R":
                    boolean devuelto = intServidor1.renovarLibro(pet.getCodCliente(), pet.getCodLibro());
                    imprimirReciboDevolucion(devuelto);
                    break;
                case "P":
                    boolean prestamo = enviarPrestamo(pet, intServidor1, intServidor2);
                    imprimirReciboPrestamo(prestamo);
                    break;
            }
        }
        return intServidor1;
    }


    //OPERACIONES DE IMPRESIÓN:
    public static void imprimirPantallaInicial() throws InterruptedException {
        System.out.println();
        System.out.println();
        System.out.println(Colores.ANSI_GREEN + "========================================================================================" + Colores.ANSI_RESET);
        System.out.println("" + Colores.ANSI_GREEN +
                " ██████╗██╗     ██╗███████╗███╗   ██╗████████╗███████╗              ██████╗ ███████╗\n" +
                "██╔════╝██║     ██║██╔════╝████╗  ██║╚══██╔══╝██╔════╝              ██╔══██╗██╔════╝\n" +
                "██║     ██║     ██║█████╗  ██╔██╗ ██║   ██║   █████╗      █████╗    ██████╔╝███████╗\n" +
                "██║     ██║     ██║██╔══╝  ██║╚██╗██║   ██║   ██╔══╝      ╚════╝    ██╔═══╝ ╚════██║\n" +
                "╚██████╗███████╗██║███████╗██║ ╚████║   ██║   ███████╗              ██║     ███████║\n" +
                " ╚═════╝╚══════╝╚═╝╚══════╝╚═╝  ╚═══╝   ╚═╝   ╚══════╝              ╚═╝     ╚══════╝");
        System.out.println(Colores.ANSI_GREEN + "========================================================================================" + Colores.ANSI_RESET);
        Thread.sleep(4000);
    }

    public static void imprimirDatosIniciales(String archivoLeer) throws InterruptedException {
        System.out.println(Colores.ANSI_GREEN +"ID cliente: "+codCliente+Colores.ANSI_RESET);
        System.out.println(Colores.ANSI_GREEN +"Nombre archivo requerimientos: "+archivoLeer+Colores.ANSI_RESET);
        System.out.println(Colores.ANSI_GREEN + "========================================================================================" + Colores.ANSI_RESET);
        Thread.sleep(2000);
    }

    public static void imprimirLecturaPeticiones() throws InterruptedException {
        System.out.println(Colores.ANSI_GREEN + listaPeticiones.size() + " peticiones cargadas con exito!" + Colores.ANSI_RESET);
        System.out.println(Colores.ANSI_GREEN + "========================================================================================" + Colores.ANSI_RESET);
        Thread.sleep(2000);
    }

    public static void imprimirEnvioPeticion(Peticion p){
        if(p.getTipo().equals("P")){
            System.out.println(Colores.ANSI_CYAN + "NUEVA PETICION ENVIADA!");
            System.out.println(Colores.ANSI_CYAN + "Tipo de peticion: " + Colores.ANSI_RESET + "Prestamo");
            System.out.println(Colores.ANSI_CYAN + "Codigo del libro: " + Colores.ANSI_RESET + p.getCodLibro());
            System.out.println(Colores.ANSI_CYAN + "Codigo de cliente: " + Colores.ANSI_RESET + +p.getCodCliente());
        }
        if(p.getTipo().equals("D")){
            System.out.println(Colores.ANSI_YELLOW + "NUEVA PETICION ENVIADA!");
            System.out.println(Colores.ANSI_YELLOW + "Tipo de peticion: " + Colores.ANSI_RESET + "Devolucion");
            System.out.println(Colores.ANSI_YELLOW + "Codigo del libro: "+ Colores.ANSI_RESET + p.getCodLibro());
            System.out.println(Colores.ANSI_YELLOW + "Codigo de cliente: " + Colores.ANSI_RESET+ p.getCodCliente());
        }
        else if(p.getTipo().equals("R")){
            System.out.println(Colores.ANSI_PURPLE + "NUEVA PETICION ENVIADA!");
            System.out.println(Colores.ANSI_PURPLE + "Tipo de peticion: " + Colores.ANSI_RESET + "Renovacion");
            System.out.println(Colores.ANSI_PURPLE + "Codigo del libro: "+ Colores.ANSI_RESET + p.getCodLibro());
            System.out.println(Colores.ANSI_PURPLE + "Codigo de cliente: " + Colores.ANSI_RESET + p.getCodCliente());
        }
        System.out.println(Colores.ANSI_GREEN + "========================================================================================" + Colores.ANSI_RESET);
    }

    public static void imprimirReciboDevolucion(boolean devuelto) throws InterruptedException{
        System.out.println(Colores.ANSI_YELLOW + "RESPUESTA ENTRANTE:" + Colores.ANSI_RESET);
        if(devuelto){
            System.out.println(Colores.ANSI_YELLOW + "Libro devuelto con exito!" + Colores.ANSI_RESET);
        }else{
            System.out.println(Colores.ANSI_RED + "Fallo en la devolucion" + Colores.ANSI_RESET);
        }
        System.out.println(Colores.ANSI_GREEN + "========================================================================================" + Colores.ANSI_RESET);
    }

    public static void imprimirReciboRenovacion(boolean renovado) throws InterruptedException {
        System.out.println(Colores.ANSI_PURPLE + "RESPUESTA ENTRANTE:" + Colores.ANSI_RESET);
        if(renovado){
            System.out.println(Colores.ANSI_PURPLE + "Libro renovado con exito!" + Colores.ANSI_RESET);
        }else{
            System.out.println(Colores.ANSI_RED + "Fallo en la renovacion!" + Colores.ANSI_RESET);
        }
        System.out.println(Colores.ANSI_GREEN + "========================================================================================" + Colores.ANSI_RESET);
    }

    public static void imprimirReciboPrestamo(boolean renovado) throws InterruptedException {
        System.out.println(Colores.ANSI_CYAN + "RESPUESTA ENTRANTE:" + Colores.ANSI_RESET);
        if(renovado){
            System.out.println(Colores.ANSI_CYAN + "Prestamo realizado con exito!" + Colores.ANSI_RESET);
        }else{
            System.out.println(Colores.ANSI_CYAN + "Fallo en el prestamo!" + Colores.ANSI_RESET);
        }
        System.out.println(Colores.ANSI_GREEN + "========================================================================================" + Colores.ANSI_RESET);
    }


    public static void imprimirPantallaFinal() throws InterruptedException {
        System.out.println(Colores.ANSI_GREEN +
                "███████╗██╗███╗   ██╗ █████╗ ██╗     \n" +
                "██╔════╝██║████╗  ██║██╔══██╗██║     \n" +
                "█████╗  ██║██╔██╗ ██║███████║██║     \n" +
                "██╔══╝  ██║██║╚██╗██║██╔══██║██║     \n" +
                "██║     ██║██║ ╚████║██║  ██║███████╗\n" +
                "╚═╝     ╚═╝╚═╝  ╚═══╝╚═╝  ╚═╝╚══════╝");
        System.out.println("Todas las peticiones han sido realizadas!");
        Thread.sleep(120000);}

    public static boolean isPortInUse(String host, int port) {
        // asume que el puerto no está en uso
        boolean result = false;

        try {
            (new Socket(host, port)).close();
            // El puerto está en uso si logramos conectarnos con éxito
            result = true;
        } catch(IOException e) {
            // no pudimos conectarnos, esto podría ser indicativo de que el puerto no está en uso
        }

        return result;
    }
}