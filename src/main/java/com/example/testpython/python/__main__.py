# Nombre de los Archivos nuevos
from test import *
from youtube import *
from image import *

# No tocar
import sys

def extract_params(params):
    import ast
    if isinstance(params, str) and params.startswith('[') and params.endswith(']'):
        try:
            return ast.literal_eval(params)
        except (ValueError, SyntaxError) as e:
            print({'error': f'Error al convertir los parámetros: {str(e)}'})
            return None
    else:
        return params

def main():
    if len(sys.argv) < 3:
        print({'error': 'No se han proporcionado suficientes argumentos para ejecutar la función'})
        return

    script_name = sys.argv[1]
    function_name = sys.argv[2]
    arguments = sys.argv[3] if len(sys.argv) > 3 else "None"

    if function_name in globals():
        func = globals()[function_name]
        params = extract_params(arguments) # Convierte los parámetros a una lista
        func(params) # Ejecuta la función
    else:
        print({'error': f'La función {function_name} no existe en el script'})

if __name__ == "__main__":
    main()