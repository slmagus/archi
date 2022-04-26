FROM maven:3.8.5-jdk-11
COPY . /usr/src/app
WORKDIR /usr/src/app
RUN apt update -y && \
    apt install -y libswt-gtk-4-jni libgtk2.0-cil dbus-x11 xvfb && \
    apt clean
RUN mvn -q clean package -P product
RUN rm -rf /usr/src/app
RUN cp -R /usr/src/app/com.archimatetool.editor.product/target/products/com.archimatetool.editor.product/linux/gtk/x86_64/Archi /opt/
ENV DISPLAY :1