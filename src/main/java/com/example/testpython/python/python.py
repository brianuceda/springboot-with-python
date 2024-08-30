# script.py

import sys

def funcion_b(variable):
    print(f"Ejecutando funcion_a con el argumento: {variable}")

def watch_youtube(v):
    from pytube import YouTube

    try:
        # Crear objeto YouTube
        yt = YouTube("https://www.youtube.com/watch?v=" + v)
        
        # Crear objeto con la información
        video_info = {
            'titulo': yt.title,
            'duracion': yt.length,
            'vistas': yt.views,
            'descripcion': yt.description,
            'autor': yt.author
        }

        # Imprimir información en formato JSON
        print(video_info)
        
    except Exception as e:
        print({'error': str(e)})

def search_youtube(params):
    import json
    from youtube_search import YoutubeSearch
    
    # Extraer los parámetros
    params = extract_params(params)
    
    search_param = params[0]
    max_results_param = int(params[1])
    
    try:
        response = YoutubeSearch(search_param, max_results=max_results_param).to_json()
        response_dict = json.loads(response)
        
        # Lista de videos con comillas dobles
        videos = json.dumps(response_dict['videos'])
        
        print(videos)
    except Exception as e:
        print({'error': str(e)})
        
# Privados
def extract_params(params):
    import ast
    
    # Verifica si params es una cadena y conviértelo a lista
    if isinstance(params, str):
        try:
            return ast.literal_eval(params)
        except (ValueError, SyntaxError) as e:
            print({'error': f'Error al convertir los parámetros: {str(e)}'})

# Main
if __name__ == "__main__":
    function_name = sys.argv[1]  # El nombre de la función que se debe ejecutar
    argument = sys.argv[2]       # El argumento que se debe pasar a la función

    # Obtener la función del script por su nombre y ejecutarla
    if function_name in globals():
        func = globals()[function_name]
        func(argument)
    else:
        print({'error': f'La función {function_name} no existe en el script'})
