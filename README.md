
## 1. Desplegar a Producción

1. Activar el entorno virtual en la terminal:
   ```bash
   venv/Scripts/activate
   ```

2. Generar un nuevo archivo con las librerías a instalar exportadas:
   ```bash
   pip freeze > requirements.txt
   ```

## 2. Conversión Automática de Python a Java

Cuando recibimos datos en formato `snake_case` de Python (por ejemplo, `long_desc`), Jackson los convierte automáticamente a formato `camelCase` en Java (por ejemplo, `longDesc`). Para que esto funcione, las propiedades en el DTO de Java deben tener el mismo nombre que en Python, pero en formato `camelCase`.

### Configuración:

En el código Java, configura el ObjectMapper para usar `snake_case`:

```java
ObjectMapper objectMapper = new ObjectMapper();
objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
```

### Ejemplo:

JSON de `Python`:

```json
{
    "long_desc": "Descripción del video",
    "publish_time": "2024-08-29T12:34:56Z"
}
```

DTO en `Java`:

```java
public class YoutubeSearchDTO {
    private String longDesc;  // Se mapea automáticamente desde "long_desc"
    private String publishTime;  // Se mapea automáticamente desde "publish_time"
}
```

Esto permite que Jackson mapee automáticamente los nombres `snake_case` a `camelCase` sin necesidad de configuración adicional en cada campo.
