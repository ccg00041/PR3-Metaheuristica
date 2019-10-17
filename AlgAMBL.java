/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pr1meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import javafx.util.Pair;

/**
 *
 * @author carol
 */
public class AlgAMBL {

    public AlgAMBL() {

    }

    /**
     * Con HashVar para los ficheros scen
     *
     * @param matrizCTR
     * @param matrizDOM
     * @param matrizVAR
     * @param hashVar
     * @param sol_actual
     * @param semilla
     * @return
     */
    public Pair<Integer, ArrayList<Integer>> busquedaLocal(ArrayList<ArrayList<Integer>> matrizCTR, ArrayList<ArrayList<Integer>> matrizDOM,
            ArrayList<ArrayList<Integer>> matrizVAR, HashMap<Integer, Integer> hashVar, ArrayList<Integer> sol_actual, Integer semilla) {
        ArrayList<Integer> sol_generada = new ArrayList<>(), sol_anterior = new ArrayList<>();

        Utils utils = new Utils();
//        MostrarDatos mostrar = new MostrarDatos();//QUITAR
        Integer coste_sol_actual = utils.calcularCoste(sol_actual, matrizCTR, hashVar);
        Integer coste_sol_generada = 0, coste_sol_anterior = 0;
        Integer iter = 0; //iteraciones, condición parada it=10000 para calcular el vecino
        Integer r_frec = 0, trx = 0, primer_trx, direc = 0, frec_elegida = 0;
        Integer dir_frec = -1;// 0 izq, 1 der
        boolean mejoraFrec = false, entorno_completo = false;
        Random posRandom = new Random(semilla);
        sol_generada = sol_actual;

        //Generar posicion al azar para elegir TRX( de 0 a tam-1)
        trx = posRandom.nextInt(sol_actual.size() - 1);
        primer_trx = trx;

        while (iter < 200 && (coste_sol_generada >= coste_sol_anterior) && (!entorno_completo)) {
            if (trx == primer_trx && iter > 0) {
                entorno_completo = true;
                mejoraFrec = true;
            }
            //Obtengo rango_frec correspondiente para acceder a su lista de frecuencias con DOM (trx+1 ya que trx empieza en 0 en el arraylist)

            r_frec = matrizVAR.get(trx).get(1) - 1; //QUITO -1 para empezar desde 0
            frec_elegida = sol_actual.get(trx);

            dir_frec = posRandom.nextInt(2);
            int i = 0;

            i = matrizDOM.get(r_frec).indexOf(frec_elegida);//PUEDE PROVOCAR FUERA DE RANGO EN ALGUN CASO, evito hacer un for y recorrer todo ese vector
            if(i == -1){
                i = 0;
            }
            if (matrizDOM.get(r_frec).get(i) == frec_elegida) {//encuentro la posicion de la frecuencia y elijo la frec de la derecha o de la izq
                //Sigo buscando frecuencia hasta que mejore

                Integer cont = 0;
                while (!mejoraFrec) {
                    //Cada vez que hago un set, la frec_elegida la actualizo (así en la siguiente iteración ya esta actualizada)
                    if (dir_frec == 0) {//la cambio por la frec de la izq
                        if (i == 0) {//si es el primer elemento de la lista de frecuencias (
                            //Cambio la frecuencia de ese TRX por la última de la lista de frecuencias
                            sol_generada.set(trx, matrizDOM.get(r_frec).get(matrizDOM.get(r_frec).size() - 1));
                            frec_elegida = matrizDOM.get(r_frec).get(matrizDOM.get(r_frec).size() - 1);
                            i = matrizDOM.get(r_frec).size() - 1;
                        } else {
                            sol_generada.set(trx, matrizDOM.get(r_frec).get(i - 1));
                            frec_elegida = matrizDOM.get(r_frec).get(i - 1);
                        }
                        i--;
                    } else {
                        i = (i + 1) % matrizDOM.get(r_frec).size();
                        sol_generada.set(trx, matrizDOM.get(r_frec).get(i));
                        frec_elegida = matrizDOM.get(r_frec).get(i);
                    }
//                        primera = false;
                    //FACTORIZACION VER ESTO (la factorizacion la hago pasando la frecuencia nueva para tener la solucion con la orginal) 
//                    coste_sol_generada = utils.factorizacion(sol_actual, matrizCTR, coste_sol_actual, trx, sol_actual.get(trx));
                    coste_sol_generada = utils.calcularCoste(sol_generada, matrizCTR, hashVar);

                    if (coste_sol_generada < coste_sol_actual) { //Si encuentro un vecino con menor coste, entonces ya no busco más frecuencias y termino con este TRX
                        mejoraFrec = true;
                    }

                    if (cont == matrizDOM.get(r_frec).size() - 1) {
                        mejoraFrec = true;
                    }
                    cont++;
                }
            }
            iter++;
            if (coste_sol_generada < coste_sol_actual) {
                sol_anterior = sol_actual;
                coste_sol_anterior = coste_sol_actual;
                sol_actual = sol_generada;
                coste_sol_actual = coste_sol_generada;
            }
            //Compruebo si mejora con la solución con la factorización, si mejora la mantengo y cambio a otro trasmisor, sino sigo buscando la siguiente frecuencia
            trx++; //sigo recorriendo el vector solución y probando si mejorarian los distintos TRX

            if (trx == sol_actual.size()) {//Si el TRX ha superado el tam 
                trx = 0;
            }

        }
        System.out.println("iteraciones " + iter);
        System.out.println("Coste solución final: " + coste_sol_actual);
        Pair<Integer, ArrayList<Integer>> p = new Pair<>(iter, sol_actual);

        return p;
    }
}
