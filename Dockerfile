# Usar una imagen base de Ubuntu 20.04
FROM ubuntu:20.04

# Establecer variables de entorno no interactivo para evitar prompts durante la instalación
ENV DEBIAN_FRONTEND=noninteractive

# Actualizar lista de paquetes e instalar dependencias necesarias
RUN apt-get update -y && apt-get upgrade -y && apt-get install -y git

# Instalar Java 17
RUN apt-get install -y maven openjdk-17-jdk && \
    apt-get clean

# Configurar variables de entorno para Java
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

# ? Instalar dependencias necesarias para usar Pyenv
RUN apt-get install -y \
    software-properties-common \
    make build-essential libssl-dev zlib1g-dev \
    libbz2-dev libreadline-dev libsqlite3-dev wget curl llvm \
    libncurses5-dev libncursesw5-dev xz-utils tk-dev libffi-dev \
    liblzma-dev && \
    apt-get clean

# Instalar Pyenv
RUN curl https://pyenv.run | bash

# Configurar Pyenv en el entorno
ENV HOME=/root
ENV PYENV_ROOT="$HOME/.pyenv"
ENV PATH="$PYENV_ROOT/bin:$PYENV_ROOT/shims:$PATH"

# Instalar Python 3.8.0 usando Pyenv
RUN /bin/bash -c "source $HOME/.bashrc && pyenv install 3.8.0 && pyenv global 3.8.0"

# ? Project
# Establecer el directorio de trabajo para la construcción de la aplicación
WORKDIR /app

# Copiar los archivos necesarios
COPY requirements.txt .
COPY pom.xml .
COPY src ./src/

# ? Python
# Crear un entorno virtual en Python
RUN python -m venv venv

# Instalar jep y otras dependencias dentro del entorno virtual
RUN /bin/bash -c "source venv/bin/activate && pip install --upgrade pip && pip install -r requirements.txt"

# ? Maven
# Construir la aplicación de Spring Boot
RUN mvn -f pom.xml clean package -DskipTests

# Exponer el puerto de la aplicación
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "target/testpython-0.0.1-SNAPSHOT.jar"]