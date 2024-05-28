package com.smd.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.controlsfx.control.Notifications;
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

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

    @FXML
    public void initialize() {
        this.wordName.setText("Bienvenido");

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
            startDataBase();
            loadInfoFromDataBase();
        } catch (Exception e) {
            // TODO: mirar cómo gestionar el error, no se puede mostrar con alerta ni
            // notificación porque aún no existe la pantalla
            wordName.setText("No se pudo conectar a la base de datos.");
            e.printStackTrace();
        }
    }

    // TODO: plantearme si hacer una clase para controlar conexiones con bbdd
    private void startDataBase() throws Exception {
        try {
            configuration = new Configuration().configure();
            sessionFactory = configuration.buildSessionFactory();
            session = sessionFactory.openSession();
        } catch (Exception e) {
        }
    }

    @FXML
    private void loadInfoFromDataBase() {
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Board> criteriaQuery = builder.createQuery(Board.class);
            Root<Board> root = criteriaQuery.from(Board.class);
            criteriaQuery.select(root);

            List<Board> boards = session.createQuery(criteriaQuery).getResultList();
            components.clear();
            for (Board board : boards) {
                for (Component component : board.getComponents()) {
                    components.add(component);
                }
            }

            componentsTable.setItems(FXCollections.<Component>observableArrayList(MainController.components));

        } catch (Exception e) {
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
                // TODO: se necesita poder abrir este tipo?
                wordName.setText("Archivo de la máquina 1: " + fileExtension);
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

    @FXML
    private void saveToDb() {
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            Board board = components.get(0).getBoardFK();
            transaction = session.beginTransaction();
            session.save(board);
            transaction.commit();

            NotificationController.informationMsg("Proceso finalizado",
                    "La placa " + board.getBoardName() + " y sus componentes se ha guardado correctamente.");
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            NotificationController.errorMsg("Error al guardar", "Lo sentimos, no se ha podido guardar los datos en la base de datos");
        }
    }

    public static void closeDataBase() {
        try {
            session.close();
            sessionFactory.close();
        } catch (Exception e) {
        }
    }

}
