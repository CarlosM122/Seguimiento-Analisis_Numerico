# Ranking de términos con ordenamientos no comparativos (versión Java)

Es la misma solución que la versión en Python (`ranking_terminos/`), con las mismas clases, los mismos pasos y los mismos resultados: la lista de 980 términos que produce es idéntica. Solo usa la librería estándar de Java, sin dependencias externas.

## Estructura

```
ranking_terminos_java/
├── data/corpus.tsv              # id, archivo, título, autores y abstract de los 20 artículos
├── src/ranking/
│   ├── Main.java                # Punto de entrada: ejecuta todo el flujo
│   ├── Articulo.java            # record: un artículo del corpus
│   ├── Termino.java             # record: par (término, frecuencia)
│   ├── Preprocesamiento.java    # tokenización, stopwords y conteo de frecuencias
│   ├── Ordenamientos.java       # Counting Sort, Radix Sort y Bucket Sort
│   ├── Medicion.java            # medición de tiempos en ms
│   └── Graficos.java            # gráficos en SVG, escritos sin librerías
└── resultados/                  # se genera al ejecutar
    ├── frecuencias.csv
    ├── top15.csv
    ├── tiempos_corpus.csv
    ├── tiempos_escalabilidad.csv
    ├── top15.svg
    └── escalabilidad.svg
```

| Python | Java |
| --- | --- |
| `src/preprocesamiento.py` | `Preprocesamiento.java` |
| `src/ordenamientos.py` | `Ordenamientos.java` |
| `src/medicion.py` | `Medicion.java` |
| `main.py` | `Main.java` + `Graficos.java` |
| `data/corpus.json` | `data/corpus.tsv` (mismos datos, en un formato que Java lee sin librerías) |

## Requisitos

JDK 17 o superior, porque se usan `record`. Para verificarlo, en una terminal ejecuta `java -version`.

## Abrirlo en IntelliJ IDEA

El proyecto ya trae la configuración de IntelliJ (carpeta `.idea` y archivo `ranking_terminos_java.iml`).

1. **File > Open** y selecciona la carpeta `ranking_terminos_java`.
2. Si arriba aparece el aviso **Project JDK is not defined**, haz clic en **Setup SDK** y elige un JDK 17 o superior. Si no tienes uno, IntelliJ ofrece descargarlo ahí mismo.
3. Arriba a la derecha elige la configuración **Main** y pulsa el triángulo verde.

Si aparece el error **The output path is not specified for module ranking_terminos_java**, ve a **File > Project Structure > Project** y en **Compiler output** pon la carpeta del proyecto seguida de `\out` (por ejemplo `C:\...\ranking_terminos_java\out`). Luego pulsa **OK** y vuelve a ejecutar.

El programa busca `data/corpus.tsv` relativo a la carpeta del proyecto. Si sale un error de archivo no encontrado, ve a **Run > Edit Configurations** y en **Working directory** pon la carpeta `ranking_terminos_java`.

Los gráficos `.svg` se abren con cualquier navegador y se pueden insertar en Word (Insertar > Imágenes).

## Ejecutar desde la terminal

```
cd ranking_terminos_java
javac -encoding UTF-8 -d out src/ranking/*.java
java -cp out ranking.Main
```

## Diferencias con la versión en Python

- **Calentamiento:** la JVM compila el código a máquina mientras se ejecuta (JIT). Por eso se hacen 200 ejecuciones de calentamiento antes de medir sobre el corpus, y 30 en la prueba de escalabilidad. Sin ellas, las primeras mediciones incluirían el tiempo de compilación.
- **Recolector de basura:** Java no permite desactivarlo, así que se pide una limpieza con `System.gc()` antes de cada medición.
- **Tiempos:** son unas 10 a 20 veces menores que en Python, porque Java compila el código y Python lo interpreta. El crecimiento sigue siendo lineal en los tres algoritmos.
- **Lista de escalabilidad:** se genera con `java.util.Random`, así que los términos sintéticos no son los mismos que en Python, aunque siguen la misma distribución de frecuencias.
