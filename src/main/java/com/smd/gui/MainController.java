package com.smd.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

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
    Label wordName;
    @FXML
    private TableView componentsTable;

    @FXML
    private TableColumn<Component, String> identifier;

    ObservableList<Component> initialData() {
        Component c1 = new Component("Identifier1");
        return FXCollections.<Component>observableArrayList(c1);
    }

    @FXML
    public void initialize() {
        identifier.setCellValueFactory(new PropertyValueFactory<Component, String>("identifier"));
        componentsTable.setItems(initialData());
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
        if (file != null) {
            openFile(file);
        }
    }

    private void openFile(File file) {
        wordName.setText(file.getName());
        int extensionIndex = file.getName().lastIndexOf('.');
        String fileExtension = file.getName().substring(extensionIndex);

        switch (fileExtension) {
            case ".txt":
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String fileContent = "";
                    ArrayList<Component> components = new ArrayList<Component>();
                    while (br.readLine() != null) {
                        Component c = extractComponent(br.readLine());
                        components.add(c);
                    }

                    componentsTable.setItems(getData(components));
                

                } catch (Exception e) {
                    wordName.setText("El archivo " + file.getName() + " no se ha podido abrir correctamente.");
                }

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

    ObservableList<Component> getData(ArrayList<Component> components) {
        return FXCollections.<Component>observableArrayList(components);
    }

    private Component extractComponent(String line) {
        if (line == null) {
            return null;
        }

        String[] parts = line.split("  ");
        String identifier = parts[0].trim();
        Component c = new Component(identifier);

        return c;

    }
}
