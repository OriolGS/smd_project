package com.smd.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.smd.model.Component;

import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainController {

    private Stage primaryStage;
    @FXML
    private Label wordName;

    @FXML
    private TableView componentsTable;

    @FXML
    private TableColumn<Component, String> identifier;

    @FXML
    private TableColumn<Component, String> type;

    @FXML
    private TableColumn<Component, String> outline;

    @FXML
    private TableColumn<Component, Float> posX;

    @FXML
    private TableColumn<Component, Float> posY;

    @FXML
    private TableColumn<Component, Float> rotation;

    @FXML
    private TableColumn<Component, Boolean> flip;

    // ObservableList<Component> initialData() {
    // Component c1 = new Component();
    // return FXCollections.<Component>observableArrayList(c1);
    // }

    @FXML
    public void initialize() {
        identifier.setCellValueFactory(new PropertyValueFactory<Component, String>("identifier"));
        type.setCellValueFactory(new PropertyValueFactory<Component, String>("type"));
        outline.setCellValueFactory(new PropertyValueFactory<Component, String>("outline"));
        posX.setCellValueFactory(new PropertyValueFactory<Component, Float>("posX"));
        posY.setCellValueFactory(new PropertyValueFactory<Component, Float>("posY"));
        rotation.setCellValueFactory(new PropertyValueFactory<Component, Float>("rotation"));
        flip.setCellValueFactory(new PropertyValueFactory<Component, Boolean>("flip"));
        // componentsTable.setItems(initialData());
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
        this.wordName.setText("HOLA MUNDO");
    }

    @FXML
    private void handleOpen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        File file = fileChooser.showOpenDialog(primaryStage);
        if (!file.exists()) {
            wordName.setText("No se ha encontrado el archivo");
        } else if (file.length() == 0) {
            wordName.setText("El archivo está vacío");
        } else {
            openFile(file);
        }
    }

    private void openFile(File file) {
        wordName.setText(file.getName());

        int extensionIndex = file.getName().lastIndexOf('.');
        String fileExtension = file.getName().substring(extensionIndex);

        switch (fileExtension) {
            case ".txt":
                getTxtData(file);
                break;

            case ".csv":
                wordName.setText("Archivo del programa 2 o máquina 2: " + fileExtension);
                break;

            case ".asq":
                wordName.setText("Archivo de la máquina 1: " + fileExtension);
                break;

            default:
                break;
        }

    }

    private void getTxtData(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = "";
            ArrayList<Component> components = new ArrayList<Component>();

            // TODO: montar un codi més bonic
            line = br.readLine();
            if (line.trim().equals(".PARTS")) {
                while ((line = br.readLine()) != null && !line.trim().equals(".ENDPARTS")) {
                    components.add(extractComponent(line));
                }

                componentsTable.setItems(FXCollections.<Component>observableArrayList(components));

            } else {
                wordName.setText("Estructura de archivo inválida: se esperaba .PARTS");
            }

        } catch (IOException e) {
            wordName.setText("No se ha podido leer bien el archivo");
        } catch (SecurityException e) {
            wordName.setText("Problema de seguridad al acceder al archivo");
        }
    }

    private Component extractComponent(String line) {
        String[] component = new String[7];

        String[] lineArray = line.split("  ");
        component[0] = lineArray[0].trim();
        String newLine = line;

        for (int i = 1; i < 5; i++) {
            lineArray = newLine.substring(component[i - 1].length()).trim().split("  ");
            newLine = newLine.substring(component[i - 1].length()).trim();
            component[i] = lineArray[0].trim();
        }

        lineArray = newLine.substring(component[4].length()).trim().split(" ");
        Component c;
        if (lineArray.length == 1) {
            component[5] = lineArray[0].trim();
            c = new Component(component[0], 1, component[1], component[2], component[3], component[4],
                    component[5], false);
        } else {
            component[5] = lineArray[1].trim();
            c = new Component(component[0], 1, component[1], component[2], component[3], component[4],
                    component[5], true);
        }

        return c;
    }
}
