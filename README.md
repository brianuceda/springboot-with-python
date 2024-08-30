
<h1 align="center">SpringBoot - Python</h1>

<p align="center">
  <img alt="Github version" src="https://img.shields.io/badge/version-1.0-blue?color=56BEB8">
  <img alt="Repository size" src="https://img.shields.io/github/repo-size/brianuceda/testpython?color=56BEB8">
</p>

<p align="center">
  <a href="#requerimientos">Requerimientos</a> &#xa0; | &#xa0;
  <a href="#clonar">Clonar</a> &#xa0; | &#xa0;
  <a href="#funcionamiento">Funcionamiento</a> &#xa0; | &#xa0;
  <a href="#despliegue">Despliegue</a> &#xa0; | &#xa0;
  <a href="https://github.com/brianuceda" target="_blank">Author</a>
</p>

## Requerimientos

- Python 3.3 (o posteriores)
- Java 17

## Clonar

Pasos a seguir luego de clonar el repositorio:

1. Crear un nuevo entorno virtual de Python dentro del proyecto.

    ```powershell
    python -m venv venv
    ```

2. Entrar al entorno virtual de Python.

    ```powershell
    venv/Scripts/activate
    ```

3. Instalar las dependencias del proyecto.

    ```powershell
    pip install -r requirements.txt
    ```

## Funcionamiento

Este proyecto permite ejecutar funciones de scripts de Python desde una aplicación Spring Boot. Aquí se explica cómo hacer esto paso a paso:

### Ejecución de Funciones de Python desde Java

Se puede ejecutar cualquier función de cualquier archivo de Python usando el método `executeFunction` de la clase `PythonUtils`.

1. El o los archivos de Python que se quiera(n) ejecutar debe(n) estar ubicados en la `ruta`:

    ```bash
    src/main/java/com/example/testpython/python/*.py
    ```

2. El método `executeFunction` necesita toma `2 o 3 parámetros`:

    * **(*) scriptName:** Nombre del archivo Python (con o sin la extensión .py).
    * **(*) functionName:** Nombre de la función a ejecutar dentro del archivo Python.
    * **(?) params:** Parámetros que se pasarán a la función de Python. Puede ser uno o muchos parámetros.

3. Ejemplo con ``sin parámetro(s)``: 

    ```java
    String response = pythonUtils.executeFunction("mi_script.py", "mi_funcion");
    ```

4. Ejemplo con ``un solo parámetro``: 

    ```java
    String response = pythonUtils.executeFunction("mi_script.py", "mi_funcion", 777);
    ```

5. Ejemplo con ``múltiples parámetros``:

    ```java
    List<Object> javaParams = List.of("param1", 2);
    String pythonParams = pythonUtils.convertParamsToListOnPython(javaParams);
    String response = pythonUtils.executeFunction("mi_script", "mi_funcion", pythonParams);
    ```

    La función **convertParamsToListOnPython**, recibe una lista en Java y la convierte a una lista legible en Python.

6. Recepción y uso de ``un solo parámetro`` en Python:

    ```python
    # De SpringBoot viene: param = "Brian"

    def return_name(param):
        print(param)
    ```

    La forma de enviar una respuesta a SpringBoot es colocando ``print(respuesta)``. La respuesta puede ser de tipo string, boolean, float, object o de **cualquier otro tipo**.

6. Recepción y uso de ``multiples parámetros`` en Python:

    ```python
    # De SpringBoot viene: params = ["Brian", 20]

    def search_youtube(params):
        # Extraer los parámetros (obligatorio)
        params = extract_params(params)
        
        # Usar los parametros como elementos de una lista
        name = params[0]
        age = int(params[1])
    ```

    Si la función en python tiene más de un parámetro, deben ser reasignados llamando a la función **extract_params(params)**. Se puede acceder a cada uno de los parámetros como elementos de una lista.

### Conversión Automática de ``snake_case`` **(Python)** a ``camelCase`` **(Java)**:
Después de ejecutar una función de Python, normalmente, se necesita procesar los datos devueltos y convertirlos en un formato que Java pueda entender. Esto se hace utilizando DTOs (Data Transfer Objects).

Cuando se reciben datos en formato snake_case de Python, Jackson (una biblioteca de Java) puede convertirlos automáticamente a camelCase. Para que esto funcione, se configura el ObjectMapper que recibe los datos en Java:

#### Configuración

```java
ObjectMapper objectMapper = new ObjectMapper();
objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
```

Para que esta transformación automática funcione, la propiedad que llega en snake_case de python, debe estar en su forma cammelCase y exactamente igual, en el DTO de Java.

#### Ejemplo:

* Lo que se recibe en formato JSON de `Python`:

    ```json
    {
        "long_desc": "Descripción del video",
        "publish_time": "2024-08-29T12:34:56Z"
    }
    ```

* DTO en `Java`:

    ```java
    public class YoutubeSearchDTO {
        private String longDesc;  // Se mapea automáticamente desde "long_desc"
        private String publishTime;  // Se mapea automáticamente desde "publish_time"
    }
    ```

Esto permite que Jackson mapee automáticamente los nombres `snake_case` a `camelCase` sin necesidad de configuración adicional en cada campo.

## Despliegue
Pasos a seguir antes de lanzar a producción:

1. Entrar al entorno virtual de Python.
    ```powershell
    venv/Scripts/activate
    ```

2. Exportar la lista de librerías usadas en Python.
    ```python
    pip freeze > requirements.txt
    ```

<br>

<p align="center">
  <a href="#springboot---python">Volver al Inicio</a>
</p>
