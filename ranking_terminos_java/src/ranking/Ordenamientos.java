package ranking;

import java.util.ArrayList;
import java.util.List;

public final class Ordenamientos {
    private Ordenamientos() {
    }

    public static Termino[] countingSort(Termino[] pares) {
        int n = pares.length;
        if (n == 0) {
            return new Termino[0];
        }
        int k = maximo(pares);

        int[] conteo = new int[k + 1];
        for (Termino t : pares) {
            conteo[k - t.frecuencia()]++;
        }

        for (int c = 1; c <= k; c++) {
            conteo[c] += conteo[c - 1];
        }

        Termino[] salida = new Termino[n];
        for (int i = n - 1; i >= 0; i--) {
            int clave = k - pares[i].frecuencia();
            conteo[clave]--;
            salida[conteo[clave]] = pares[i];
        }
        return salida;
    }

    public static Termino[] radixSort(Termino[] pares) {
        return radixSort(pares, 10);
    }

    public static Termino[] radixSort(Termino[] pares, int base) {
        if (pares.length == 0) {
            return new Termino[0];
        }
        int k = maximo(pares);
        Termino[] resultado = pares.clone();
        for (long exp = 1; k / exp > 0; exp *= base) {
            resultado = countingSortPorDigito(resultado, (int) exp, base);
        }
        return resultado;
    }

    private static Termino[] countingSortPorDigito(Termino[] pares, int exp, int base) {
        int n = pares.length;
        int[] conteo = new int[base];
        for (Termino t : pares) {
            conteo[base - 1 - (t.frecuencia() / exp) % base]++;
        }
        for (int c = 1; c < base; c++) {
            conteo[c] += conteo[c - 1];
        }
        Termino[] salida = new Termino[n];
        for (int i = n - 1; i >= 0; i--) {
            int clave = base - 1 - (pares[i].frecuencia() / exp) % base;
            conteo[clave]--;
            salida[conteo[clave]] = pares[i];
        }
        return salida;
    }

    public static Termino[] bucketSort(Termino[] pares) {
        return bucketSort(pares, pares.length);
    }

    public static Termino[] bucketSort(Termino[] pares, int numCubetas) {
        int n = pares.length;
        if (n == 0) {
            return new Termino[0];
        }
        int kMin = minimo(pares);
        int kMax = maximo(pares);
        long rango = (long) kMax - kMin + 1;

        List<List<Termino>> cubetas = new ArrayList<>(numCubetas);
        for (int c = 0; c < numCubetas; c++) {
            cubetas.add(new ArrayList<>());
        }
        for (Termino t : pares) {
            int indice = (int) ((long) (kMax - t.frecuencia()) * numCubetas / rango);
            cubetas.get(indice).add(t);
        }

        Termino[] salida = new Termino[n];
        int pos = 0;
        for (List<Termino> cubeta : cubetas) {
            for (int i = 1; i < cubeta.size(); i++) {
                Termino actual = cubeta.get(i);
                int j = i - 1;
                while (j >= 0 && cubeta.get(j).frecuencia() < actual.frecuencia()) {
                    cubeta.set(j + 1, cubeta.get(j));
                    j--;
                }
                cubeta.set(j + 1, actual);
            }
            for (Termino t : cubeta) {
                salida[pos++] = t;
            }
        }
        return salida;
    }

    private static int maximo(Termino[] pares) {
        int max = pares[0].frecuencia();
        for (Termino t : pares) {
            if (t.frecuencia() > max) {
                max = t.frecuencia();
            }
        }
        return max;
    }

    private static int minimo(Termino[] pares) {
        int min = pares[0].frecuencia();
        for (Termino t : pares) {
            if (t.frecuencia() < min) {
                min = t.frecuencia();
            }
        }
        return min;
    }
}
