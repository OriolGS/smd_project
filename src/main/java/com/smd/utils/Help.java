package com.smd.utils;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class Help {
        /**
         * Método que muestra un diálogo de ayuda con información sobre el programa
         */
        public static void helpManual() {
                // Creamos un diálogo de tipo Alert con el tipo AlertType.INFORMATION
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Ayuda");
                alert.setHeaderText(null);
                alert.setResizable(false);

                // Creamos el contenido del diálogo
                VBox vBox = new VBox();
                vBox.setSpacing(10);

                // Estilos para los textos
                String estiloLabel = "-fx-font-weight: bold; -fx-font-size: 16pt;";
                String estiloLabel2 = "-fx-font-weight: bold; -fx-font-size: 14pt; -fx-font-style: italic;";
                String estiloText = "-fx-font-size: 12pt;";

                Label infoLabel = new Label("Información");
                infoLabel.setStyle(estiloLabel);

                Label infoLabel1 = new Label("SMD");
                infoLabel1.setStyle(estiloLabel2);

                Text infoText1 = new Text(
                                "Este programa está diseñado para automatizar la configuración de la máquina SMT Pick\n"
                                                + "and Place en una empresa de montaje de circuitos electrónicos. \n"
                                                + "Acepta dos tipos de ficheros generados por diferentes programas de diseño de placas\n"
                                                + "electrónicas, los transforma a un formato compatible con las máquinas SMT de la empresa\n"
                                                + "y sube los datos a una base de datos. Adicionalmente, ofrece funcionalidades avanzadas\n"
                                                + "como centrar componentes y transformar coordenadas para componentes en la cara inferior\nde la placa.");
                infoText1.setStyle(estiloText);

                vBox.getChildren().addAll(infoLabel, infoLabel1, infoText1);

                // Establecemos el contenido del diálogo
                alert.getDialogPane().setContent(vBox);

                // Mostramos el diálogo y esperamos a que el usuario lo cierre
                alert.showAndWait();
        }

        public static void helpTutorials() {
                // Creamos un diálogo de tipo Dialog
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Ayuda");
                dialog.setHeaderText("Tutoriales");
                dialog.setResizable(false);

                // Creamos un botón para cerrar el diálogo
                ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
                dialog.getDialogPane().getButtonTypes().addAll(closeButton);

                // Creamos el contenido del diálogo
                VBox vBox = new VBox();
                vBox.setSpacing(10);
                vBox.setPadding(new Insets(10));

                // Estilos para los textos
                String estiloLabel = "-fx-font-weight: bold; -fx-font-size: 16pt;";
                String estiloLabel2 = "-fx-font-weight: bold; -fx-font-size: 14pt; -fx-font-style: italic;";
                String estiloText = "-fx-font-size: 12pt;";

                // Sección Archivo
                Label fileLabel = new Label("Archivo");
                fileLabel.setStyle(estiloLabel);

                Label fileLabel1 = new Label("Abrir...");
                fileLabel1.setStyle(estiloLabel2);

                Text fileText1 = new Text(
                                "Permite abrir un fichero TXT (de Seetrax) y hasta dos CSV (de KiCad) y cargarlos en la tabla.");
                fileText1.setStyle(estiloText);

                TextFlow fileTextFlow = new TextFlow(fileText1);

                Label fileLabel2 = new Label("Exportar...");
                fileLabel2.setStyle(estiloLabel2);

                Text fileText2 = new Text("Muestra 3 opciones: Seleccionar directorio, a asq y a csv.\n");
                Text fileText3 = new Text(
                                "• Seleccionar directorio: Permite indicar que carpeta nos saldrá por defecto cuando exportemos datos.\n");
                Text fileText4 = new Text(
                                "• A asq: Exporta los datos de la tabla a dos ficheros con extensión .asq.\n");
                Text fileText5 = new Text("• A csv: Exporta los datos de la tabla a dos ficheros con extensión .csv.");
                fileText2.setStyle(estiloText);
                fileText3.setStyle(estiloText);
                fileText4.setStyle(estiloText);
                fileText5.setStyle(estiloText);

                TextFlow fileTextFlow2 = new TextFlow(fileText2, fileText3, fileText4, fileText5);

                Label fileLabel3 = new Label("Imprimir");
                fileLabel3.setStyle(estiloLabel2);

                Text fileText6 = new Text(
                                "Concede la possibilidad de imprimir los datos de la tabla ya sea a través de una impresora o a un fichero PDF.");
                fileText6.setStyle(estiloText);

                TextFlow fileTextFlow3 = new TextFlow(fileText6);

                // Sección Base de datos
                Label databaseLabel = new Label("Base de datos");
                databaseLabel.setStyle(estiloLabel);

                Label databaseLabel1 = new Label("Recargar datos");
                databaseLabel1.setStyle(estiloLabel2);

                Text databaseText1 = new Text("Recarga los datos de la tabla desde la base de datos.");
                databaseText1.setStyle(estiloText);

                TextFlow databaseTextFlow = new TextFlow(databaseText1);

                Label databaseLabel2 = new Label("Configurar base de datos");
                databaseLabel2.setStyle(estiloLabel2);

                Text databaseText2 = new Text(
                                "Permite connectarse a una base de datos, se tendrá que indicar la URL, el usuario y la contraseña del servidor MySQL.");
                databaseText2.setStyle(estiloText);

                TextFlow databaseTextFlow2 = new TextFlow(databaseText2);

                // Sección PCB
                Label pcbLabel = new Label("PCB");
                pcbLabel.setStyle(estiloLabel);

                Label pcbLabel1 = new Label("Centrar");
                pcbLabel1.setStyle(estiloLabel2);

                Text pcbText1 = new Text(
                                "Da la posibilidad de centrar todos los componentes respecto a un componente o una posición indicada por el usuario");
                pcbText1.setStyle(estiloText);

                TextFlow pcbTextFlow = new TextFlow(pcbText1);

                Label pcbLabel2 = new Label("Girar placa");
                pcbLabel2.setStyle(estiloLabel2);

                Text pcbText2 = new Text(
                                "Permite invertir la posición de los elementos cuando los componentes se encuentran por la otra cara.");
                pcbText2.setStyle(estiloText);

                TextFlow pcbTextFlow2 = new TextFlow(pcbText2);

                fileTextFlow.maxWidthProperty().bind(dialog.getDialogPane().widthProperty().subtract(50));
                fileTextFlow2.maxWidthProperty().bind(dialog.getDialogPane().widthProperty().subtract(50));
                fileTextFlow3.maxWidthProperty().bind(dialog.getDialogPane().widthProperty().subtract(50));
                databaseTextFlow.maxWidthProperty().bind(dialog.getDialogPane().widthProperty().subtract(50));
                databaseTextFlow2.maxWidthProperty().bind(dialog.getDialogPane().widthProperty().subtract(50));
                pcbTextFlow.maxWidthProperty().bind(dialog.getDialogPane().widthProperty().subtract(50));
                pcbTextFlow2.maxWidthProperty().bind(dialog.getDialogPane().widthProperty().subtract(50));

                // Añadimos los elementos al VBox
                vBox.getChildren().addAll(fileLabel, fileLabel1, fileTextFlow, fileLabel2, fileTextFlow2, fileLabel3,
                                fileTextFlow3, databaseLabel, databaseLabel1, databaseTextFlow, databaseLabel2,
                                databaseTextFlow2,
                                pcbLabel, pcbLabel1, pcbTextFlow, pcbLabel2, pcbTextFlow2);

                dialog.getDialogPane().setContent(vBox);

                dialog.getDialogPane().setPrefSize(1000, 400);

                // Mostramos el diálogo y esperamos a que el usuario lo cierre
                dialog.showAndWait();
        }

        public static void helpAbout() {
                // Creamos un diálogo de tipo Alert con el tipo AlertType.INFORMATION
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Ayuda");
                alert.setHeaderText(null);
                alert.setResizable(false);

                // Creamos el contenido del diálogo
                VBox vBox = new VBox();
                vBox.setSpacing(10);

                // Estilos para los textos
                String estiloLabel = "-fx-font-weight: bold; -fx-font-size: 16pt;";
                String estiloLabel2 = "-fx-font-weight: bold; -fx-font-size: 14pt; -fx-font-style: italic;";
                String estiloText = "-fx-font-size: 12pt;";

                Label infoLabel = new Label("Información");
                infoLabel.setStyle(estiloLabel);

                Label infoLabel1 = new Label("SMD");
                infoLabel1.setStyle(estiloLabel2);

                Text infoText1 = new Text(
                                "Versión: 4.0.0\nAutor: SMD (Marc Freixanet y Oriol Gracia)\nFecha: 2024-06-01\nLicencia: MIT");
                infoText1.setStyle(estiloText);

                vBox.getChildren().addAll(infoLabel, infoLabel1, infoText1);

                // Establecemos el contenido del diálogo
                alert.getDialogPane().setContent(vBox);

                // Mostramos el diálogo y esperamos a que el usuario lo cierre
                alert.showAndWait();
        }
}
