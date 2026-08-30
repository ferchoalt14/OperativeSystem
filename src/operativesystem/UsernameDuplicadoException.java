/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package operativesystem;

/**
 *
 * @author User
 */
public class UsernameDuplicadoException extends Exception {

    public UsernameDuplicadoException(String username) {
        super("El username '" + username + "' ya existe en el sistema.");
    }
}