/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package operativesystem;

/**
 *
 * @author User
 */
public class ArchivoCorruptoException extends Exception {

    public ArchivoCorruptoException(String nombreArchivo, Throwable causa) {
        super("El archivo '" + nombreArchivo + "' está corrupto o no se pudo leer.", causa);
    }
}
