package com.pacificos.escritor;

import com.pacificos.comunicacion.Colores;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public class EscritorImpl extends java.rmi.server.UnicastRemoteObject implements InterfazEscritor {
    static String archivoLibros;
    static String archivoPrestamos;

    static private final Queue<Integer> colaSolicitudes = new LinkedList<>();
    private boolean recursoOcupado;

    protected EscritorImpl(String name, String archivoLibros, String archivoPrestamos, int puerto) throws RemoteException {
        super();
        try{
            EscritorImpl.archivoLibros = archivoLibros;
            EscritorImpl.archivoPrestamos = archivoPrestamos;
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
    public boolean escribirDevolucion(int codCliente, String codLibro)  {
        imprimirReciboPeticion("Devolucion", codCliente, codLibro);

        //Eliminación en base de datos prestamos
        ArrayList<Prestamo> prestamos = new ArrayList<>();
        boolean devolucion = eliminarPrestamoEnArchivo(archivoPrestamos, codLibro, codCliente, prestamos);
        if(devolucion){
            escribirDevolucionEnArchivo(prestamos);
        }
        else{
            return false;
        }

        //Actualización en base de datos libros
        ArrayList<Libro> libros = new ArrayList<>();
        actualizarEjemplaresEnArchivo(archivoLibros, codLibro, libros);
        escribirArchivoLibroNuevo(libros);

        return true;
    }

    @Override
    public boolean escribirRenovacion(int codCliente, String codLibro) {
        imprimirReciboPeticion("Renovacion", codCliente, codLibro);


        ArrayList<Prestamo> prestamos = new ArrayList<>();
        boolean renovacion = renovarFechaEnArchivo(archivoPrestamos, codCliente, codLibro, prestamos);
        if(renovacion){
            escribirArchivoPrestamosNuevo(prestamos);
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public boolean escribirPrestamo(int codCliente, String codLibro) {
        imprimirReciboPeticion("Prestamo", codCliente, codLibro);


        ArrayList<Libro> libros = new ArrayList<>();
        boolean descuentoLibro = descontarLibroEnArchivo(archivoLibros, codLibro, libros);
        if(descuentoLibro){
            LocalDate fechaActual = LocalDate.now();
            DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String fechaActualFormateada = fechaActual.format(formatoFecha);

            Prestamo prestamo = new Prestamo();
            prestamo.setCodLibro(codLibro);
            prestamo.setCodCliente(codCliente);
            prestamo.setFecha(fechaActualFormateada);

            escribirArchivoLibroNuevo(libros);
            agregarNuevoPrestamo(prestamo, archivoPrestamos);

            return true;
        }else{
            return false;
        }
    }

    private boolean descontarLibroEnArchivo(String archivo, String codLibro, ArrayList<Libro> libros) {
        try {
            // Crear un objeto FileReader
            FileReader fileReader = new FileReader(archivo);

            // Crear un objeto BufferedReader para leer el contenido del archivo
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            // Leer el archivo línea por línea
            String linea;
            while ((linea = bufferedReader.readLine()) != null) {
                Libro l = new Libro();
                String[] datos = linea.split(";");
                l.setCodLibro(datos[0]);
                l.setNombre(datos[1]);
                l.setAutor(datos[2]);
                l.setNumEjemplares(Integer.parseInt(datos[3]));
                if(l.getCodLibro().equals(codLibro)){
                    if(l.getNumEjemplares() == 0){
                        return false;
                    }
                    else{
                        l.setNumEjemplares(l.getNumEjemplares() - 1);
                    }
                }
                libros.add(l);
            }

            // Cerrar el BufferedReader
            bufferedReader.close();
        } catch (IOException e) {
            // Manejar excepciones relacionadas con la entrada y salida del archivo
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
        return true;
    }

    @Override
    public synchronized boolean entrar(int processId, char tipoOperacion, int codCliente, String codLibro) throws RemoteException, MalformedURLException, NotBoundException {
        colaSolicitudes.add(processId);

        while (recursoOcupado || colaSolicitudes.peek() != processId) {
            try {
                wait(); // Esperar hasta que el recurso esté disponible
            } catch (InterruptedException e) {
                System.err.println("Error en la espera: " + e.getMessage());
            }
        }

        recursoOcupado = true; // Asignar el recurso
        imprimirEntradaCoordi(processId, tipoOperacion, codCliente, codLibro);
        boolean exito = solicitudOperacion(tipoOperacion, codCliente, codLibro);
        return exito;
    }

    @Override
    public boolean solicitudOperacion(char tipoOperacion, int codCliente, String codLibro) throws RemoteException, MalformedURLException, NotBoundException {
        boolean exito = false;
        InterfazEscritor intEscritorS2 = (InterfazEscritor) Naming.lookup("rmi://localhost:1095/Escritor");

        if(tipoOperacion == 'D'){
            boolean respuestaS1 = this.escribirDevolucion(codCliente, codLibro);
            if(respuestaS1){
                System.out.println("DEVOLUCION REALIZADA EXITOSAMENTE EN S1!");
            }
            else{
                System.out.println("DATOS INCORRECTOS EN S1!");
            }
            boolean respuestaS2 = intEscritorS2.escribirDevolucion(codCliente, codLibro);
            if(respuestaS2){
                System.out.println("DEVOLUCION REALIZADA EXITOSAMENTE EN S2!");
            }
            else{
                System.out.println("DATOS INCORRECTOS EN S2!");
            }
            if(respuestaS1 && respuestaS2){
                exito = true;
            }
        }else if(tipoOperacion == 'R'){
            boolean respuestaS1= this.escribirRenovacion(codCliente,codLibro);

            if(respuestaS1){
                System.out.println("RENOVACION REALIZADA EXITOSAMENTE EN S1!");
            }else {
                System.out.println("DATOS INCORRECTOS EN S1!");
            }
            boolean respuestaS2=intEscritorS2.escribirRenovacion(codCliente,codLibro);
            if(respuestaS2){
                System.out.println("RENOVACION REALIZADA EXITOSAMENTE EN S2!");
            }
            else{
                System.out.println("DATOS INCORRECTOS EN S2!");
            }
            if(respuestaS1 && respuestaS2){
                exito = true;
            }
        }
        else if(tipoOperacion == 'P'){
            boolean respuestaS1= this.escribirPrestamo(codCliente,codLibro);

            if(respuestaS1){
                System.out.println("PRESTAMO REALIZADA EXITOSAMENTE EN S1!");
            }else {
                System.out.println("DATOS INCORRECTOS EN S1!");
            }
            boolean respuestaS2=intEscritorS2.escribirPrestamo(codCliente,codLibro);
            if(respuestaS2){
                System.out.println("PRESTAMO REALIZADA EXITOSAMENTE EN S2!");
            }
            else{
                System.out.println("DATOS INCORRECTOS EN S2!");
            }
            if(respuestaS1 && respuestaS2){
                exito = true;
            }
        }
        System.out.println(Colores.ANSI_RED + "==========================================================================================" + Colores.ANSI_RESET);
        return exito;
    }

    @Override
    public synchronized void salir(int processId) throws RemoteException {
        if (colaSolicitudes.peek() == processId) {
            colaSolicitudes.poll(); // Eliminar el cliente que sale de la región crítica
            recursoOcupado = false; // Liberar el recurso
            imprimirSalidaCoordi(processId);
            notifyAll(); // Notificar a todos los clientes en espera
        }
    }

    public static boolean renovarFechaEnArchivo(String archivo, int codCliente, String codLibro, ArrayList<Prestamo> lineasEscribir){
        boolean encontrado = false;
        try {
            // Crear un objeto FileReader
            FileReader fileReader = new FileReader(archivo);

            // Crear un objeto BufferedReader para leer el contenido del archivo
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            // Leer el archivo línea por línea
            String linea;
            while ((linea = bufferedReader.readLine()) != null) {
                Prestamo p = new Prestamo();
                String[] datos = linea.split(";");
                p.setCodLibro(datos[0]);
                p.setCodCliente(Integer.parseInt(datos[1]));
                p.setFecha(datos[2]);

                if(p.getCodLibro().equals(codLibro) && p.getCodCliente() == codCliente){
                    encontrado = true;
                    DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    LocalDate fecha = LocalDate.parse(p.getFecha(), formato);

                    // Añadir una semana a la fecha
                    LocalDate fechaSemanaDespues = fecha.plusWeeks(1);

                    // Convertir la fecha resultante al formato deseado y mostrarla
                    String fechaSemanaDespuesTexto = fechaSemanaDespues.format(formato);
                    p.setFecha(fechaSemanaDespuesTexto);
                }
                lineasEscribir.add(p);
            }

            // Cerrar el BufferedReader
            bufferedReader.close();
        } catch (IOException e) {
            // Manejar excepciones relacionadas con la entrada y salida del archivo
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
        return encontrado;
    }

    public static void escribirArchivoPrestamosNuevo(ArrayList<Prestamo> prestamos){
        try {
            // Crear un objeto FileWriter con la opción 'append' establecida en 'false' para sobrescribir el archivo
            FileWriter fileWriter = new FileWriter(archivoPrestamos, false);

            // Crear un objeto BufferedWriter para escribir en el archivo
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            // Escribir el contenido en el archivo
            for (Prestamo p : prestamos) {
                String linea;
                linea = p.getCodLibro() + ";" + p.getCodCliente() + ";" + p.getFecha() + "\n";
                bufferedWriter.write(linea);
            }

            // Asegurar que el contenido se haya escrito y cerrar el BufferedWriter
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            // Manejar excepciones relacionadas con la entrada y salida del archivo
            System.err.println("Error al escribir en el archivo: " + e.getMessage());
        }
    }

    public static boolean eliminarPrestamoEnArchivo(String archivo, String codLibro, int codCliente,ArrayList<Prestamo> prestamos){
        boolean encontrado=false;
        try {
            // Crear un objeto FileReader
            FileReader fileReader = new FileReader(archivo);

            // Crear un objeto BufferedReader para leer el contenido del archivo
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            // Leer el archivo línea por línea
            String linea;
            while ((linea = bufferedReader.readLine()) != null) {
                Prestamo p = new Prestamo();
                String[] datos = linea.split(";");
                p.setCodLibro(datos[0]);
                p.setCodCliente(Integer.parseInt(datos[1]));
                p.setFecha(datos[2]);
                if(Objects.equals(datos[0], codLibro) && Integer.parseInt(datos[1])==codCliente){
                    encontrado=true;
                   continue;
                }
                prestamos.add(p);
            }
            // Cerrar el BufferedReader
            bufferedReader.close();
        } catch (IOException e) {
            // Manejar excepciones relacionadas con la entrada y salida del archivo
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
        return encontrado;
    }

    public static void escribirDevolucionEnArchivo(ArrayList<Prestamo> prestamos){
        try {
            // Crear un objeto FileWriter con la opción 'append' establecida en 'false' para sobrescribir el archivo
            FileWriter fileWriter = new FileWriter(archivoPrestamos, false);

            // Crear un objeto BufferedWriter para escribir en el archivo
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            // Escribir el contenido en el archivo
            for (Prestamo p : prestamos) {
                String linea;
                linea = p.getCodLibro() + ";" + p.getCodCliente() + ";" + p.getFecha() + "\n";
                bufferedWriter.write(linea);
            }

            // Asegurar que el contenido se haya escrito y cerrar el BufferedWriter
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            // Manejar excepciones relacionadas con la entrada y salida del archivo
            System.err.println("Error al escribir en el archivo: " + e.getMessage());
        }
    }

    public static void actualizarEjemplaresEnArchivo(String archivo, String codLibro, ArrayList<Libro> libros){
        try {
            // Crear un objeto FileReader
            FileReader fileReader = new FileReader(archivo);

            // Crear un objeto BufferedReader para leer el contenido del archivo
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            // Leer el archivo línea por línea
            String linea;
            while ((linea = bufferedReader.readLine()) != null) {
                Libro l = new Libro();
                String[] datos = linea.split(";");
                l.setCodLibro(datos[0]);
                l.setNombre(datos[1]);
                l.setAutor(datos[2]);
                l.setNumEjemplares(Integer.parseInt(datos[3]));

                if(l.getCodLibro().equals(codLibro)){
                    l.setNumEjemplares(l.getNumEjemplares() + 1);
                }

                libros.add(l);
            }

            // Cerrar el BufferedReader
            bufferedReader.close();
        } catch (IOException e) {
            // Manejar excepciones relacionadas con la entrada y salida del archivo
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
    }

    public static void escribirArchivoLibroNuevo(ArrayList<Libro> libros){
        try {
            // Crear un objeto FileWriter con la opción 'append' establecida en 'false' para sobrescribir el archivo
            FileWriter fileWriter = new FileWriter(archivoLibros, false);

            // Crear un objeto BufferedWriter para escribir en el archivo
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            // Escribir el contenido en el archivo
            for (Libro l : libros) {
                String linea;
                linea = l.getCodLibro() + ";" + l.getNombre() + ";" + l.getAutor() + ";" + l.getNumEjemplares() + "\n";
                bufferedWriter.write(linea);
            }

            // Asegurar que el contenido se haya escrito y cerrar el BufferedWriter
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            // Manejar excepciones relacionadas con la entrada y salida del archivo
            System.err.println("Error al escribir en el archivo: " + e.getMessage());
        }
    }

    public static void agregarNuevoPrestamo(Prestamo p, String archivo){
        String lineaAAgregar = p.getCodLibro() + ";" + p.getCodCliente() + ";" + p.getFecha();

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(archivo, true));
            writer.write(lineaAAgregar);
            writer.newLine(); // Agregar una nueva línea antes de la línea a agregar
            writer.close();
        } catch (IOException e) {
            System.out.println("Error al agregar el prestamo: " + e.getMessage());
        }
    }

    public static void imprimirReciboPeticion(String tipoEscritura, int codCliente, String codLibro) {
        System.out.println(Colores.ANSI_BLUE + "Nueva peticion de escritura!" + Colores.ANSI_RESET);
        System.out.println(Colores.ANSI_BLUE+ "Tipo de escritura: " + Colores.ANSI_RESET + tipoEscritura);
        System.out.println(Colores.ANSI_BLUE + "Codigo del cliente: " + Colores.ANSI_RESET + codCliente);
        System.out.println(Colores.ANSI_BLUE + "Codigo del libro: " + Colores.ANSI_RESET + codLibro);
        System.out.println(Colores.ANSI_BLUE + "==============================================================" + Colores.ANSI_RESET);
    }

    public static void imprimirEntradaCoordi(int processId, char tipoOperacion, int codCliente, String codLibro){
        System.out.println(Colores.ANSI_RED + "NUEVO ACTOR EN BBDD!");
        System.out.println(Colores.ANSI_RED + "Codigo del actor: " + Colores.ANSI_RESET + processId);
        System.out.println(Colores.ANSI_RED + "Codigo del cliente: " + Colores.ANSI_RESET + codCliente);
        System.out.println(Colores.ANSI_RED + "Codigo del libro: " + Colores.ANSI_RESET + codLibro);
        System.out.println(Colores.ANSI_RED + "==========================================================================================" + Colores.ANSI_RESET);
    }

    public static void imprimirSalidaCoordi(int processId){
        System.out.println(Colores.ANSI_RED + "El actor " + processId + " ha salido de la BBDD!");
        System.out.println(Colores.ANSI_RED + "==========================================================================================" + Colores.ANSI_RESET);
    }
}
