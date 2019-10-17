/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pr1meta;

import static java.lang.Math.ceil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import javafx.util.Pair;

/**
 *
 * @author carol
 */
public class AlgMemetico {

    Random rand;
    Integer semilla;

    public AlgMemetico(int s) {
        rand = new Random(s);
        semilla = s;
    }

    ArrayList<Integer> AM1(ArrayList<ArrayList<Integer>> dom, ArrayList<ArrayList<Integer>> var,
            ArrayList<ArrayList<Integer>> ctr, HashMap<Integer, Integer> hashVar) {
        int t = 0, t2 = 0, coste = 0;
        ArrayList<ArrayList<Integer>> cromosomas = new ArrayList<>();
        ArrayList<ArrayList<Integer>> nuevaP = new ArrayList<>();
        ArrayList<Integer> sol = new ArrayList<>();
        ArrayList<Integer> costes = new ArrayList<>();
        ArrayList<Integer> posi = new ArrayList<>(Collections.nCopies(hashVar.size(), 0));
        AlgGreedySimple greedy = new AlgGreedySimple(semilla);
        Utils utils = new Utils();
        AlgAMBL bl = new AlgAMBL();

        //1º evaluación de la poblacion
        int peorCr = 0, evalu = 0, peorCo, elite = 0, mejorCo, mejorCr, mejorCo20;
        mejorCo = 999999;
        for (int i = 0; i < 50; ++i) {
            cromosomas.add(sol = greedy.greedy(dom, var, ctr, hashVar));
            costes.add(utils.calcularCoste(cromosomas.get(i), ctr, hashVar));
            if (costes.get(i) < mejorCo) {
                mejorCo = costes.get(i);
                elite = i;
            }
            evalu++;
        }
        mejorCr = elite;
        mejorCo20 = mejorCo;

        //esperanza matematica
        int probCruce = (int) ceil(0.7 * 50 / 2);
        while (evalu < 20000) {
            t++;
            t2++;

            //Torneo binario
            int cont = 0;
            for (int i = 0; i <= 2; ++i) {
                int p1, p2;
                ArrayList<Boolean> marcados;
                marcados = new ArrayList<>(Collections.nCopies(hashVar.size(), false));
                for (int j = 0; j < 50 / 2; ++j) {
                    p1 = rand.nextInt(50);
                    while (marcados.get(p1)) {
                        p1 = rand.nextInt(50);
                    }
                    marcados.set(p1, true);
                    p2 = rand.nextInt(50);
                    while (marcados.get(p2)) {
                        p2 = rand.nextInt(50);
                    }
                    marcados.set(p2, true);
                    posi.add(cont, (costes.get(p1) < costes.get(p2)) ? p1 : p2);
                    cont++;
                }
            }

            //Cogemos los cromosomas mas prometedores
            for (int i = 0; i < 50; ++i) {
                nuevaP.add(cromosomas.get(posi.get(i)));
            }
            //Cruzamos padres
            for (int i = 0; i < 0.7 * 2; i += 2) {
                Pair<ArrayList<Integer>, ArrayList<Integer>> p;
                p = utils.cruceBLX(nuevaP.get(i), nuevaP.get(i + 1), dom, var, hashVar);
                evalu++;
                if (evalu >= 10) {
                    break;
                }
            }

            //Mutamos
            int ncr = rand.nextInt(50);
            int p, c = 0;
            ArrayList<Boolean> marc = new ArrayList<>(Collections.nCopies(hashVar.size(), false));
            int m = (int) 0.7 * hashVar.size();
            while (c < m) {
                c++;
                p = rand.nextInt(hashVar.size());
                while (marc.get(p)) {
                    p = rand.nextInt(hashVar.size());
                }
                int rFrec = var.get(hashVar.get(p) - 1).get(1);
                int limite = dom.get(rFrec - 1).size();
                int x = rand.nextInt((limite + 1) + 2);
                int anterior = nuevaP.get(ncr).get(p);
                int nueva = dom.get(rFrec - 1).get(0);
                while (nueva == anterior) {
                    x = rand.nextInt((limite + 1) + 2);
                    nueva = dom.get(rFrec - 1).get(0);
                }
                nuevaP.get(ncr).set(p, nueva);
                marc.set(p, true);
            }

            peorCo = 0;
            for (int i = 0; i < 50; ++i) {
                costes.set(i, utils.calcularCosteGS(nuevaP.get(i), ctr, hashVar));
                evalu++;
                if (evalu >= 10) {
                    break;
                }

                if (costes.get(i) > peorCo) {
                    peorCo = costes.get(i);
                    peorCr = i;
                } else {
                    if (costes.get(i) < mejorCo) {
                        mejorCo = costes.get(i);
                        mejorCr = i;
                    }
                }
            }

            //Si no sobrevive el anterior, lo rescato
            if (!Objects.equals(nuevaP.get(ncr).get(elite), cromosomas.get(ncr).get(elite))) {
                nuevaP.set(peorCr, cromosomas.get(elite));
                costes.set(peorCo, utils.calcularCosteGS(cromosomas.get(elite), ctr, hashVar));
                if (costes.get(peorCr) < mejorCo) {
                    mejorCo = costes.get(peorCr);
                }
                evalu++;
                if (evalu >= 10) {
                    break;
                }
            }

            //Generamos poblacion nueva
            elite = mejorCr;
            cromosomas = nuevaP;

            if (evalu % 10 == 0) {
                for (int i = 0; i < 50; ++i) {
                    Pair<Integer, ArrayList<Integer>> pa;

                    pa = new Pair(bl.busquedaLocal(ctr, dom, var, hashVar, cromosomas.get(i), semilla).getKey(), bl.busquedaLocal(ctr, dom, var, hashVar, cromosomas.get(i), semilla).getValue());
                    cromosomas.set(i, pa.getValue());
                    evalu += pa.getKey();
                    if (costes.get(i) < mejorCo) {
                        mejorCo = costes.get(i);
                        mejorCr = i;
                    }
                }
            }

            //Actualiza el mejor coste de reinicializacion
            if (mejorCo < mejorCo20) {
                mejorCo20 = mejorCo;
                t2 = 0;
            }

            //Reinicializacion
            int total = cromosomas.size();
            ArrayList<Integer> v = new ArrayList<>(Collections.nCopies(total, 0));
//            System.out.println("total "+total);
            for (int i = 0; i < total; ++i) {
                if (v.get(i) == 0) {
                    v.set(i, v.get(i) + 1);
                    for (int j = i + 1; j < total; ++j) {
                        if (cromosomas.get(i) == cromosomas.get(j)) {
                            v.set(i, v.get(i) + 1);
                        }
                    }
                }
            }
            int max = 0;
            for (int i = 0; i < v.size(); ++i) {
                if (v.get(i) > max) {
                    max = v.get(i);
                }
            }
            int porc = (max * 100) / total;
            if (porc >= 80 || t2 == 20) {
                t2 = 0;
                cromosomas.set(0, cromosomas.get(mejorCr));
                costes.set(0, mejorCo);
                for (int i = 0; i < 50; ++i) {
                    costes.set(0, utils.calcularCosteGS(sol, ctr, hashVar));
                    cromosomas.set(i, sol);
                    evalu++;
                    if (evalu >= 10) {
                        break;
                    }

                    if (costes.get(i) < mejorCo) {
                        mejorCo = costes.get(i);
                        elite = i;
                    }
                }
                mejorCr = elite;
            }
        }
        System.out.println("iteraciones " + evalu);
        System.out.println("Coste solución final: " + utils.calcularCosteGS(cromosomas.get(mejorCr), ctr, hashVar));
        return cromosomas.get(mejorCr);
    }

