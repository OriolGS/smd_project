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
    // Component c1 = new Component("Identifier1", "");
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
                    String line = "";
                    ArrayList<Component> components = new ArrayList<Component>();
                    // TODO: montar un codi més bonic
                    br.readLine();
                    while ((line = br.readLine()) != null) {
                        Component c = extractComponent(line);
                        components.add(c);
                    }
                    components.removeLast();

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
        String[] lineArray = line.split("  ");
        String identifier = lineArray[0].trim();

        // String rotation = lineArray[lineArray.length-1].trim();

        lineArray = line.substring(identifier.length()).trim().split("  ");
        String newLine = line.substring(identifier.length()).trim();
        String type = lineArray[0].trim();

        lineArray = newLine.substring(type.length()).trim().split("  ");
        newLine = newLine.substring(type.length()).trim();
        String outline = lineArray[0].trim();

        lineArray = newLine.substring(outline.length()).trim().split("  ");
        newLine = newLine.substring(outline.length()).trim();
        String posX = (lineArray[0].trim());

        lineArray = newLine.substring(posX.length()).trim().split("  ");
        newLine = newLine.substring(posX.length()).trim();
        String posY = (lineArray[0].trim());

        lineArray = newLine.substring(posX.length()).trim().split(" ");
        String rotation = "";
        Boolean flip;
        if (lineArray.length == 1) {
            rotation = lineArray[0].trim();
            flip = false;
        } else {
            rotation = lineArray[1].trim();
            flip = true;
        }

        // String[] posXLine = line.substring(beginIndex).trim().split(" ");
        // beginIndex += posXLine[0].length();
        // Float posX = Float.parseFloat(posXLine[0].trim());

        // String[] posYLine = line.substring(beginIndex).trim().split(" ");
        // beginIndex += posYLine[0].length();
        // Float posY = Float.parseFloat(posYLine[0].trim());

        // String[] rotationLine = line.substring(beginIndex).trim().split(" ");
        // beginIndex += rotationLine[0].length();
        // Float rotation = Float.parseFloat(rotationLine[0].trim());

        // String[] flipLine = line.substring(beginIndex).trim().split(" ");
        // beginIndex += flipLine[0].length();
        // String flip = flipLine[0].trim();

        Component c = new Component(identifier, 1, type, outline, posX, posY, rotation, flip);

        return c;
    }
}
