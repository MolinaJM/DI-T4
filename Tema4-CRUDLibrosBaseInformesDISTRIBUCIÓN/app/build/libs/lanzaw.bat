REM Versión para windows, evita que se vea el comando y también oculta la ventana al terminar
@echo off
start javaw --module-path lib --add-reads org.mariadb.jdbc=ALL-UNNAMED --add-modules javafx.base,javafx.web,javafx.controls,javafx.fxml,javafx.graphics,javafx.media --enable-native-access=javafx.graphics -jar app.jar