    ArrayList<Integer> AM2(ArrayList<ArrayList<Integer>> dom, ArrayList<ArrayList<Integer>> var,
            ArrayList<ArrayList<Integer>> ctr, HashMap<Integer, Integer> hashVar) {
        int t = 0, t2 = 0, coste = 0;
        ArrayList<ArrayList<Integer>> cromosomas = new ArrayList<>();
        ArrayList<ArrayList<Integer>> nuevaP = new ArrayList<>();
        ArrayList<Integer> sol = new ArrayList<>();
        ArrayList<Integer> costes = new ArrayList<>();
        ArrayList<Integer> posi = new ArrayList<>(Collections.nCopies(hashVar.size(), 0));
        AlgGreedySimple greedy = new AlgGreedySimple(semilla);
        Utils utils = new Utils();
        AlgAMBL bl = new AlgAMBL();
        int peorCr = 0, evalu = 0, peorCo, elite = 0, mejorCo, mejorCr, mejorCo20;
        //1º evaluacion y carga inicial
        mejorCo = 999999;
        for (int i = 0; i < 50; ++i) {
            cromosomas.add(sol = greedy.greedy(dom, var, ctr, hashVar));
            costes.add(utils.calcularCoste(cromosomas.get(i), ctr, hashVar));
            if (costes.get(i) < mejorCo) {
                mejorCo = costes.get(i);
                elite = i;
            }
            evalu++;
        }
        mejorCr = elite;
        mejorCo20 = mejorCo;

        //esperanza matematica
        int probCruce = (int) ceil(0.7 * 50 / 2);
        while (evalu < 20000) {
            t++;
            t2++;

            //Torneo binario
            int cont = 0;
            for (int i = 0; i <= 2; ++i) {
                int p1, p2;
                ArrayList<Boolean> marcados;
                marcados = new ArrayList<>(Collections.nCopies(hashVar.size(), false));
                for (int j = 0; j < 50 / 2; ++j) {
                    p1 = rand.nextInt(50);
                    while (marcados.get(p1)) {
                        p1 = rand.nextInt(50);
                    }
                    marcados.set(p1, true);
                    p2 = rand.nextInt(50);
                    while (marcados.get(p2)) {
                        p2 = rand.nextInt(50);
                    }
                    marcados.set(p2, true);
                    posi.add(cont, (costes.get(p1) < costes.get(p2)) ? p1 : p2);
                    cont++;
                }
            }

            //Cogemos los cromosomas prometedores
            for (int i = 0; i < 50; ++i) {
                nuevaP.add(cromosomas.get(posi.get(i)));
            }

            //Cruzamos padres del 0.7
            for (int i = 0; i < probCruce * 2; i += 2) {
                Pair<ArrayList<Integer>, ArrayList<Integer>> p;
                p = utils.cruceBLX(nuevaP.get(i), nuevaP.get(i + 1), dom, var, hashVar);
                evalu++;
                if (evalu >= 10) {
                    break;
                }
            }

            //Mutamos
            int ncr = rand.nextInt(50);
            int p, c = 0;
            ArrayList<Boolean> marc = new ArrayList<>(Collections.nCopies(hashVar.size(), false));
            int m = (int) 0.7 * hashVar.size();
            while (c < m) {
                c++;
                p = rand.nextInt(hashVar.size());
                while (marc.get(p)) {
                    p = rand.nextInt(hashVar.size());
                }
                int rFrec = var.get(hashVar.get(p) - 1).get(1);
                int limite = dom.get(rFrec - 1).size();
                int x = rand.nextInt((limite + 1) + 2);
                int anterior = nuevaP.get(ncr).get(p);
                int nueva = dom.get(rFrec - 1).get(0);
                while (nueva == anterior) {
                    x = rand.nextInt((limite + 1) + 2);
                    nueva = dom.get(rFrec - 1).get(0);
                }
                nuevaP.get(ncr).set(p, nueva);
                marc.set(p, true);
            }

            peorCo = 0;
            for (int i = 0; i < 50; ++i) {
                costes.set(i, utils.calcularCosteGS(nuevaP.get(i), ctr, hashVar));
                evalu++;
                if (evalu >= 10) {
                    break;
                }

                if (costes.get(i) > peorCo) {
                    peorCo = costes.get(i);
                    peorCr = i;
                } else {
                    if (costes.get(i) < mejorCo) {
                        mejorCo = costes.get(i);
                        mejorCr = i;
                    }
                }
            }

            //Si no sobrevive el mejor anterior, lo rescato
            if (!Objects.equals(nuevaP.get(ncr).get(elite), cromosomas.get(ncr).get(elite))) {
                nuevaP.set(peorCr, cromosomas.get(elite));
                costes.set(peorCr, utils.calcularCosteGS(cromosomas.get(elite), ctr, hashVar));
                if (costes.get(peorCr) < mejorCo) {
                    mejorCo = costes.get(peorCr);
                }
                evalu++;
                if (evalu >= 10) {
                    break;
                }
            }

            //Generamos poblacion nueva
            elite = mejorCr;
            cromosomas = nuevaP;

            if (evalu % 10 == 0) {
                for (int i = 0; i < 50 * 0.1; ++i) {
                    do {
                        p = rand.nextInt(50);
                    } while (marc.get(p));
                    marc.set(p, true);
                    Pair<Integer, ArrayList<Integer>> pa;

                    pa = new Pair(bl.busquedaLocal(ctr, dom, var, hashVar, cromosomas.get(i), semilla).getKey(), bl.busquedaLocal(ctr, dom, var, hashVar, cromosomas.get(i), semilla).getValue());
                    cromosomas.set(i, pa.getValue());
                    evalu += pa.getKey();
                    if (costes.get(i) < mejorCo) {
                        mejorCo = costes.get(i);
                        mejorCr = i;
                    }
                }
            }
            //Actualiza el mejor coste de reinicializacion
            if (mejorCo < mejorCo20) {
                mejorCo20 = mejorCo;
                t2 = 0;
            }
            // Reinicializacion
            int total = cromosomas.size();
            ArrayList<Integer> v = new ArrayList<>(Collections.nCopies(total, 0));
//            System.out.println("total "+total);
            for (int i = 0; i < total; ++i) {
                if (v.get(i) == 0) {
                    v.set(i, v.get(i) + 1);
                    for (int j = i + 1; j < total; ++j) {
                        if (cromosomas.get(i) == cromosomas.get(j)) {
                            v.set(i, v.get(i) + 1);
                        }
                    }
                }
            }
            int max = 0;
            for (int i = 0; i < v.size(); ++i) {
                if (v.get(i) > max) {
                    max = v.get(i);
                }
            }
            int porc = (max * 100) / total;
            if (porc >= 80 || t2 == 20) {
                t2 = 0;
                cromosomas.set(0, cromosomas.get(mejorCr));
                costes.set(0, mejorCo);
                for (int i = 0; i < 50; ++i) {
                    costes.set(0, utils.calcularCosteGS(sol, ctr, hashVar));
                    cromosomas.set(i, sol);
                    evalu++;
                    if (evalu >= 10) {
                        break;
                    }

                    if (costes.get(i) < mejorCo) {
                        mejorCo = costes.get(i);
                        elite = i;
                    }
                }
                mejorCr = elite;
            }

        }
        System.out.println("iteraciones " + evalu);
        System.out.println("Coste solución final: " + utils.calcularCosteGS(cromosomas.get(mejorCr), ctr, hashVar));
        return cromosomas.get(mejorCr);

    }

