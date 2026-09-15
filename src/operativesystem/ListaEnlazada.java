package operativesystem;
 
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
 
/**
 *
 * @author Leandro
 */
public class ListaEnlazada<T> implements Iterable<T> {
 
    private Nodo<T> cabeza;
    private Nodo<T> cola;
    private int tamano;
 
    public ListaEnlazada() {
        this.cabeza = null;
        this.cola = null;
        this.tamano = 0;
    }
 
    public void agregar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (estaVacia()) {
            cabeza = nuevo;
            cola = nuevo;
        } else {
            cola.setSiguiente(nuevo);
            cola = nuevo;
        }
        tamano++;
    }
 
    public void agregarAlInicio(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (estaVacia()) {
            cabeza = nuevo;
            cola = nuevo;
        } else {
            nuevo.setSiguiente(cabeza);
            cabeza = nuevo;
        }
        tamano++;
    }
 
    public boolean eliminar(T dato) {
        if (estaVacia()) {
            return false;
        }
        if (cabeza.getDato().equals(dato)) {
            cabeza = cabeza.getSiguiente();
            if (cabeza == null) {
                cola = null;
            }
            tamano--;
            return true;
        }
        Nodo<T> actual = cabeza;
        while (actual.getSiguiente() != null) {
            if (actual.getSiguiente().getDato().equals(dato)) {
                Nodo<T> aEliminar = actual.getSiguiente();
                actual.setSiguiente(aEliminar.getSiguiente());
                if (aEliminar == cola) {
                    cola = actual;
                }
                tamano--;
                return true;
            }
            actual = actual.getSiguiente();
        }
        return false;
    }
 
    public boolean contiene(T dato) {
        Nodo<T> actual = cabeza;
        while (actual != null) {
            if (actual.getDato().equals(dato)) {
                return true;
            }
            actual = actual.getSiguiente();
        }
        return false;
    }
 
    public T obtener(int indice) {
        if (indice < 0 || indice >= tamano) {
            throw new IndexOutOfBoundsException("Indice fuera de rango: " + indice);
        }
        Nodo<T> actual = cabeza;
        for (int i = 0; i < indice; i++) {
            actual = actual.getSiguiente();
        }
        return actual.getDato();
    }
 
    public boolean estaVacia() {
        return tamano == 0;
    }
 
    public int tamano() {
        return tamano;
    }
 
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Nodo<T> actual = cabeza;
 
            @Override
            public boolean hasNext() {
                return actual != null;
            }
 
            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("No hay mas elementos en la lista.");
 
                }
                T dato = actual.getDato();
                actual = actual.getSiguiente();
                return dato;
            }
        };
    }
    
    /** Ordena la lista enlazada in-place según el comparador dado. */
    public void ordenarPor(Comparator<T> comparador) {
        if (tamano < 2) {
            return;
        }
        List<T> temporal = new ArrayList<>();
        for (T dato : this) {
            temporal.add(dato);
        }
        Collections.sort(temporal, comparador);
        Nodo<T> actual = cabeza;
        for (T dato : temporal) {
            actual.setDato(dato);
            actual = actual.getSiguiente();
        }
    }

    @Override
    public String toString(){
    StringBuilder sb = new StringBuilder("[");
    Nodo<T> actual = cabeza;
    while (actual != null){
    sb.append(actual.getDato());
        if (actual.getSiguiente() != null) {
            sb.append(", ");
        }
        actual = actual.getSiguiente();
    }
    sb.append("]");
    return sb.toString();
    }
 
}