def watch_youtube(params):
    from pytube import YouTube
    
    try:
        # Crear objeto YouTube
        yt = YouTube("https://www.youtube.com/watch?v=" + params)
        
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
