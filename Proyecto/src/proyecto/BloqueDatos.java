/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto;

/**
 *
 * @author kalam
 */
public class BloqueDatos {
    
    int[] datos;
    int FILAS = 4;
    //numeración del propio bloque, del 40 en adelante
    int serial;
    //ID del bloque a contener
    int ID;
    //Modificado,Compartido Invàlido
    char estado;

    BloqueDatos(int id){
        
    	this.ID = id;
        this.datos = new int[FILAS];
        estado = 'I';
        
    }
    
    
    void inicializarMemoriaDatos(){
        
      for(int i=0; i<FILAS; i++) {
            datos [i] = 1;

        }
    
    }
    
    public int getID() {
    	return this.ID;
    }
    
    void inicializarCacheDatos(){

        for(int i=0; i<FILAS; i++) {
            datos [i] = 0;
        }

    }
    
    void setDato(int palabra,int miDato){
        
        this.datos[palabra] = miDato;
    }
    int getDato(int palabra){
        
       return this.datos[palabra];
       
    }

}
