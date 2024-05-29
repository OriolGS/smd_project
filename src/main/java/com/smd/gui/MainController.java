package com.smd.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import com.smd.controller.NotificationController;
import com.smd.model.Board;
import com.smd.model.Components;
import com.smd.utils.AsqWriter;
import com.smd.utils.CsvFileReader;
import com.smd.utils.CsvWriter;
import com.smd.utils.TxtFileReader;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.BooleanStringConverter;
import javafx.util.converter.FloatStringConverter;

public class MainController {

    private Stage primaryStage;

    private static Configuration configuration;
    private static SessionFactory sessionFactory;
    private static Session session;

    public static ArrayList<Components> components = new ArrayList<>();
    public static List<Board> boards;

    public String selectedFolderPath = "";

    @FXML
    private Label dbNameLabel;

    @FXML
    private Label wordNameLabel;

    @FXML
    private ComboBox<String> comboBoxBoards;

    @FXML
    private TableView<Components> componentsTable;

    @FXML
    private TableColumn<Components, String> identifier;

    @FXML
    private TableColumn<Components, String> type;

    @FXML
    private TableColumn<Components, String> outline;

    @FXML
    private TableColumn<Components, Float> posX;

    @FXML
    private TableColumn<Components, Float> posY;

    @FXML
    private TableColumn<Components, Float> rotation;

    @FXML
    private TableColumn<Components, Boolean> flip;

    @FXML
    public void initialize() {
        this.wordNameLabel.setText("Bienvenido");

        identifier.setCellValueFactory(new PropertyValueFactory<>("identifier"));
        type.setCellValueFactory(new PropertyValueFactory<>("type"));
        outline.setCellValueFactory(new PropertyValueFactory<>("outline"));
        posX.setCellValueFactory(new PropertyValueFactory<>("posX"));
        posY.setCellValueFactory(new PropertyValueFactory<>("posY"));
        rotation.setCellValueFactory(new PropertyValueFactory<>("rotation"));
        flip.setCellValueFactory(new PropertyValueFactory<>("flip"));

        // Hace las columnas editables
        identifier.setCellFactory(TextFieldTableCell.forTableColumn());
        type.setCellFactory(TextFieldTableCell.forTableColumn());
        outline.setCellFactory(TextFieldTableCell.forTableColumn());
        posX.setCellFactory(TextFieldTableCell.forTableColumn(new FloatStringConverter()));
        posY.setCellFactory(TextFieldTableCell.forTableColumn(new FloatStringConverter()));
        rotation.setCellFactory(TextFieldTableCell.forTableColumn(new FloatStringConverter()));
        flip.setCellFactory(TextFieldTableCell.forTableColumn(new BooleanStringConverter()));
        try {
            startDb();
            getBoardsFromDb();
        } catch (Exception e) {
            // TODO: mirar cómo gestionar el error, no se puede mostrar con alerta ni
            // notificación porque aún no existe la pantalla
            wordNameLabel.setText("No se pudo conectar a la base de datos.");
            e.printStackTrace();
        }
    }

    // TODO: plantearme si hacer una clase para controlar conexiones con bbdd
    private void startDb() throws Exception {
        try {
            configuration = new Configuration().configure();
            sessionFactory = configuration.buildSessionFactory();
            session = sessionFactory.openSession();
        } catch (Exception e) {
        }
    }

    private void getBoardsFromDb() {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Board> criteriaQuery = builder.createQuery(Board.class);
        Root<Board> root = criteriaQuery.from(Board.class);
        criteriaQuery.select(root);

        boards = session.createQuery(criteriaQuery).getResultList();

        comboBoxBoards.getItems().clear();

        if (boards != null) {
            for (Board board : boards) {
                comboBoxBoards.getItems().add(board.getBoardName());
            }
        } else {
            dbNameLabel.setText("No se ha encontrado la base de datos");
        }
    }

    @FXML
    private void loadInfoFromDb() {
        components.clear();
        boards.clear();
        try {
            session = sessionFactory.openSession();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Board> criteriaQuery = builder.createQuery(Board.class);
            Root<Board> root = criteriaQuery.from(Board.class);
            criteriaQuery.select(root);

            boards = session.createQuery(criteriaQuery).getResultList();

            String boardName = comboBoxBoards.getSelectionModel().isEmpty() ? "" : comboBoxBoards.getValue();

            if (boardName != "") {
                for (Board board : boards) {
                    if (boardName.equals(board.getBoardName())) {
                        components.addAll(board.getComponents());
                    }
                }

                if (components.size() != 0) {
                    componentsTable.setItems(FXCollections.<Components>observableArrayList(components));
                } else {
                    NotificationController.warningMsg("Componentes no encontrados",
                            "No hemos podido extraer los componentes. Comprueba que la placa tiene componentes.");
                }
            } else {
                NotificationController.informationMsg("Escoge una placa",
                        "No podemos cargar los componentes si no tienes una placa seleccionada.");
            }

        } catch (Exception e) {
            NotificationController.errorMsg("Error", "No se han podido cargar los componentes.");
        }
    }

