/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

/**
 *
 * @author b04732 & a96097
 */
public class Controlador implements Runnable{
    int[][] colaEspera;
    Contexto [] vectorContextos;
    int numeroHilos;
    int apuntadorCola;
    int apuntadorCola2;
    int hiloActual1;
    int hiloActual2;
    Memoria memoriaInstrucciones;
    MemoriaDatos memoriaDatos;
    CyclicBarrier barrera;
    Nucleo[] vectorNucleos;
    int quantum;
    int ciclosReloj;
    static int numNUCLEOS = 2;
    Thread hilo1;
    Thread hilo2;
    static Semaphore busInstrucciones;
    static Semaphore busDatos;
    static Semaphore bloqueoCacheDatos;
    public boolean seguir;   
    Ventana ventana;
    
	
    public Controlador(int tamanoCola,int quantum,Ventana ventana) {
	    colaEspera = new int[4][tamanoCola];
	    this.vectorContextos = new Contexto [tamanoCola];
	    this.vectorNucleos = new Nucleo [tamanoCola];
	    numeroHilos = tamanoCola;
	    apuntadorCola = 0;
	    apuntadorCola2 = 1;
	    hiloActual1 = 0;
	    hiloActual2 = 1;
	    this.quantum = quantum;
	    this.ciclosReloj = 0;
	
	    this.busInstrucciones = new Semaphore(1);
            this.busDatos = new Semaphore(1);
            this.bloqueoCacheDatos = new Semaphore(1);
	    this.seguir = true;
            this.ventana = ventana;

	}
	        
    void iniciar(){
        this.memoriaInstrucciones = new Memoria();
        this.memoriaDatos = new MemoriaDatos();
        int bloque; //bloque donde inicia cada archivo (hilo)
            ventana.setVisible(true);
        //Guarda en cola donde inicia cada hilo
        for(int j = 1; j <= numeroHilos; j++ ){   
            bloque = memoriaInstrucciones.leerArchivo(j);
            colaEspera[0][j-1] = bloque;
            colaEspera[1][j-1] = memoriaInstrucciones.getPosicion();
            //guardo el length total de instrucciones por hilo = PC final                
            colaEspera[2][j-1] = memoriaInstrucciones.getLength();
            //n han termnado
            colaEspera[3][j-1] = 0;
            System.out.println("Hilo "+j+" comienza en "+bloque+" "+memoriaInstrucciones.getPosicion()+" PC"+colaEspera[2][j-1]);
            memoriaInstrucciones.setLength();
                
        }

        this.barrera = new CyclicBarrier(2, this);
        
        //iniciar vector de nucleos
        vectorNucleos[0] = new Nucleo("Nucleo 1",barrera,this.memoriaInstrucciones,this.memoriaDatos,colaEspera[0][this.apuntadorCola],
        		colaEspera[2][this.apuntadorCola],this.quantum,this.ciclosReloj,this.busInstrucciones,this.busDatos,this.bloqueoCacheDatos, colaEspera, hiloActual1);
        vectorNucleos[1] = new Nucleo("Nucleo 2",barrera,this.memoriaInstrucciones,this.memoriaDatos,colaEspera[0][this.apuntadorCola2],
        		colaEspera[2][this.apuntadorCola2],this.quantum,this.ciclosReloj,this.busInstrucciones,this.busDatos,this.bloqueoCacheDatos, colaEspera, hiloActual2);

        //inicializo vector de contextos
        for(int i=0; i<numeroHilos; i++) {
                vectorContextos[i] = new Contexto();
        }
                    
        //aqui cargar contexto

        vectorNucleos[0].setContexto(this.vectorContextos[this.apuntadorCola].PC,this.vectorContextos[this.apuntadorCola].registros);
        vectorNucleos[1].setContexto(this.vectorContextos[this.apuntadorCola2].PC,this.vectorContextos[this.apuntadorCola2].registros);
        vectorNucleos[0].numInstruccion = 0;
        vectorNucleos[1].numInstruccion = 0;

        System.out.println("Antes registro");
        for(int i=0; i<numNUCLEOS; i++) {
               // vectorNucleos[i].imprimirRegistros();
        ventana.jTextArea5.setText(vectorNucleos[i].imprimirRegistros());
        }
        
        //this.memoriaInstrucciones.imprimirMem();
        ventana.jTextArea4.setText(this.memoriaInstrucciones.imprimirMem());
        System.out.println("Antes cache");
        for(int i=0; i<numNUCLEOS; i++) {
                //vectorNucleos[i].imprimirCache();
                ventana.jTextArea3.setText(vectorNucleos[i].imprimirCache());
                
        }
        hilo1 = new Thread(vectorNucleos[0]);
        hilo2 = new Thread(vectorNucleos[1]);
        hilo1.start();
        hilo2.start();
            
    }
        
