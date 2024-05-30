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
import com.smd.utils.CenterComponents;
import com.smd.utils.CsvFileReader;
import com.smd.utils.CsvWriter;
import com.smd.utils.TxtFileReader;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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

    private static File selectedFolder;

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

        String userHome = System.getProperty("user.home");
        String downloadsFolder = userHome + File.separator + "Downloads";
        selectedFolder = new File(downloadsFolder);

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
        FileChooser.ExtensionFilter txtFilter = new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt");
        FileChooser.ExtensionFilter csvFilter = new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().addAll(txtFilter, csvFilter);

        fileChooser.setInitialDirectory(selectedFolder);

        File file = fileChooser.showOpenDialog(primaryStage);

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

            default:
                break;
        }
    }

    @FXML
    private void setDefaultDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(selectedFolder);
        directoryChooser.setTitle("Selecciona el directorio por defecto");

        selectedFolder = directoryChooser.showDialog(primaryStage);

    }

    @FXML
    private void exportToAsq() {
        String fileName = getFileName();
        if (!fileName.equals("")) {
            AsqWriter.generate(components, selectedFolder.getAbsolutePath() + File.separator + fileName);
        }
    }

    @FXML
    private void exportToCsv() {
        String fileName = getFileName();
        if (!fileName.equals("")) {
            CsvWriter.generate(components, selectedFolder.getAbsolutePath() + File.separator + fileName);
        }
    }

    private String getFileName() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Información necesaria");
        dialog.setHeaderText("Nombre del archivo");
        dialog.setContentText("¿Cómo quieres que se llame el archivo?");

        DialogPane dialogPane = dialog.getDialogPane();
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);

        TextField textField = dialog.getEditor();

        okButton.setDisable(true);

        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(newValue.trim().isEmpty());
        });

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            return result.get();
        } else {
            return "";
        }
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
    private void centerOn() {
        CenterComponents.showDialog(componentsTable);
    }

    // TODO: Meterlo dentro de misma clase que CenterComponents y cambiarle el nombre a algo más global?
    public void flipBoard() {
        // Flip the board by changing the sign of the X position of all components
        for (Components component : components) {
            component.setPosX(-component.getPosX());
        }
        // Refresh the components table
        componentsTable.refresh();
        // Show notification message
        NotificationController.informationMsg("Proceso Completado", "La placa ha sido volteada.");
    }

    public void askForCancel() {
        // Create the dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Información necesaria");
        dialog.setHeaderText("¿Estás seguro de que quieres cancelar?");
        // Add OK and Cancel buttons to the dialog
        ButtonType okButtonType = new ButtonType("OK", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);
        // Show the dialog and wait for a response
        dialog.showAndWait().ifPresent(response -> {
            if (response == okButtonType) {
                // empty table
                components.clear();
                componentsTable.setItems(FXCollections.<Components>observableArrayList(components));
                // Show notification message
                NotificationController.informationMsg("Proceso Cancelado", "El proceso ha sido cancelado.");
            }
        });
    }

    public static void closeDb() {
        try {
            session.close();
            sessionFactory.close();
        } catch (Exception e) {
        }
    }

}
