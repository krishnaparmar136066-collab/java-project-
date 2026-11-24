@echo off
cd /d "C:\sem2 project\JAVA"

java --module-path "C:\sem2 project\JAVA\javafx-sdk-27.0.1.1\lib" --add-modules javafx.controls,javafx.fxml -cp ".;ojdbc11.jar" LoginScreen

pause