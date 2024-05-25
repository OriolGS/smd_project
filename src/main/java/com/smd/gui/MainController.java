package com.smd.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.smd.model.Component;
import com.smd.utils.CsvFileReader;
import com.smd.utils.TxtFileReader;

import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.converter.StringConverter;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.*;

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

    public static ArrayList<Component> components = new ArrayList<>();

    // ObservableList<Component> initialData() {
    // Component c1 = new Component();
    // return FXCollections.<Component>observableArrayList(c1);
    // }

    @FXML
    public void initialize() {
        this.wordName.setText("HOLA MUNDO");
        identifier.setCellValueFactory(new PropertyValueFactory<>("identifier"));
        type.setCellValueFactory(new PropertyValueFactory<>("type"));
        outline.setCellValueFactory(new PropertyValueFactory<>("outline"));
        posX.setCellValueFactory(new PropertyValueFactory<>("posX"));
        posY.setCellValueFactory(new PropertyValueFactory<>("posY"));
        rotation.setCellValueFactory(new PropertyValueFactory<>("rotation"));
        flip.setCellValueFactory(new PropertyValueFactory<>("flip"));

        // Hacer las columnas editables
        identifier.setCellFactory(TextFieldTableCell.forTableColumn());
        // ...
        identifier.setCellFactory(TextFieldTableCell.forTableColumn());
        type.setCellFactory(TextFieldTableCell.forTableColumn());
        outline.setCellFactory(TextFieldTableCell.forTableColumn());
        posX.setCellFactory(TextFieldTableCell.forTableColumn(new FloatStringConverter()));
        posY.setCellFactory(TextFieldTableCell.forTableColumn(new FloatStringConverter()));
        rotation.setCellFactory(TextFieldTableCell.forTableColumn(new FloatStringConverter()));
        flip.setCellFactory(TextFieldTableCell.forTableColumn(new BooleanStringConverter()));

        // componentsTable.setItems(initialData());
    }

    // public void setPrimaryStage(Stage stage) {
    // this.primaryStage = stage;
    // this.wordName.setText("HOLA MUNDO");
    // }

    @FXML
    private void handleOpen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file == null || !file.exists()) {
            wordName.setText("No se ha encontrado el archivo");
        } else if (file.length() == 0) {
            wordName.setText("El archivo está vacío");
        } else {
            openFile(file);
        }
    }

    @FXML
    private void printTable() {
        PrinterJob printerJob = PrinterJob.createPrinterJob();
        if (printerJob != null && printerJob.showPrintDialog(primaryStage)) {
            for (Component component : components) {
                printerJob.printPage(component.getNode());
            }
            printerJob.endJob();
        }
    }

    private void openFile(File file) {
        wordName.setText(file.getName());

        int extensionIndex = file.getName().lastIndexOf('.');
        String fileExtension = file.getName().substring(extensionIndex);

        switch (fileExtension) {
            case ".txt":
                TxtFileReader.read(file, wordName, componentsTable);
                break;

            case ".csv":
                CsvFileReader.read(file, wordName, componentsTable);
                break;

            case ".asq":
                wordName.setText("Archivo de la máquina 1: " + fileExtension);
                break;

            default:
                break;
        }
    }

}
