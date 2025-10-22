# Simulador AFD (Aplicación de escritorio en Java Swing)

Esta aplicación permite cargar, editar y simular paso a paso un Autómata Finito Determinista (AFD).

Características principales:
- Menú Archivo: Abrir, Nuevo, Guardar, Salir.
- Menú Ejemplos: 3 AFD integrados listos para usar.
- Menú Acerca de…: Acerca de y Ayuda.
- Tabla de transiciones (estática) generada a partir del AFD.
- Diagrama del AFD dibujado y resaltado dinámicamente durante la simulación.
- Tabla de cadenas: agregue cadenas y procese todas para ver si son aceptadas.
- Controles de simulación: Anterior, Siguiente, Reiniciar, Auto (reproducción).
- Diseño moderno con Look&Feel Nimbus.

## Formato del archivo de AFD
Ejemplo de archivo de texto soportado:

```
# Comentarios con # o //
symbols: a,b
states: q0,q1,q2
start: q0
finals: q2
transitions:
q0,a->q1
q1,b->q2
q2,a->q2
strings:
ab
aab
```

- symbols: alfabeto separado por comas
- states: estados separados por comas
- start: estado inicial
- finals: estados de aceptación separados por comas
- transitions: una por línea con el patrón `origen,símbolo->destino`
- strings: (opcional) lista de cadenas a evaluar

## Ejecución
- Requisitos: JDK 17+ (o el que utilice su IDE)
- Abrir el proyecto en IntelliJ IDEA y ejecutar la clase `Main`.

## Generar instalador (Windows)
Se incluye una plantilla de script de Inno Setup `installer.iss`.

Pasos:
1) Generar un JAR ejecutable (IntelliJ > Build > Build Artifacts > Jar > From modules with dependencies). Nombre sugerido: `dist/SimuladorAFD.jar`.
2) Instalar Inno Setup: https://jrsoftware.org/isinfo.php
3) Abrir `installer.iss` en Inno Setup.
4) Ajustar si es necesario la ruta del JAR en la sección `[Files]`.
5) Compilar el instalador para obtener `SimuladorAFD-Setup.exe`.

## Créditos
- Universidad, curso, fecha e integrantes pueden editarse en el diálogo "Acerca de" dentro de la app.
