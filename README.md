
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

### Reglas

> * **Regla 1:** En Java, se puede ejecutar cualquier función de cualquier archivo de Python creando un objeto de la clase `PythonUtils` y usando el método `execute()`.

> * **Regla 2:** Luego de crear `CUALQUIER` archivo Python dentro de `python/*.py`, se debe agregar ese archivo al la lista de `importaciones` de `__main__.py`.

> * **Regla 3:** Los métodos en Python, `SIEMPRE` deben tener un `ÚNICO` parámetro llamado 'params'.

### Ejecución de Funciones de Python desde Java

1. El o los archivos de Python que se quiera(n) ejecutar debe(n) estar ubicados en la `ruta`:

    ```powershell
    src/main/java/com/example/testpython/python/*.py
    ```

2. El método `execute(*, *, ?, ?)` necesita toma `2, 3 o 4 parámetros`:

    * **(*) scriptName:** Nombre del archivo Python (con o sin la extensión .py).
    * **(*) functionName:** Nombre de la función a ejecutar dentro del archivo Python.
    * **(?) params:** Parámetros que se pasarán a la función de Python. Puede ser uno o muchos parámetros.
    * **(?) file:** Archivo de cualquier tipo que se quiera analizar en Python.

3. La forma de enviar una respuesta a SpringBoot es colocando ``print(respuesta)``. La respuesta puede ser de tipo string, boolean, float, object o de **cualquier otro tipo**.

4. Ejemplo ``sin parámetro(s)`` en Java:

    ```java
    String response = pythonUtils.execute("mi_script.py", "mi_funcion");
    ```

    Recepción y uso ``sin parámetro(s)`` en Python:
    ```python
    def example(params):
        print('Hello World')
    ```

5. Ejemplo con ``un solo parámetro`` en Java: 

    ```java
    String example1 = pythonUtils.execute("script.py", "example", "Brian");
    String example2 = pythonUtils.execute("script", "example", 777);
    ```

    Recepción y uso de ``un solo parámetro`` en Python:

    ```python
    def example(param):
        print(param)
    ```

6. Ejemplo con ``múltiples parámetros`` en Java:

    ```java
    List<Object> params = List.of("param1", 2);
    String response = pythonUtils.execute("mi_script", "mi_funcion", params);
    ```

    Se debe crear una **Lista** que contenga los parametros **en orden** que recibirá la función de Python.

    Recepción y uso de ``multiples parámetros`` en Python:

    ```python
    def search_youtube(params):
        # Extraer los parámetros (obligatorio)
        params = extract_params(params)
        
        # Usar los parametros como elementos de una lista
        name = params[0]
        age = int(params[1])
    ```

    Si la función en python tiene más de un parámetro, deben ser reasignados llamando a la función **extract_params(params)**. Se puede acceder a cada uno de los parámetros como elementos de una lista.

7. Ejemplo con o sin `parametros` y con `un parámetro de tipo imagen` en Java:

    * Argumento 3: **Parámetro(s) de cualquier tipo** de que se enviarán a la función **o MultipartFile** que se enviará a Python en formato de Bytes (en caso no se quiera enviar ningún parámetro).
    * Argumento 4: Archivo de tipo **MultipartFile** que será procesado en Python en formato de Bytes.

    ```java
    public ResponseEntity<?> image(@RequestParam MultipartFile file) {
        String example1 = pythonUtils.execute("image.py", "process_image", file);
        String example2 = pythonUtils.execute("image.py", "process_image" 322, file);
        String example3 = pythonUtils.execute("image.py", "process_image" List.of("Brian", 1), file);

        // ...
    }
    ```

    Recepción y uso de ``un parámetro de tipo imagen`` en Python:
    ```python
    def function(params):
        import sys
        import json

        try:
            data = sys.stdin.buffer.read()
            
            response_object = {
                'response': 'respuesta'
            }

            print(json.dumps(response_object))
        except Exception as e:
            print(json.dumps({'error': str(e)}))
    ```

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
