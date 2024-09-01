def process_image(params):
    import sys
    import json
    from io import BytesIO
    from PIL import Image
    import base64

    try:
        data = sys.stdin.buffer.read()
        
        image = Image.open(BytesIO(data))
        
        # Convertir a escala de grises
        gray_image = image.convert("L")

        # Convertir la imagen procesada a base64
        buffered = BytesIO()
        gray_image.save(buffered, format="PNG")
        encoded_image = base64.b64encode(buffered.getvalue()).decode("utf-8")
        
        response_object = {
            'encoded_image': encoded_image
        }

        print(json.dumps(response_object))
    except Exception as e:
        print(json.dumps({'error': str(e)}))
