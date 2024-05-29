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
import com.smd.model.Component;
import com.smd.utils.AsqWriter;
import com.smd.utils.CsvFileReader;
import com.smd.utils.CsvWriter;
import com.smd.utils.TxtFileReader;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.BooleanStringConverter;
import javafx.util.converter.FloatStringConverter;

public class MainController {

    private Stage primaryStage;

    private static Configuration configuration;
    private static SessionFactory sessionFactory;
    private static Session session;

    public static ArrayList<Component> components = new ArrayList<>();
    public static List<Board> boards;

    @FXML
    private Label dbNameLabel;

    @FXML
    private Label wordNameLabel;

    @FXML
    private ComboBox<String> comboBoxBoards;

    @FXML
    private TableView<Component> componentsTable;

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
                    componentsTable.setItems(FXCollections.<Component>observableArrayList(components));
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
    private void setDefaultDirectory() {

    }

    @FXML
    private void exportToAsq() {
        AsqWriter.generate(components);
    }

    @FXML
    private void exportToCsv() {
        CsvWriter.generate(components);
    }

    @FXML
    private void printTable() {
        PrinterJob printerJob = PrinterJob.createPrinterJob();
        if (printerJob != null && printerJob.showPrintDialog(primaryStage)) {
            for (Component component : components) {
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

    public static void closeDb() {
        try {
            session.close();
            sessionFactory.close();
        } catch (Exception e) {
        }
    }

}
