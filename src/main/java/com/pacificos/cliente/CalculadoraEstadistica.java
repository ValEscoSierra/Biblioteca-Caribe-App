package com.pacificos.cliente;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CalculadoraEstadistica {

    public static void main(String[] args) {
        String nombreArchivo = "Requerimientos/TiemposRespuesta.txt"; // Reemplaza con el nombre de tu archivo

        List<Double> datos = leerDatos(nombreArchivo);
        if (!datos.isEmpty()) {
            double promedio = calcularPromedio(datos);
            double desviacionEstandar = calcularDesviacionEstandar(datos);

            System.out.println("Promedio: " + promedio);
            System.out.println("Desviación estándar: " + desviacionEstandar);
        } else {
            System.out.println("El archivo está vacío o no se pudo leer.");
        }
    }

    private static List<Double> leerDatos(String nombreArchivo) {
        List<Double> datos = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(nombreArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                double valor = Double.parseDouble(linea);
                datos.add(valor);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return datos;
    }

    private static double calcularPromedio(List<Double> datos) {
        double suma = 0;
        for (double dato : datos) {
            suma += dato;
        }
        return suma / datos.size();
    }

    private static double calcularDesviacionEstandar(List<Double> datos) {
        double promedio = calcularPromedio(datos);
        double sumaCuadrados = 0;

        for (double dato : datos) {
            sumaCuadrados += Math.pow(dato - promedio, 2);
        }

        double varianza = sumaCuadrados / datos.size();
        return Math.sqrt(varianza);
    }
}
