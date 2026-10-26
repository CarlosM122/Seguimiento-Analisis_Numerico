package ranking;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class Graficos {
    private static final String[] COLORES = {"#2a6f97", "#e76f51", "#2a9d8f"};

    private Graficos() {
    }

    public static void top(Map<String, Termino[]> topPorAlgoritmo, Path ruta) throws IOException {
        int panelAncho = 320, filaAlto = 26, margenIzq = 110, y0 = 70;
        int filas = topPorAlgoritmo.values().iterator().next().length;
        int ancho = panelAncho * topPorAlgoritmo.size() + 20;
        int alto = y0 + filas * filaAlto + 30;
        int maxFrec = 0;
        for (Termino[] top : topPorAlgoritmo.values()) {
            maxFrec = Math.max(maxFrec, top[0].frecuencia());
        }

        StringBuilder svg = inicio(ancho, alto);
        texto(svg, 20, 30, 18, "#222", "Top " + filas + " términos más frecuentes en los abstracts del corpus");
        int panel = 0;
        for (Map.Entry<String, Termino[]> e : topPorAlgoritmo.entrySet()) {
            int x0 = 10 + panel * panelAncho;
            int anchoBarras = panelAncho - margenIzq - 40;
            texto(svg, x0 + margenIzq, 55, 14, "#222", e.getKey());
            Termino[] top = e.getValue();
            for (int i = 0; i < top.length; i++) {
                int y = y0 + i * filaAlto;
                int largo = (int) Math.round((double) top[i].frecuencia() / maxFrec * anchoBarras);
                svg.append(String.format(Locale.ROOT,
                        "<rect x='%d' y='%d' width='%d' height='%d' fill='%s'/>%n",
                        x0 + margenIzq, y, largo, filaAlto - 6, COLORES[0]));
                textoDerecha(svg, x0 + margenIzq - 6, y + 15, 12, "#333", escapar(top[i].termino()));
                texto(svg, x0 + margenIzq + largo + 4, y + 15, 11, "#333", String.valueOf(top[i].frecuencia()));
            }
            panel++;
        }
        guardar(svg, ruta);
    }

    public static void escalabilidad(List<String> algoritmos, int[] tamanos, double[][] medianas, Path ruta)
            throws IOException {
        int ancho = 760, alto = 420, izq = 70, der = 590, arriba = 70, abajo = 360;
        double maxMs = 0;
        for (double[] fila : medianas) {
            for (double v : fila) {
                maxMs = Math.max(maxMs, v);
            }
        }
        maxMs *= 1.1;
        int maxN = tamanos[tamanos.length - 1];

        StringBuilder svg = inicio(ancho, alto);
        texto(svg, 20, 30, 18, "#222", "Crecimiento del tiempo de ejecución con n");
        texto(svg, 20, 55, 12, "#666", "Tiempo (ms, mediana)");
        for (int i = 0; i <= 4; i++) {
            double valor = maxMs * i / 4;
            int y = (int) (abajo - (abajo - arriba) * i / 4.0);
            svg.append(String.format(Locale.ROOT,
                    "<line x1='%d' x2='%d' y1='%d' y2='%d' stroke='#ddd'/>%n", izq, der, y, y));
            textoDerecha(svg, izq - 6, y + 4, 11, "#666", String.format(Locale.ROOT, "%.1f", valor));
        }
        for (int i = 0; i <= 4; i++) {
            int n = maxN * i / 4;
            int x = izq + (der - izq) * i / 4;
            svg.append(String.format(Locale.ROOT,
                    "<text x='%d' y='%d' font-size='11' fill='#666' text-anchor='middle'>%,d</text>%n",
                    x, abajo + 18, n));
        }
        svg.append(String.format(Locale.ROOT,
                "<text x='%d' y='%d' font-size='12' fill='#666' text-anchor='middle'>n (número de términos)</text>%n",
                (izq + der) / 2, abajo + 42));

        for (int a = 0; a < algoritmos.size(); a++) {
            StringBuilder puntos = new StringBuilder();
            int xUlt = 0, yUlt = 0;
            for (int i = 0; i < tamanos.length; i++) {
                xUlt = (int) (izq + (double) tamanos[i] / maxN * (der - izq));
                yUlt = (int) (abajo - medianas[a][i] / maxMs * (abajo - arriba));
                puntos.append(xUlt).append(',').append(yUlt).append(' ');
                svg.append(String.format(Locale.ROOT,
                        "<circle cx='%d' cy='%d' r='4' fill='%s'/>%n", xUlt, yUlt, COLORES[a]));
            }
            svg.append(String.format(Locale.ROOT,
                    "<polyline points='%s' fill='none' stroke='%s' stroke-width='2.5'/>%n",
                    puntos.toString().trim(), COLORES[a]));
            texto(svg, xUlt + 10, yUlt + 4, 12, COLORES[a], String.format(Locale.ROOT, "%s: %.1f ms",
                    algoritmos.get(a), medianas[a][tamanos.length - 1]));
        }
        guardar(svg, ruta);
    }

    private static StringBuilder inicio(int ancho, int alto) {
        StringBuilder svg = new StringBuilder();
        svg.append(String.format(Locale.ROOT,
                "<svg xmlns='http://www.w3.org/2000/svg' width='%d' height='%d' viewBox='0 0 %d %d' "
                        + "font-family='Arial, sans-serif'>%n<rect width='100%%' height='100%%' fill='white'/>%n",
                ancho, alto, ancho, alto));
        return svg;
    }

    private static void texto(StringBuilder svg, int x, int y, int tam, String color, String contenido) {
        svg.append(String.format(Locale.ROOT, "<text x='%d' y='%d' font-size='%d' fill='%s'>%s</text>%n",
                x, y, tam, color, contenido));
    }

    private static void textoDerecha(StringBuilder svg, int x, int y, int tam, String color, String contenido) {
        svg.append(String.format(Locale.ROOT,
                "<text x='%d' y='%d' font-size='%d' fill='%s' text-anchor='end'>%s</text>%n",
                x, y, tam, color, contenido));
    }

    private static String escapar(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static void guardar(StringBuilder svg, Path ruta) throws IOException {
        svg.append("</svg>\n");
        Files.writeString(ruta, svg.toString(), StandardCharsets.UTF_8);
    }
}