    public void buscarSiguienteHilo(int par) {//0 indica par, 1 impar
    	int i = par;
    	boolean encontrado = false;
    	if(par==0) {
        	while(i<this.numeroHilos/2+1 && !encontrado) {
	        	if((this.apuntadorCola+2)<= this.numeroHilos){ 
	
	                if((this.apuntadorCola)==(this.numeroHilos-2)){ //Ultimo hilo que puede verificar, entonces va al primero de la cola
	                   this.apuntadorCola=0; 
	                   if(this.colaEspera[3][apuntadorCola] == 0){
		                	encontrado = true;
		                }
	
	                } else{
	                   this.apuntadorCola+=2; //Siguiente hilo
	                }
	                if(this.colaEspera[3][apuntadorCola] == 0){
	                	encontrado = true;
	                }
	        	} else{
	        		this.apuntadorCola = 0; 
                }
                if(this.colaEspera[3][apuntadorCola] == 0){
                	encontrado = true;
	
	            }
                i++;
        	}
    	} else {
    		while(i<this.numeroHilos/2+1 && !encontrado) {
	        	if((this.apuntadorCola2+2)<= this.numeroHilos){
	
	                if((this.apuntadorCola2)==(this.numeroHilos-2)   ){
	                   this.apuntadorCola2=1;
	                   if(this.colaEspera[3][apuntadorCola2] == 0){
		                	encontrado = true;
		                }
	
	                } else{
	                   this.apuntadorCola2+=2; 
	                }
	                if(this.colaEspera[3][apuntadorCola2] == 0){
	                	if(this.colaEspera[3][apuntadorCola2] == 0){
		                	encontrado = true;
		                }
	                }
	        	} else{
		        		this.apuntadorCola2 = 1; 
	                }
	                if(this.colaEspera[3][apuntadorCola2] == 0){
	                	encontrado = true;
		
		            }
		        	i++;
	        	}
	
    	}
    }