    @FXML
    private void handleOpenFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        if (selectedFolderPath != "") {
            fileChooser.setInitialDirectory(new File(selectedFolderPath));
        } else {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }
        File file = fileChooser.showOpenDialog(primaryStage);
        // TODO: controlar el tipo de archivos que puede abrir

        if (file == null || !file.exists()) {
        } else if (file.length() == 0) {
            NotificationController.warningMsg("Problema con archivo", "El archivo seleccionado está vacío.");
        } else {
            openFile(file);
        }
    }

    private void openFile(File file) {
        wordNameLabel.setText(file.getName());

        int extensionIndex = file.getName().lastIndexOf('.');
        String fileExtension = file.getName().substring(extensionIndex);

        switch (fileExtension) {
            case ".txt":
                TxtFileReader.read(file, wordNameLabel, componentsTable);
                break;

            case ".csv":
                CsvFileReader.read(file, wordNameLabel, componentsTable);
                break;

            case ".asq":
                // TODO: se necesita poder abrir este tipo?
                wordNameLabel.setText("Archivo de la máquina 1: " + fileExtension);
                break;

            default:
                break;
        }
    }

    @FXML
    private String setDefaultDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Selecciona la carpeta por defecto");
        File selectedDirectory = directoryChooser.showDialog(primaryStage);
        if (selectedDirectory != null) {
            selectedFolderPath = selectedDirectory.getAbsolutePath();
            NotificationController.informationMsg("Proceso finalizado", "La carpeta por defecto ha sido seleccionada correctamente.");
            selectedFolderPath = selectedFolderPath + "\\";
            return selectedFolderPath;
        } else {
            NotificationController.warningMsg("Proceso cancelado", "No se ha seleccionado ninguna carpeta.");
            return null;
        }
    }

    @FXML
    private void exportToAsq() {
        AsqWriter.generate(components, selectedFolderPath);
    }

    @FXML
    private void exportToCsv() {
        CsvWriter.generate(components, selectedFolderPath);
    }

    @FXML
    private void printTable() {
        PrinterJob printerJob = PrinterJob.createPrinterJob();
        if (printerJob != null && printerJob.showPrintDialog(primaryStage)) {
            for (Components component : components) {
                printerJob.printPage(component.getNode());
            }
            printerJob.endJob();
            NotificationController.informationMsg("Proceso finalizado", "El contenido de la tabla ha sido imprimido.");
        }
    }

    private void saveToDb(Board board) {
        Transaction transaction = null;

        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            session.save(board);
            transaction.commit();

            NotificationController.informationMsg("Proceso finalizado",
                    "La placa " + board.getBoardName() + " y sus componentes se ha guardado correctamente.");
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            NotificationController.errorMsg("Error al guardar",
                    "Lo sentimos, no se ha podido guardar los datos en la base de datos");
        }

        getBoardsFromDb();
    }

    @FXML
    private void askBoardName() {
        Board board = components.get(0).getBoardFK();
        TextInputDialog dialog = new TextInputDialog(board.getBoardName());
        dialog.setTitle("Información necesaria");
        dialog.setHeaderText("Nombre de la placa");
        dialog.setContentText("Introduce el nombre de la placa:");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            boolean existingName = false;
            ObservableList<String> boardList = comboBoxBoards.getItems();
            for (String boardName : boardList) {
                if (result.get().equals(boardName)) {
                    existingName = true;
                }
            }
            if (!existingName) {
                board.setBoardName(result.get());
                saveToDb(board);
            } else {
                NotificationController.warningMsg("Nombre no válido",
                        "La placa ya existe! Prueba a guardarla con otro nombre.");
            }

        }
    }

    @FXML
    private void CenterOn() {
        // Create the dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Información necesaria");
        dialog.setHeaderText("¿Respecto a qué quieres centrar la placa?");
        
        // Create the radio buttons and toggle group
        RadioButton rbComponent = new RadioButton("Centrar respecto a un componente");
        RadioButton rbPosition = new RadioButton("Centrar respecto a una posición");
        ToggleGroup group = new ToggleGroup();
        rbComponent.setToggleGroup(group);
        rbPosition.setToggleGroup(group);
        
        // Add radio buttons to a VBox
        VBox vbox = new VBox(rbComponent, rbPosition);
        dialog.getDialogPane().setContent(vbox);
        
        // Add OK and Cancel buttons to the dialog
        ButtonType okButtonType = new ButtonType("OK", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // Show the dialog and wait for a response
        dialog.showAndWait().ifPresent(response -> {
            if (response == okButtonType) {
                if (rbComponent.isSelected()) {
                    //Dialog for selecting a component
                    Dialog<ButtonType> dialogComponent = new Dialog<>();
                    dialogComponent.setTitle("Información necesaria");
                    dialogComponent.setHeaderText("Selecciona un componente para centrar la placa");
                    // Create the combo box
                    ComboBox<String> comboBoxComponents = new ComboBox<>();
                    for (Components component : components) {
                        comboBoxComponents.getItems().add(component.getIdentifier());
                    }
                    // Add the combo box to the dialog
                    dialogComponent.getDialogPane().setContent(comboBoxComponents);
                    // Add OK and Cancel buttons to the dialog
                    ButtonType okButtonTypeComponent = new ButtonType("OK", ButtonData.OK_DONE);
                    dialogComponent.getDialogPane().getButtonTypes().addAll(okButtonTypeComponent, ButtonType.CANCEL);
                    // Show the dialog and wait for a response
                    dialogComponent.showAndWait().ifPresent(responseComponent -> {
                        if (responseComponent == okButtonTypeComponent) {
                            String selectedComponentId = comboBoxComponents.getValue();
                            if (selectedComponentId != null) {
                                Components selectedComponent = null;
                                for (Components component : components) {
                                    if (component.getIdentifier().equals(selectedComponentId)) {
                                        selectedComponent = component;
                                        break;
                                    }
                                }
                                if (selectedComponent != null) {
                                    // Save the previous position of the selected component
                                    float oldPosX = selectedComponent.getPosX();
                                    float oldPosY = selectedComponent.getPosY();
                                    // Move the selected component to the center of the board (0,0)
                                    selectedComponent.setPosX(0.0f);
                                    selectedComponent.setPosY(0.0f);
                                    // Adjust the positions of the other components relative to the centered component
                                    for (Components otherComponent : components) {
                                        if (!otherComponent.getIdentifier().equals(selectedComponentId)) {
                                            otherComponent.setPosX(otherComponent.getPosX() - oldPosX);
                                            otherComponent.setPosY(otherComponent.getPosY() - oldPosY);
                                        }
                                    }
                                    // Refresh the components table
                                    componentsTable.refresh();
                                    // Show notification message
                                    NotificationController.informationMsg("Proceso Completado", "El componente " + selectedComponentId + " ha sido centrado en la placa.");
                                }
                            }
                        }
                    });
                } else if (rbPosition.isSelected()) {
                    //Dialog for selecting a position
                    Dialog<ButtonType> dialogPosition = new Dialog<>();
                    dialogPosition.setTitle("Información necesaria");
                    dialogPosition.setHeaderText("Introduce la posición a la que quieres centrar la placa");
                    // Create the text input fields
                    TextInputDialog dialog2 = new TextInputDialog();
                    dialog2.setTitle("Información necesaria");
                    dialog2.setHeaderText("Introduce la posición a la que quieres centrar la placa");
                    dialog2.setContentText("Introduce la posición X:");
                    Optional<String> resultX = dialog2.showAndWait();
                    dialog2.setContentText("Introduce la posición Y:");
                    Optional<String> resultY = dialog2.showAndWait();
                    if (resultX.isPresent() && resultY.isPresent()) {
                        try {
                            float posX = Float.parseFloat(resultX.get());
                            float posY = Float.parseFloat(resultY.get());
                            // Save the previous position of the selected component
                            float oldPosX = posX;
                            float oldPosY = posY;
                            // Move the selected component to the center of the board (0,0)
                            for (Components component : components) {
                                component.setPosX(component.getPosX() - oldPosX);
                                component.setPosY(component.getPosY() - oldPosY);
                            }
                            // Refresh the components table
                            componentsTable.refresh();
                            // Show notification message
                            NotificationController.informationMsg("Proceso Completado", "La placa ha sido centrada en la posición (" + posX + ", " + posY + ").");
                        } catch (NumberFormatException e) {
                            NotificationController.errorMsg("Error", "Las posiciones deben ser números.");
                        }
                    }
                }
            }
        });
    }

    public void FlipBoard() {
        // Flip the board by changing the sign of the X position of all components
        for (Components component : components) {
            component.setPosX(-component.getPosX());
        }
        // Refresh the components table
        componentsTable.refresh();
        // Show notification message
        NotificationController.informationMsg("Proceso Completado", "La placa ha sido volteada.");
    }

    public static void closeDb() {
        try {
            session.close();
            sessionFactory.close();
        } catch (Exception e) {
        }
    }

}