    ArrayList<Integer> AM3(ArrayList<ArrayList<Integer>> dom, ArrayList<ArrayList<Integer>> var,
            ArrayList<ArrayList<Integer>> ctr, HashMap<Integer, Integer> hashVar) {
        int t = 0, t2 = 0, coste = 0;
        ArrayList<ArrayList<Integer>> cromosomas = new ArrayList<>();
        ArrayList<ArrayList<Integer>> nuevaP = new ArrayList<>();
        ArrayList<Integer> sol = new ArrayList<>();
        ArrayList<Integer> costes = new ArrayList<>();
        ArrayList<Integer> posi = new ArrayList<>(Collections.nCopies(hashVar.size(), 0));
        AlgGreedySimple greedy = new AlgGreedySimple(semilla);
        Utils utils = new Utils();
        AlgAMBL bl = new AlgAMBL();
        int peorCr = 0, evalu = 0, peorCo, elite = 0, mejorCo, mejorCr, mejorCo20, mejorCr2 = 0, mejorCo2;
        //1º evaluacion y carga inicial
        mejorCo = 999999;
        for (int i = 0; i < 50; ++i) {
            cromosomas.add(sol = greedy.greedy(dom, var, ctr, hashVar));
            costes.add(utils.calcularCoste(cromosomas.get(i), ctr, hashVar));
            if (costes.get(i) < mejorCo) {
                mejorCo = costes.get(i);
                mejorCo2 = costes.get(i);
                elite = i;
            }
            evalu++;
        }
        mejorCr = elite;
        mejorCo20 = mejorCo;

        //esperanza matematica
        int probCruce = (int) ceil(0.7 * 50 / 2);
        while (evalu < 20000) {
            t++;
            t2++;

            //Torneo binario
            int cont = 0;
            for (int i = 0; i <= 2; ++i) {
                int p1, p2;
                ArrayList<Boolean> marcados;
                marcados = new ArrayList<>(Collections.nCopies(hashVar.size(), false));
                for (int j = 0; j < 50 / 2; ++j) {
                    p1 = rand.nextInt(50);
                    while (marcados.get(p1)) {
                        p1 = rand.nextInt(50);
                    }
                    marcados.set(p1, true);
                    p2 = rand.nextInt(50);
                    while (marcados.get(p2)) {
                        p2 = rand.nextInt(50);
                    }
                    marcados.set(p2, true);
                    posi.add(cont, (costes.get(p1) < costes.get(p2)) ? p1 : p2);
                    cont++;
                }
            }

            //Cogemos los cromosomas prometedores
            for (int i = 0; i < 50; ++i) {
                nuevaP.add(cromosomas.get(posi.get(i)));
            }

            //Cruzamos padres del 0.7
            for (int i = 0; i < probCruce * 2; i += 2) {
                Pair<ArrayList<Integer>, ArrayList<Integer>> p;
                p = utils.cruceBLX(nuevaP.get(i), nuevaP.get(i + 1), dom, var, hashVar);
                evalu++;
                if (evalu >= 10) {
                    break;
                }
            }

            //Mutamos
            int ncr = rand.nextInt(50);
            int p, c = 0;
            ArrayList<Boolean> marc = new ArrayList<>(Collections.nCopies(hashVar.size(), false));
            int m = (int) 0.7 * hashVar.size();
            while (c < m) {
                c++;
                p = rand.nextInt(hashVar.size());
                while (marc.get(p)) {
                    p = rand.nextInt(hashVar.size());
                }
                int rFrec = var.get(hashVar.get(p) - 1).get(1);
                int limite = dom.get(rFrec - 1).size();
                int x = rand.nextInt((limite + 1) + 2);
                int anterior = nuevaP.get(ncr).get(p);
                int nueva = dom.get(rFrec - 1).get(0);
                while (nueva == anterior) {
                    x = rand.nextInt((limite + 1) + 2);
                    nueva = dom.get(rFrec - 1).get(0);
                }
                nuevaP.get(ncr).set(p, nueva);
                marc.set(p, true);
            }

            peorCo = 0;
            for (int i = 0; i < 50; ++i) {
                costes.set(i, utils.calcularCosteGS(nuevaP.get(i), ctr, hashVar));
                evalu++;
                if (evalu >= 10) {
                    break;
                }

                if (costes.get(i) > peorCo) {
                    peorCo = costes.get(i);
                    peorCr = i;
                } else {
                    if (costes.get(i) < mejorCo) {
                        mejorCo2 = mejorCo;
                        mejorCr2 = mejorCr;
                        mejorCo = costes.get(i);
                        mejorCr = i;
                    } else {
                        if(costes.get(i) < mejorCr2){
                            mejorCo2 = costes.get(i);
                            mejorCr2 = i;
                        }
                    }
                }
            }

            //Si no sobrevive el mejor anterior, lo rescato
            if (!Objects.equals(nuevaP.get(ncr).get(elite), cromosomas.get(ncr).get(elite))) {
                nuevaP.set(peorCr, cromosomas.get(elite));
                costes.set(peorCr, utils.calcularCosteGS(cromosomas.get(elite), ctr, hashVar));
                if (costes.get(peorCr) < mejorCo) {
                    mejorCo = costes.get(peorCr);
                }
                evalu++;
                if (evalu >= 10) {
                    break;
                }
            }

            //Generamos poblacion nueva
            elite = mejorCr;
            cromosomas = nuevaP;

            if (evalu % 10 == 0) {

                Pair<Integer, ArrayList<Integer>> pa;
                pa = new Pair(bl.busquedaLocal(ctr, dom, var, hashVar, cromosomas.get(mejorCr), semilla).getKey(), bl.busquedaLocal(ctr, dom, var, hashVar, cromosomas.get(mejorCr), semilla).getValue());
                evalu += pa.getKey();
                if (costes.get(mejorCr) < mejorCo) {
                    mejorCo = costes.get(mejorCr);
                    mejorCr = mejorCr;
                }
                
                pa = new Pair(bl.busquedaLocal(ctr, dom, var, hashVar, cromosomas.get(mejorCr), semilla).getKey(), bl.busquedaLocal(ctr, dom, var, hashVar, cromosomas.get(mejorCr), semilla).getValue());
                evalu += pa.getKey();
                if (costes.get(mejorCr2) < mejorCo) {
                    mejorCo = costes.get(mejorCr2);
                    mejorCr = mejorCr2;
                }
            }
            //Actualiza el mejor coste de reinicializacion
            if (mejorCo < mejorCo20) {
                mejorCo20 = mejorCo;
                t2 = 0;
            }
            // Reinicializacion
            int total = cromosomas.size();
            ArrayList<Integer> v = new ArrayList<>(Collections.nCopies(total, 0));
//            System.out.println("total "+total);
            for (int i = 0; i < total; ++i) {
                if (v.get(i) == 0) {
                    v.set(i, v.get(i) + 1);
                    for (int j = i + 1; j < total; ++j) {
                        if (cromosomas.get(i) == cromosomas.get(j)) {
                            v.set(i, v.get(i) + 1);
                        }
                    }
                }
            }
            int max = 0;
            for (int i = 0; i < v.size(); ++i) {
                if (v.get(i) > max) {
                    max = v.get(i);
                }
            }
            int porc = (max * 100) / total;
            if (porc >= 80 || t2 == 20) {
                t2 = 0;
                cromosomas.set(0, cromosomas.get(mejorCr));
                costes.set(0, mejorCo);
                for (int i = 0; i < 50; ++i) {
                    costes.set(0, utils.calcularCosteGS(sol, ctr, hashVar));
                    cromosomas.set(i, sol);
                    evalu++;
                    if (evalu >= 10) {
                        break;
                    }

                    if (costes.get(i) < mejorCo) {
                        mejorCo = costes.get(i);
                        elite = i;
                    }
                }
                mejorCr = elite;
            }

        }
        System.out.println("iteraciones " + evalu);
        System.out.println("Coste solución final: " + utils.calcularCosteGS(cromosomas.get(mejorCr), ctr, hashVar));
        return cromosomas.get(mejorCr);

    }
}
