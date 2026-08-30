/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package operativesystem;

/**
 *
 * @author User
 */
public class CuentaDesactivadaException extends Exception {

    public CuentaDesactivadaException(String username) {
        super("La cuenta del usuario '" + username + "' está desactivada.");
    }
}
