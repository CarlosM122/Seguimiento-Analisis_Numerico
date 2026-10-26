package ranking;

import java.util.Arrays;
import java.util.Random;
import java.util.function.UnaryOperator;

public final class Medicion {
    public record Tiempos(double minimo, double mediana, double promedio) {
    }

    private Medicion() {
    }

    public static Tiempos medir(UnaryOperator<Termino[]> algoritmo, Termino[] pares,
                                int repeticiones, int calentamiento) {
        for (int i = 0; i < calentamiento; i++) {
            algoritmo.apply(pares.clone());
        }
        System.gc();

        double[] tiempos = new double[repeticiones];
        for (int i = 0; i < repeticiones; i++) {
            Termino[] entrada = pares.clone();
            long inicio = System.nanoTime();
            algoritmo.apply(entrada);
            long fin = System.nanoTime();
            tiempos[i] = (fin - inicio) / 1_000_000.0;
        }

        double[] ordenados = tiempos.clone();
        Arrays.sort(ordenados);
        int mitad = repeticiones / 2;
        double mediana = repeticiones % 2 == 1
                ? ordenados[mitad]
                : (ordenados[mitad - 1] + ordenados[mitad]) / 2.0;
        double suma = 0;
        for (double t : tiempos) {
            suma += t;
        }
        return new Tiempos(ordenados[0], mediana, suma / repeticiones);
    }

    public static Termino[] listaSintetica(Termino[] paresReales, int n, long semilla) {
        Random rnd = new Random(semilla);
        Termino[] lista = new Termino[n];
        for (int i = 0; i < n; i++) {
            int f = paresReales[rnd.nextInt(paresReales.length)].frecuencia();
            lista[i] = new Termino("t" + i, f);
        }
        return lista;
    }
}
