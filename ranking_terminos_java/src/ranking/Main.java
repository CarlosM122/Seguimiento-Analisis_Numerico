package ranking;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.UnaryOperator;

public class Main {
    private static final Path RUTA_CORPUS = Paths.get("data", "corpus.tsv");
    private static final Path CARPETA_RESULTADOS = Paths.get("resultados");
    private static final int TOP = 15;
    private static final int[] TAMANOS_ESCALABILIDAD = {1_000, 5_000, 10_000, 50_000, 100_000, 200_000};

    public static void main(String[] args) throws IOException {
        Locale.setDefault(Locale.ROOT);
        Files.createDirectories(CARPETA_RESULTADOS);

        Map<String, UnaryOperator<Termino[]>> algoritmos = new LinkedHashMap<>();
        algoritmos.put("Counting Sort", Ordenamientos::countingSort);
        algoritmos.put("Radix Sort", Ordenamientos::radixSort);
        algoritmos.put("Bucket Sort", Ordenamientos::bucketSort);

        List<Articulo> corpus = Preprocesamiento.cargarCorpus(RUTA_CORPUS);
        int totalTokens = 0;
        for (Articulo a : corpus) {
            totalTokens += Preprocesamiento.tokenizar(a.resumen()).size();
        }
        Termino[] pares = Preprocesamiento.contarFrecuencias(corpus);
        int k = Arrays.stream(pares).mapToInt(Termino::frecuencia).max().orElse(0);
        System.out.println("Artículos: " + corpus.size());
        System.out.println("Tokens después del preprocesamiento: " + totalTokens);
        System.out.println("Términos distintos (n): " + pares.length);
        System.out.println("Frecuencia máxima (k): " + k);

        Map<String, Termino[]> ordenados = new LinkedHashMap<>();
        for (Map.Entry<String, UnaryOperator<Termino[]>> e : algoritmos.entrySet()) {
            ordenados.put(e.getKey(), e.getValue().apply(pares));
        }
        Termino[] referencia = pares.clone();
        Arrays.sort(referencia, Comparator.comparingInt(Termino::frecuencia).reversed());
        for (Map.Entry<String, Termino[]> e : ordenados.entrySet()) {
            String estado = Arrays.equals(e.getValue(), referencia) ? "OK" : "DIFERENTE";
            System.out.println("Verificación " + e.getKey() + ": " + estado);
        }

        Termino[] lista = ordenados.get("Counting Sort");
        try (PrintWriter w = escritor("frecuencias.csv")) {
            w.println("posicion,termino,frecuencia");
            for (int i = 0; i < lista.length; i++) {
                w.println((i + 1) + "," + lista[i].termino() + "," + lista[i].frecuencia());
            }
        }

        Map<String, Termino[]> topPorAlgoritmo = new LinkedHashMap<>();
        for (Map.Entry<String, Termino[]> e : ordenados.entrySet()) {
            topPorAlgoritmo.put(e.getKey(), Arrays.copyOf(e.getValue(), TOP));
        }
        try (PrintWriter w = escritor("top15.csv")) {
            StringBuilder encabezado = new StringBuilder("posicion");
            for (String nombre : algoritmos.keySet()) {
                encabezado.append(',').append(nombre).append(" - termino,").append(nombre).append(" - frecuencia");
            }
            w.println(encabezado);
            System.out.println("\nTop " + TOP + " (Counting Sort | Radix Sort | Bucket Sort)");
            for (int i = 0; i < TOP; i++) {
                StringBuilder csv = new StringBuilder().append(i + 1);
                StringBuilder consola = new StringBuilder(String.format("%2d.", i + 1));
                for (Termino[] top : topPorAlgoritmo.values()) {
                    csv.append(',').append(top[i].termino()).append(',').append(top[i].frecuencia());
                    consola.append(String.format(" %-14s%4d  |", top[i].termino(), top[i].frecuencia()));
                }
                w.println(csv);
                System.out.println(consola.substring(0, consola.length() - 1).stripTrailing());
            }
        }

        System.out.println("\nTiempos sobre la lista de frecuencias del corpus (100 repeticiones)");
        try (PrintWriter w = escritor("tiempos_corpus.csv")) {
            w.println("algoritmo,n,min_ms,mediana_ms,promedio_ms");
            for (Map.Entry<String, UnaryOperator<Termino[]>> e : algoritmos.entrySet()) {
                Medicion.Tiempos t = Medicion.medir(e.getValue(), pares, 100, 200);
                w.printf("%s,%d,%.4f,%.4f,%.4f%n", e.getKey(), pares.length, t.minimo(), t.mediana(), t.promedio());
                System.out.printf("  %-14s min %8.4f ms   mediana %8.4f ms   promedio %8.4f ms%n",
                        e.getKey(), t.minimo(), t.mediana(), t.promedio());
            }
        }

        System.out.println("\nEscalabilidad (mediana en ms)");
        List<String> nombres = new ArrayList<>(algoritmos.keySet());
        double[][] medianas = new double[nombres.size()][TAMANOS_ESCALABILIDAD.length];
        try (PrintWriter w = escritor("tiempos_escalabilidad.csv")) {
            w.println("n,algoritmo,min_ms,mediana_ms,promedio_ms");
            for (int i = 0; i < TAMANOS_ESCALABILIDAD.length; i++) {
                int n = TAMANOS_ESCALABILIDAD[i];
                Termino[] sintetica = Medicion.listaSintetica(pares, n, 2026);
                int repeticiones = n <= 10_000 ? 100 : 30;
                StringBuilder linea = new StringBuilder(String.format("  n=%7d:", n));
                for (int a = 0; a < nombres.size(); a++) {
                    Medicion.Tiempos t = Medicion.medir(algoritmos.get(nombres.get(a)), sintetica, repeticiones, 30);
                    medianas[a][i] = t.mediana();
                    w.printf("%d,%s,%.4f,%.4f,%.4f%n", n, nombres.get(a), t.minimo(), t.mediana(), t.promedio());
                    linea.append(String.format("  %s %9.3f", nombres.get(a), t.mediana()));
                }
                System.out.println(linea);
            }
        }

        Graficos.top(topPorAlgoritmo, CARPETA_RESULTADOS.resolve("top15.svg"));
        Graficos.escalabilidad(nombres, TAMANOS_ESCALABILIDAD, medianas,
                CARPETA_RESULTADOS.resolve("escalabilidad.svg"));
        System.out.println("\nResultados guardados en: " + CARPETA_RESULTADOS.toAbsolutePath());
    }

    private static PrintWriter escritor(String nombre) throws IOException {
        return new PrintWriter(Files.newBufferedWriter(CARPETA_RESULTADOS.resolve(nombre), StandardCharsets.UTF_8));
    }
}
