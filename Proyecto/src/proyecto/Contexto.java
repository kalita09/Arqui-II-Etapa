/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto;

/**
 *
 * @author b04732 & a96097
 */
public class Contexto {
	int PC;
    int[] registros;
    Contexto(){
        this.registros = new int[33];
        for(int i = 0; i < 33; i++){
            this.registros[i] = 0;
        }
        int PC = 0;
    }
    void guardarContexto(int nuevoPC,int[] nuevoRegistros){
        this.PC = nuevoPC;
        this.registros = nuevoRegistros;
    }
    
}
