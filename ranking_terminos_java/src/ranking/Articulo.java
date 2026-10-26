package ranking;

import java.util.List;

public record Articulo(int id, String archivo, String titulo, List<String> autores, String resumen) {
}
