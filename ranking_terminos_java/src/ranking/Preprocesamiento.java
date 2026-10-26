package ranking;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Preprocesamiento {
    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList((
            "a about above after again against all also am an and any are aren as at be because been "
            + "before being below between both but by can cannot could couldn did didn do does doesn doing "
            + "don down during each few for from further had hadn has hasn have haven having he her here "
            + "hers herself him himself his how i if in into is isn it its itself just let ll me more most "
            + "mustn my myself no nor not now of off on once only or other ought our ours ourselves out over "
            + "own re s same shan she should shouldn so some such t than that the their theirs them "
            + "themselves then there these they this those through to too under until up ve very was "
            + "wasn we were weren what when where which while who whom why will with won would wouldn you "
            + "your yours yourself yourselves d m o y ma ain "
            + "however thus therefore whereas whether well may might must shall via within without among "
            + "upon e g eg ie et al per one two three four five six seven eight nine ten"
    ).split(" ")));

    private static final Pattern PATRON_PALABRA = Pattern.compile("[a-z]+");
    private static final Pattern POSESIVO = Pattern.compile("'s\\b");

    private Preprocesamiento() {
    }

    public static List<Articulo> cargarCorpus(Path ruta) throws IOException {
        List<String> lineas = Files.readAllLines(ruta, StandardCharsets.UTF_8);
        List<Articulo> corpus = new ArrayList<>();
        for (int i = 1; i < lineas.size(); i++) {
            String[] campos = lineas.get(i).split("\t");
            corpus.add(new Articulo(
                    Integer.parseInt(campos[0]),
                    campos[1],
                    campos[2],
                    Arrays.asList(campos[3].split("; ")),
                    campos[4]));
        }
        return corpus;
    }

    public static List<String> tokenizar(String texto) {
        texto = Normalizer.normalize(texto, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
        texto = texto.replace('’', '\'');
        texto = POSESIVO.matcher(texto).replaceAll("");

        List<String> tokens = new ArrayList<>();
        Matcher m = PATRON_PALABRA.matcher(texto);
        while (m.find()) {
            String token = m.group();
            if (token.length() > 1 && !STOPWORDS.contains(token)) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    public static Termino[] contarFrecuencias(List<Articulo> corpus) {
        Map<String, Integer> frecuencias = new LinkedHashMap<>();
        for (Articulo articulo : corpus) {
            for (String token : tokenizar(articulo.resumen())) {
                frecuencias.merge(token, 1, Integer::sum);
            }
        }
        Termino[] pares = new Termino[frecuencias.size()];
        int i = 0;
        for (Map.Entry<String, Integer> e : frecuencias.entrySet()) {
            pares[i++] = new Termino(e.getKey(), e.getValue());
        }
        return pares;
    }
}
