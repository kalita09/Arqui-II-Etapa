/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

import javax.swing.JOptionPane;

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
    int m;
    int b;
    boolean lento;
    static int numNUCLEOS = 2;
    Thread hilo1;
    Thread hilo2;
    static Semaphore busInstrucciones;
    static Semaphore busDatos;
    public boolean seguir;   
    Ventana ventana;
    public boolean nucleo1BloqueoCache2;
    public boolean nucleo2BloqueoCache1;
	
    public Controlador(int tamanoCola,int quantum,int m,int b,boolean lento,Ventana ventana) {
	    
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
            this.m = m;
            this.b = b;
            this.lento = lento;
	    this.busInstrucciones = new Semaphore(1);
            this.busDatos = new Semaphore(1);
	    this.seguir = true;
            this.ventana = ventana;
            this.nucleo1BloqueoCache2 = false ;
            this.nucleo2BloqueoCache1 = false;
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
        		colaEspera[2][this.apuntadorCola],this.quantum,this.ciclosReloj,this.busInstrucciones,this.busDatos, colaEspera, hiloActual1);
        vectorNucleos[1] = new Nucleo("Nucleo 2",barrera,this.memoriaInstrucciones,this.memoriaDatos,colaEspera[0][this.apuntadorCola2],
        		colaEspera[2][this.apuntadorCola2],this.quantum,this.ciclosReloj,this.busInstrucciones,this.busDatos, colaEspera, hiloActual2);

        //inicializo vector de contextos
        for(int i=0; i<numeroHilos; i++) {
                vectorContextos[i] = new Contexto();
        }
                    
        //aqui cargar contexto

        vectorNucleos[0].setContexto(this.vectorContextos[this.apuntadorCola].PC,this.vectorContextos[this.apuntadorCola].registros);
        vectorNucleos[1].setContexto(this.vectorContextos[this.apuntadorCola2].PC,this.vectorContextos[this.apuntadorCola2].registros);

        System.out.println("Antes registro");
        for(int i=0; i<numNUCLEOS; i++) {
               // vectorNucleos[i].imprimirRegistros();
        ventana.jTextArea5.setText(vectorNucleos[i].imprimirRegistros());
        }
        
        //this.memoriaInstrucciones.imprimirMem();
        ventana.jTextArea4.setText(this.memoriaInstrucciones.imprimirMem());
        
        ventana.jTextArea2.setText(this.memoriaDatos.imprimirMem());
        
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
        Nucleo.ciclosReloj++;
        if(Nucleo.ciclosReloj==45) {
        	@SuppressWarnings("unused")
			int a=40;
        }
        this.ventana.jLabel9.setText(Integer.toString(Nucleo.ciclosReloj));
        this.ventana.jLabel10.setText(Integer.toString(Nucleo.quantum));
        this.ventana.jLabel11.setText(Integer.toString(vectorNucleos[0].hiloActual));
        this.ventana.jLabel12.setText(Integer.toString(vectorNucleos[1].hiloActual));
        //si no estan en fallo de cache los dos nucleos entraron en el 
        //primer caso del if donde ejecutan una instruccion, por lo tanto se resta quantum
        if((!vectorNucleos[0].esperandoBus && !vectorNucleos[0].esperandoCache) ||
        		(!vectorNucleos[1].esperandoBus && !vectorNucleos[1].esperandoCache)){
        	
            Nucleo.quantum--;
        }

        System.out.println("Despues registro");
        
       // for(int i=0; i<2; i++) {
          //  System.out.println("Nucleo "+i);
            //vectorNucleos[i].imprimirRegistros();
            ventana.jTextArea5.setText(vectorNucleos[0].imprimirRegistros());
            ventana.jTextArea6.setText(vectorNucleos[1].imprimirRegistros());
            
       // }
        
            
        //this.memoriaInstrucciones.imprimirMem();
        ventana.jTextArea4.setText(this.memoriaInstrucciones.imprimirMem());
        ventana.jTextArea2.setText(this.memoriaDatos.imprimirMem());
        
    
        ventana.jTextArea3.setText(vectorNucleos[0].imprimirCache());
        ventana.jTextArea7.setText(vectorNucleos[1].imprimirCache());
        
        ventana.jTextArea1.setText(vectorNucleos[0].cacheDatos.imprimir());
        ventana.jTextArea8.setText(vectorNucleos[1].cacheDatos.imprimir());
            
        if((Nucleo.quantum == 0) || ((vectorNucleos[0].terminado) && (!vectorNucleos[0].desactivado))) { //nucleo 1 termino su hilo

                //guardo contexto de hilo terminado
                this.vectorContextos[this.apuntadorCola].guardarContexto(this.vectorNucleos[0].PC,this.vectorNucleos[0].registros);
                if(vectorNucleos[0].terminado) {
                	colaEspera[3][this.apuntadorCola] = 1;
                }

                 this.buscarSiguienteHilo(0);
                 if(colaEspera[3][this.apuntadorCola] == 1) {
                	 this.vectorNucleos[0].desactivado = true;
                 } else {
                    //asignacion de nuevo hilo para el nucleo
                    vectorNucleos[0].terminado=false;
                    vectorNucleos[0].seguir=true;
                    vectorNucleos[0].setContexto(this.vectorContextos[this.apuntadorCola].PC,this.vectorContextos[this.apuntadorCola].registros);
                    vectorNucleos[0].bloqueInicio = colaEspera[0][this.apuntadorCola];
                    vectorNucleos[0].hiloActual = apuntadorCola;
                 }
                 
                 if(Nucleo.quantum == 0){
	                	//revisar si hay LL activo
	                    if(this.vectorNucleos[0].banderaLL){
	                        if(this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[0].direccion).ID==this.vectorNucleos[0].bloqueLL){
	                            this.vectorNucleos[0].registros[32] = -1;
	                        }	                        
	                    }
	                    	                    
                 }
            }
	
            if((Nucleo.quantum == 0) || ((vectorNucleos[1].terminado) && (!vectorNucleos[1].desactivado))) { //nucleo 2 termino su hilo

                //guardo contexto de hilo terminado
                this.vectorContextos[this.apuntadorCola2].guardarContexto(this.vectorNucleos[1].PC,this.vectorNucleos[1].registros);
                if(vectorNucleos[1].terminado) {
                	colaEspera[3][this.apuntadorCola2] = 1;
                }
                
                this.buscarSiguienteHilo(1);
                if(colaEspera[3][this.apuntadorCola2] == 1) {
                	this.vectorNucleos[1].desactivado = true;
                } else {
                    //asignacion de nuevo hilo para el nucleo
                    vectorNucleos[1].terminado=false;
                    vectorNucleos[1].seguir=true;
                    vectorNucleos[1].setContexto(this.vectorContextos[this.apuntadorCola2].PC,this.vectorContextos[this.apuntadorCola2].registros);
                    vectorNucleos[1].bloqueInicio = colaEspera[0][this.apuntadorCola2];
                    vectorNucleos[1].hiloActual = apuntadorCola2;
                }
                
                if(Nucleo.quantum == 0){
                	//revisar si hay LL activo
                    if(this.vectorNucleos[1].banderaLL){
                        if(this.vectorNucleos[1].cacheDatos.getBloque(this.vectorNucleos[1].direccion).ID==this.vectorNucleos[1].bloqueLL){
                            this.vectorNucleos[1].registros[32] = -1;
                        }
                    }
                    Nucleo.quantum = this.quantum;
	                        	                        		                
	            }
                
            }
	            
	                
                    //analizar posicion del codigo
                    //si nucleo 1 esta pidiendo si alguien tiene el bloque que ocupa
                    //LOAD WORD
                    
                    if(this.vectorNucleos[0].revisarOtraCacheLW) {
                    	if(vectorNucleos[1].bloqueoCacheDatos.tryAcquire()){
                    		
                    		//Verifica si hay un bloque modificado en la posición que voy a escribir
                        	BloqueDatos bloque = this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[0].direccion);
                        	if(bloque.estado == 'M') {
                        		
                        		//Lo guardo en memoria
                        		this.memoriaDatos.setBloque(bloque.ID*16+640, bloque.datos);
                        	}

                    			//Hit en la otra cache
		                        if((this.vectorNucleos[1].cacheDatos.contenerBloque(this.vectorNucleos[0].direccion))){
		                            if(this.vectorNucleos[1].cacheDatos.getBloque(this.vectorNucleos[0].direccion).estado == 'M'){		                            			                            	
		                            	
		                                //copiar a mem y cache
		                                bloque = this.vectorNucleos[1].cacheDatos.getBloque(this.vectorNucleos[0].direccion);
		                                this.memoriaDatos.setBloque(this.vectorNucleos[0].direccion, bloque.datos);
		                                this.vectorNucleos[0].cacheDatos.setBloque(bloque);
		                                this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[0].direccion).estado = 'C';
		                                this.vectorNucleos[1].cacheDatos.getBloque(this.vectorNucleos[0].direccion).estado = 'C';
		                                this.vectorNucleos[0].datoCargado = true;
		                            
		                            //estado==C
		                            }else {
		                                
		                                bloque = this.memoriaDatos.getBloque(this.vectorNucleos[0].direccion);
		                                this.vectorNucleos[0].cacheDatos.setBloque(bloque);
		                                this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[0].direccion).estado = 'C';
		                                this.vectorNucleos[0].datoCargado = true;
		
		                            }
		                            
		                        //Fallo en la otra cache
		                        }else{
		                            bloque = this.memoriaDatos.getBloque(this.vectorNucleos[0].direccion);
		                            this.vectorNucleos[0].cacheDatos.setBloque(bloque);
		                            this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[0].direccion).estado = 'C';
		                            this.vectorNucleos[0].datoCargado = true;
		                        }
                            nucleo1BloqueoCache2 = false;
                            vectorNucleos[1].bloqueoCacheDatos.release();
                            this.vectorNucleos[0].datoCargado = true;
                            //Nucleo.busDatos.release();
                                        
                    	}
                    
                    //si nucleo 2 esta pidiendo si alguien tiene el bloque que ocupa
                    }else if(this.vectorNucleos[1].revisarOtraCacheLW){
                    	if(vectorNucleos[0].bloqueoCacheDatos.tryAcquire()){
                    		
                    		//Verifica si hay un bloque modificado en la posición que voy a escribir
                        	BloqueDatos bloque = this.vectorNucleos[1].cacheDatos.getBloque(this.vectorNucleos[1].direccion);
                        	if(bloque.estado == 'M') {
                        		
                        		//Lo guardo en memoria
                        		this.memoriaDatos.setBloque(bloque.ID*16+640, bloque.datos);
                        	}
                    		/*nucleo2BloqueoCache1 = true;
                    		
                    		}else if(nucleo2BloqueoCache1) {*/
		                        if((this.vectorNucleos[0].cacheDatos.contenerBloque(this.vectorNucleos[1].direccion))){
		                            if(this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[1].direccion).estado == 'M'){
		                                //copiar a mem y cache
		                                bloque = this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[1].direccion);
		                                this.memoriaDatos.setBloque(this.vectorNucleos[1].direccion, bloque.datos);
		                                this.vectorNucleos[1].cacheDatos.setBloque(bloque);
		                                this.vectorNucleos[1].cacheDatos.getBloque(this.vectorNucleos[1].direccion).estado = 'C';
		                                this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[1].direccion).estado = 'C';
		                                this.vectorNucleos[1].datoCargado = true;
		                            
		                            //estado==C
		                            }else {
		                                
		                                bloque = this.memoriaDatos.getBloque(this.vectorNucleos[1].direccion);
		                                this.vectorNucleos[1].cacheDatos.setBloque(bloque);
		                                this.vectorNucleos[1].cacheDatos.getBloque(this.vectorNucleos[1].direccion).estado = 'C';
		                                this.vectorNucleos[1].datoCargado = true;
		
		                            }
		                            
		                        //Fallo en la otra cache
		                        }else{
		                            bloque = this.memoriaDatos.getBloque(this.vectorNucleos[1].direccion);
		                            this.vectorNucleos[1].cacheDatos.setBloque(bloque);
		                            this.vectorNucleos[1].cacheDatos.getBloque(this.vectorNucleos[1].direccion).estado = 'C';
		                            this.vectorNucleos[1].datoCargado = true;
		                        }
                            nucleo2BloqueoCache1 = false;
                            vectorNucleos[0].bloqueoCacheDatos.release();
                            this.vectorNucleos[1].datoCargado = true;
                            //Nucleo.busDatos.release();
                            
                    	}
                    
                    }
                    //STORE WORD
                    
                    //Verifica si el nucleo 1 solicita revisar otra cache
                    if(this.vectorNucleos[0].revisarOtraCacheSW) {
                    	if(vectorNucleos[1].bloqueoCacheDatos.tryAcquire()){/*
                    		nucleo1BloqueoCache2 = true; //Indica al controlador que el nucleo 1 tiene bloqueada la cache del nucleo 2 y asi utilizarla en el otro ciclo
                    		
                    	}else if(nucleo1BloqueoCache2) {*/
                    		if((this.vectorNucleos[1].cacheDatos.contenerBloque(this.vectorNucleos[0].direccion))){
                    			
                    			/*//Verifica si hay un bloque modificado en la posición que voy a escribir
                            	BloqueDatos bloque = this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[0].direccion);
                            	if(bloque.estado == 'M') {
                            		
                            		//Lo guardo en memoria
                            		this.memoriaDatos.setBloque(bloque.ID, bloque);
                            	}*/
                    			
                    			//Fallo en nucleo 1 y modificado en nucleo 2
                    			if(this.vectorNucleos[1].cacheDatos.getBloque(this.vectorNucleos[0].direccion).estado == 'M') {
                    				
                    				//Verifica si hay un bloque modificado en la posición que voy a escribir
                                	BloqueDatos bloque = this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[0].direccion);
                                	if(bloque.estado == 'M') {
                                		
                                		//Lo guardo en memoria
                                		this.memoriaDatos.setBloque(bloque.ID*16+640, bloque.datos);
                                	}
                    				
                    				//copiar a memoria y cache de nucleo 1
                    				bloque = this.vectorNucleos[1].cacheDatos.getBloque(this.vectorNucleos[0].direccion);
	                                this.memoriaDatos.setBloque(this.vectorNucleos[0].direccion, bloque.datos);
                              
                                        //revisar si hay LL activo
                                        if(this.vectorNucleos[0].banderaLL){
                                            if(this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[0].direccion).ID==this.vectorNucleos[0].bloqueLL){
                                                this.vectorNucleos[0].registros[32] = -1;
                                            }
                                        }
                                        
	                                this.vectorNucleos[1].cacheDatos.getBloque(this.vectorNucleos[0].direccion).estado = 'I';
	                                this.vectorNucleos[1].bloqueoCacheDatos.release();
	                                nucleo1BloqueoCache2 = false;
	                                this.vectorNucleos[0].cacheDatos.setBloque(bloque);
	                                this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[0].direccion).estado = 'M';
	                                this.vectorNucleos[0].leerBloqueOtraCache = true;
                                
	                            //Fallo en nucleo 1 y compartido en nucleo 2
                    			}else if(!this.vectorNucleos[0].cacheDatos.contenerBloque(this.vectorNucleos[0].direccion)) {
                    				
                    				//Verifica si hay un bloque modificado en la posición que voy a escribir
                                	BloqueDatos bloque = this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[0].direccion);
                                	if(bloque.estado == 'M') {
                                		
                                		//Lo guardo en memoria
                                		this.memoriaDatos.setBloque(bloque.ID*16+640, bloque.datos);
                                	}
                    				
                    				//revisar si hay LL activo
                    				if(this.vectorNucleos[1].banderaLL){
                                        if(this.vectorNucleos[1].cacheDatos.getBloque(this.vectorNucleos[1].direccion).ID==this.vectorNucleos[1].bloqueLL){
                                            this.vectorNucleos[1].registros[32] = -1;
                                        }
                                    }
                    				
                    				//Subir de memoria
                    				this.vectorNucleos[1].cacheDatos.getBloque(this.vectorNucleos[0].direccion).estado = 'I';
	                                bloque = this.memoriaDatos.getBloque(this.vectorNucleos[0].direccion);
	                                this.vectorNucleos[0].cacheDatos.setBloque(bloque);
	                                vectorNucleos[1].bloqueoCacheDatos.release();
	                                nucleo1BloqueoCache2 = false;
	                                //this.busDatos.release();
	                                this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[0].direccion).estado = 'M';
	                                this.vectorNucleos[0].leerBloqueOtraCache = true;
                    				                            
	                            //Bloque compartido en ambos nucleos
                    			}else {
                    				if(this.vectorNucleos[1].banderaLL){
                                        if(this.vectorNucleos[1].cacheDatos.getBloque(this.vectorNucleos[1].direccion).ID==this.vectorNucleos[1].bloqueLL){
                                            this.vectorNucleos[1].registros[32] = -1;
                                        }
                    				}
                    				this.vectorNucleos[1].cacheDatos.getBloque(this.vectorNucleos[0].direccion).estado = 'I';
                    				vectorNucleos[1].bloqueoCacheDatos.release();
                    				nucleo1BloqueoCache2 = false;
                    				this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[0].direccion).estado = 'M';
	                                this.vectorNucleos[0].leerBloqueOtraCache = true;
                    			}
                            
                        //Fallo en ambas caches
                        }else if(!this.vectorNucleos[0].cacheDatos.contenerBloque(this.vectorNucleos[0].direccion)){                                   	
                        	this.vectorNucleos[1].bloqueoCacheDatos.release();
                        	
                        	//Verifica si hay un bloque modificado en la posición que voy a escribir
                        	BloqueDatos bloque = this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[0].direccion);
                        	if(bloque.estado == 'M') {
                        		
                        		//Lo guardo en memoria
                        		this.memoriaDatos.setBloque(bloque.ID*16+640, bloque.datos);
                        	}
                        	
                        	//Subir de memoria
                            bloque = this.memoriaDatos.getBloque(this.vectorNucleos[0].direccion);
                            this.vectorNucleos[0].cacheDatos.setBloque(bloque);
                            //this.busDatos.release();                            
                            nucleo1BloqueoCache2 = false;
                            bloque.estado = 'M';
                            this.vectorNucleos[0].leerBloqueOtraCache = true;
                        
                        //Fallo en nucleo 2 y compartido nucleo 1
                        }else {
                        	this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[0].direccion).estado = 'M';
                        	this.vectorNucleos[0].leerBloqueOtraCache = true;
                        }
                        
                	}
                    
                //Verifica si nucleo 2 solicita revisar otra cache
                }else if(this.vectorNucleos[1].revisarOtraCacheSW){
                	
                    	//Se bloquea la cache del nucleo 2
                    	if(vectorNucleos[0].bloqueoCacheDatos.tryAcquire()){
                    		/*nucleo2BloqueoCache1 = true; //Indica al controlador que el nucleo 2 tiene bloqueada la cache del nucleo 1 y asi utilizarla en el otro ciclo
                    	
                    	}else if(nucleo2BloqueoCache1) {*/
                    		if((this.vectorNucleos[0].cacheDatos.contenerBloque(this.vectorNucleos[1].direccion))){
                    			
                    			//Cache del nucleo 1 tiene el bloque modificado
                    			if(this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[1].direccion).estado == 'M'){
                    				
	                                //copiar a memoria y cache del nucleo 2
	                                BloqueDatos bloque = this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[1].direccion);
	                                this.memoriaDatos.setBloque(this.vectorNucleos[1].direccion, bloque.datos);
	
									//revisar si hay LL activo
									if(this.vectorNucleos[0].banderaLL){
									    if(this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[0].direccion).ID==this.vectorNucleos[0].bloqueLL){
									        this.vectorNucleos[0].registros[32] = -1;
									    }
	
	
	                                }
	                                this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[1].direccion).estado = 'I';
	                                this.vectorNucleos[0].bloqueoCacheDatos.release();
	                                nucleo2BloqueoCache1 = false;
	                                this.vectorNucleos[1].cacheDatos.setBloque(bloque);
	                                this.vectorNucleos[1].cacheDatos.getBloque(this.vectorNucleos[1].direccion).estado = 'M';
                            
                                //Cache del nucleo 1 tiene el bloque compartido

                    			}else{  
                                            
                                        //revisar si hay LL activo
                                        if(this.vectorNucleos[0].banderaLL){
                                            if(this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[0].direccion).ID==this.vectorNucleos[0].bloqueLL){
                                                this.vectorNucleos[0].registros[32] = -1;
                                            }
                                        }
                                        
                                    //Subir de memoria
	                            	this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[1].direccion).estado = 'I';
	                            	BloqueDatos bloque = this.memoriaDatos.getBloque(this.vectorNucleos[1].direccion);
	                                this.vectorNucleos[1].cacheDatos.setBloque(bloque);
	                                vectorNucleos[0].bloqueoCacheDatos.release();
	                                nucleo2BloqueoCache1 = false;
	                                //this.busDatos.release();
	                                this.vectorNucleos[1].cacheDatos.getBloque(this.vectorNucleos[1].direccion).estado = 'M';
	                                this.vectorNucleos[1].leerBloqueOtraCache = true;
                    			}
                    			
                			//Fallo en ambas caches
                    		}else if(!this.vectorNucleos[1].cacheDatos.contenerBloque(this.vectorNucleos[1].direccion)) {
                    			
                    			//Subir de memoria
                    			BloqueDatos bloque = this.memoriaDatos.getBloque(this.vectorNucleos[1].direccion);
                    			this.vectorNucleos[1].cacheDatos.setBloque(bloque);
                    			vectorNucleos[0].bloqueoCacheDatos.release();
                    			nucleo2BloqueoCache1 = false;	                            
	                            //this.busDatos.release();
                    			bloque.estado = 'M';
	                            this.vectorNucleos[1].leerBloqueOtraCache = true;
                    		
	                        //Fallo en cache del nucleo 2 y bloque compartido en cache del nucleo 1
                            }else {
                            	this.vectorNucleos[0].cacheDatos.getBloque(this.vectorNucleos[0].direccion).estado = 'M';
                            	this.vectorNucleos[0].leerBloqueOtraCache = true;
                            }
                                                                
                    	}
                    	
                    }
                    

                    
	            if(this.vectorNucleos[0].desactivado && this.vectorNucleos[1].desactivado) {
	            	this.vectorNucleos[0].apagado = true;
	            	this.vectorNucleos[0].apagado = true;
                	seguir = false;
                	int res = JOptionPane.showConfirmDialog(null, "Desea salir del programa?");
                	if(res == JOptionPane.YES_OPTION) {
                		System.exit(0);
                	}

	            }
                    if(this.lento==true){
	                    try {
	                        Thread.sleep(5000);
	                     
	                        
	                    } catch (Exception e) {
	                        }
                    }
        }
	                
}
    
