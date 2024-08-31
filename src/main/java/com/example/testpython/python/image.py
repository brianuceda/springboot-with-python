def process_image(params):
    import sys
    
    from io import BytesIO
    from PIL import Image
    import base64
    
    # Procesar la imagen desde los bytes
    image_data = sys.stdin.buffer.read()
    
    try:
        # Procesar la imagen directamente desde los bytes
        image = Image.open(BytesIO(image_data))
        
        # Procesar la imagen (por ejemplo, convertirla a escala de grises)
        gray_image = image.convert("L")

        # Convertir la imagen procesada a base64
        buffered = BytesIO()
        gray_image.save(buffered, format="PNG")
        encoded_image = base64.b64encode(buffered.getvalue()).decode("utf-8")

        print({'encoded_image': encoded_image})

    except Exception as e:
        print({'error': str(e)})