    @Override
     public void run() {
        System.out.println("Todos han llegado a la barrera");
        
        //si no estan en fallo de cache los dos nucleos entraron en el 
        //primer caso del if donde ejecutan una instruccion, por lo tanto se resta quantum
        if(!vectorNucleos[0].esperandoBus || !vectorNucleos[1].esperandoBus){
            Nucleo.quantum--;
        }
        Nucleo.ciclosReloj++;
        
        System.out.println("Quantum"+Nucleo.quantum);
        System.out.println("Ciclo reloj"+Nucleo.ciclosReloj);
        System.out.println("Despues registro");
        
       // for(int i=0; i<2; i++) {
          //  System.out.println("Nucleo "+i);
            //vectorNucleos[i].imprimirRegistros();
            ventana.jTextArea5.setText(vectorNucleos[0].imprimirRegistros());
            ventana.jTextArea6.setText(vectorNucleos[1].imprimirRegistros());
            
       // }
        
            
        //this.memoriaInstrucciones.imprimirMem();
        ventana.jTextArea4.setText(this.memoriaInstrucciones.imprimirMem());
        System.out.println("Despues cache");
        for(int i=0; i<2; i++){ 
            System.out.println("Nucleo "+i);
            //vectorNucleos[i].imprimirCache();
            ventana.jTextArea3.setText(vectorNucleos[i].imprimirCache());
        }
            
        if((vectorNucleos[0].terminado) && (!vectorNucleos[0].desactivado)) { //nucleo 1 termino su hilo

                //guardo contexto de hilo terminado
                this.vectorContextos[this.apuntadorCola].guardarContexto(this.vectorNucleos[0].PC,this.vectorNucleos[0].registros);
                 colaEspera[3][this.apuntadorCola] = 1;

                 this.buscarSiguienteHilo(0);
                 if(colaEspera[3][this.apuntadorCola] == 1) {
                	 this.vectorNucleos[0].desactivado = true;
                 } else {
                    //asignacion de nuevo hilo para el nucleo
                    vectorNucleos[0].terminado=false;
                    vectorNucleos[0].seguir=true;
                    vectorNucleos[0].setContexto(this.vectorContextos[this.apuntadorCola].PC,this.vectorContextos[this.apuntadorCola].registros);
                    vectorNucleos[0].bloqueInicio = colaEspera[0][this.apuntadorCola];
                    vectorNucleos[0].setPCFin(colaEspera[2][this.apuntadorCola]);
                    vectorNucleos[0].numInstruccion = 0;
                    vectorNucleos[0].hiloActual = apuntadorCola;
                 }
            }
	
	            if((vectorNucleos[1].terminado==true)&& (!vectorNucleos[1].desactivado)) { //nucleo 2 termino su hilo
	
	                //guardo contexto de hilo terminado
	                this.vectorContextos[this.apuntadorCola2].guardarContexto(this.vectorNucleos[1].PC,this.vectorNucleos[1].registros);
	                colaEspera[3][this.apuntadorCola2] = 1;
	
	                this.buscarSiguienteHilo(1);
	                if(colaEspera[3][this.apuntadorCola2] == 1) {
	                	this.vectorNucleos[1].desactivado = true;
	                } else {
	                    //asignacion de nuevo hilo para el nucleo
	                    vectorNucleos[1].terminado=false;
	                    vectorNucleos[1].seguir=true;
	                    vectorNucleos[1].setContexto(this.vectorContextos[this.apuntadorCola2].PC,this.vectorContextos[this.apuntadorCola2].registros);
	                    vectorNucleos[1].bloqueInicio = colaEspera[0][this.apuntadorCola2];
	                    vectorNucleos[1].setPCFin(colaEspera[2][this.apuntadorCola2]);
	                    vectorNucleos[1].numInstruccion = 0;
	                    vectorNucleos[1].hiloActual = apuntadorCola2;
	                }
	            }	            
	            
	            if(Nucleo.quantum == 0){
	                
	                //Nucleo.quantum = this.quantum;
	                
	                 boolean programaTerminado = true;
	                 for(int i=0;i<this.numeroHilos;i++){
	                    if(colaEspera[3][i] != 1) {
	                        programaTerminado = false;
	                    }
	
	                 }
	
	                if(programaTerminado == true) {
	                     for(int i=0; i<2; i++) {
	                         vectorNucleos[i].seguir=false;
	                     }
	
	                }else{
	
	                    Nucleo.quantum = this.quantum;	                
	                        	                        
	                 }
	                
	            }
                    
                    //analizar posicion del codigo
                    //si nucleo 1 esta pidiendo si alguien tiene el bloque que ocupa
                    //LOAD WORD
                    
                    if(this.vectorNucleos[0].revisarOtraCacheLW) {
                        if((this.vectorNucleos[1].cacheDatos.contenerBloque(this.vectorNucleos[0].direccion))){
                            if(  this.vectorNucleos[1].cacheDatos.getBloque(this.vectorNucleos[0].direccion).estado == 'M'){
                                //copiar a mem y cache
                                BloqueDatos bloque = this.vectorNucleos[1].cacheDatos.getBloque(this.vectorNucleos[0].direccion);
                                this.memoriaDatos.setBloque(this.vectorNucleos[0].direccion, bloque);
                                this.vectorNucleos[0].cacheDatos.setBloque(bloque);
                                this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[0].direccion).estado = 'C';
                                this.vectorNucleos[1].cacheDatos.getBloque(this.vectorNucleos[0].direccion).estado = 'C';
                            
                            }else if (this.vectorNucleos[1].cacheDatos.getBloque(this.vectorNucleos[0].direccion).estado == 'C'){
                                
                                BloqueDatos bloque = this.memoriaDatos.getBloque(this.vectorNucleos[0].direccion);
                                this.vectorNucleos[0].cacheDatos.setBloque(bloque);
                                this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[0].direccion).estado = 'C';

                            }
                        }else{
                            BloqueDatos bloque = this.memoriaDatos.getBloque(this.vectorNucleos[0].direccion);
                            this.vectorNucleos[0].cacheDatos.setBloque(bloque);
                            this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[0].direccion).estado = 'C';
                        }
                    
                    //si nucleo 2 esta pidiendo si alguien tiene el bloque que ocupa
                    }else if(this.vectorNucleos[1].revisarOtraCacheLW){
                        if((this.vectorNucleos[0].cacheDatos.contenerBloque(this.vectorNucleos[1].direccion))){
                            if(  this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[1].direccion).estado == 'M'){
                                //copiar a mem y cache
                                BloqueDatos bloque = this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[1].direccion);
                                this.memoriaDatos.setBloque(this.vectorNucleos[1].direccion, bloque);
                                this.vectorNucleos[1].cacheDatos.setBloque(bloque);
                                this.vectorNucleos[1].cacheDatos.getBloque(this.vectorNucleos[1].direccion).estado = 'C';
                                this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[1].direccion).estado = 'C';
                            
                            }else if (this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[1].direccion).estado == 'C'){
                                
                                BloqueDatos bloque = this.memoriaDatos.getBloque(this.vectorNucleos[1].direccion);
                                this.vectorNucleos[1].cacheDatos.setBloque(bloque);
                                this.vectorNucleos[1].cacheDatos.getBloque(this.vectorNucleos[1].direccion).estado = 'C';

                            }
                        }else{
                            BloqueDatos bloque = this.memoriaDatos.getBloque(this.vectorNucleos[1].direccion);
                            this.vectorNucleos[1].cacheDatos.setBloque(bloque);
                            this.vectorNucleos[1].cacheDatos.getBloque(this.vectorNucleos[1].direccion).estado = 'C';
                        }
                        
                    
                    
                    }
                    //STORE WORD
                    if(this.vectorNucleos[0].revisarOtraCacheSW) {
                        if((this.vectorNucleos[1].cacheDatos.contenerBloque(this.vectorNucleos[0].direccion))){
                            if(  this.vectorNucleos[1].cacheDatos.getBloque(this.vectorNucleos[0].direccion).estado == 'M'){
                                //copiar a mem y cache
                                BloqueDatos bloque = this.vectorNucleos[1].cacheDatos.getBloque(this.vectorNucleos[0].direccion);
                                this.memoriaDatos.setBloque(this.vectorNucleos[0].direccion, bloque);
                                this.vectorNucleos[0].cacheDatos.setBloque(bloque);
                                this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[0].direccion).estado = 'C';
                                this.vectorNucleos[1].cacheDatos.getBloque(this.vectorNucleos[0].direccion).estado = 'C';
                            
                            }else if (this.vectorNucleos[1].cacheDatos.getBloque(this.vectorNucleos[0].direccion).estado == 'C'){
                                
                                BloqueDatos bloque = this.memoriaDatos.getBloque(this.vectorNucleos[0].direccion);
                                this.vectorNucleos[0].cacheDatos.setBloque(bloque);
                                this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[0].direccion).estado = 'C';

                            }
                        }else{
                            BloqueDatos bloque = this.memoriaDatos.getBloque(this.vectorNucleos[0].direccion);
                            this.vectorNucleos[0].cacheDatos.setBloque(bloque);
                            this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[0].direccion).estado = 'C';
                        }
                    
                    //si nucleo 2 esta pidiendo si alguien tiene el bloque que ocupa
                    }else if(this.vectorNucleos[1].revisarOtraCacheSW){
                        if((this.vectorNucleos[0].cacheDatos.contenerBloque(this.vectorNucleos[1].direccion))){
                            if(  this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[1].direccion).estado == 'M'){
                                //copiar a mem y cache
                                BloqueDatos bloque = this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[1].direccion);
                                this.memoriaDatos.setBloque(this.vectorNucleos[1].direccion, bloque);
                                this.vectorNucleos[1].cacheDatos.setBloque(bloque);
                                this.vectorNucleos[1].cacheDatos.getBloque(this.vectorNucleos[1].direccion).estado = 'C';
                                this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[1].direccion).estado = 'C';
                            
                            }else if (this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[1].direccion).estado == 'C'){
                                
                                BloqueDatos bloque = this.memoriaDatos.getBloque(this.vectorNucleos[1].direccion);
                                this.vectorNucleos[1].cacheDatos.setBloque(bloque);
                                this.vectorNucleos[1].cacheDatos.getBloque(this.vectorNucleos[1].direccion).estado = 'C';

                            }
                        }else{
                            BloqueDatos bloque = this.memoriaDatos.getBloque(this.vectorNucleos[1].direccion);
                            this.vectorNucleos[1].cacheDatos.setBloque(bloque);
                            this.vectorNucleos[1].cacheDatos.getBloque(this.vectorNucleos[1].direccion).estado = 'C';
                        }
                        
                    
                    
                    }                    
                    
                    
	            if(this.vectorNucleos[0].desactivado && this.vectorNucleos[1].desactivado) {
	            	this.vectorNucleos[0].apagado = true;
	            	this.vectorNucleos[0].apagado = true;
                	seguir = false;
                	//System.exit(0);

	            }
	                
    }
}
    
