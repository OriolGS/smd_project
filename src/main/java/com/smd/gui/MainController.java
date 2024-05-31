package com.smd.gui;

import javafx.scene.layout.GridPane;
import javafx.util.Pair;

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
import com.smd.controller.EmptyTableController;
import com.smd.controller.NotificationController;
import com.smd.model.Board;
import com.smd.model.Components;
import com.smd.utils.AsqWriter;
import com.smd.utils.ModifyComponents;
import com.smd.utils.CsvFileReader;
import com.smd.utils.CsvWriter;
import com.smd.utils.TxtFileReader;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.BooleanStringConverter;
import javafx.util.converter.FloatStringConverter;

public class MainController {

    private Stage primaryStage;

    private static Thread statesThread;
    private static EmptyTableController stateController;

    private static Configuration configuration;
    private static SessionFactory sessionFactory;
    private static Session session;

    public static ArrayList<Components> components = new ArrayList<>();
    public static List<Components> originalComponents = new ArrayList<>();
    public static List<Board> boards;

    private static File selectedFolder;

    public static boolean isModifying = false;

    private ModifyComponents modifyComponents;

    @FXML
    private MenuItem exportToAsq, exportToCsv, printBoard, reloadDb, centerComponent, flipBoard;

    @FXML
    private Label dbName;

    @FXML
    private ComboBox<String> comboBoxBoards;

    @FXML
    private Button loadBoard, cancelButton, saveButton;

    @FXML
    private TableView<Components> componentsTable;

    @FXML
    private TableColumn<Components, String> identifier, type, outline;

    @FXML
    private TableColumn<Components, Float> posX, posY, rotation;

    @FXML
    private TableColumn<Components, Boolean> flip;

    @FXML
    public void initialize() {
        identifier.setCellValueFactory(new PropertyValueFactory<>("identifier"));
        type.setCellValueFactory(new PropertyValueFactory<>("type"));
        outline.setCellValueFactory(new PropertyValueFactory<>("outline"));
        posX.setCellValueFactory(new PropertyValueFactory<>("posX"));
        posY.setCellValueFactory(new PropertyValueFactory<>("posY"));
        rotation.setCellValueFactory(new PropertyValueFactory<>("rotation"));
        flip.setCellValueFactory(new PropertyValueFactory<>("flip"));

        // Hace las columnas editables
        identifier.setCellFactory(TextFieldTableCell.forTableColumn());
        identifier.setOnEditCommit(event -> onCellEdit(event));
        type.setCellFactory(TextFieldTableCell.forTableColumn());
        type.setOnEditCommit(event -> onCellEdit(event));
        outline.setCellFactory(TextFieldTableCell.forTableColumn());
        outline.setOnEditCommit(event -> onCellEdit(event));
        posX.setCellFactory(TextFieldTableCell.forTableColumn(new FloatStringConverter()));
        posX.setOnEditCommit(event -> onCellEdit(event));
        posY.setCellFactory(TextFieldTableCell.forTableColumn(new FloatStringConverter()));
        posY.setOnEditCommit(event -> onCellEdit(event));
        rotation.setCellFactory(TextFieldTableCell.forTableColumn(new FloatStringConverter()));
        rotation.setOnEditCommit(event -> onCellEdit(event));
        flip.setCellFactory(TextFieldTableCell.forTableColumn(new BooleanStringConverter()));
        flip.setOnEditCommit(event -> onCellEdit(event));

        String userHome = System.getProperty("user.home");
        String downloadsFolder = userHome + File.separator + "Downloads";
        selectedFolder = new File(downloadsFolder);

        try {
            startDb();
            getBoardsFromDb();
        } catch (Exception e) {
            dbName.setText("No se pudo conectar a la base de datos.");
            reloadDb.setDisable(true);
            comboBoxBoards.setDisable(true);
            loadBoard.setDisable(true);
        }

        saveButton.setDisable(true);
        cancelButton.setDisable(true);
        modifyComponents = new ModifyComponents(cancelButton, saveButton);

        stateController = new EmptyTableController(componentsTable, exportToAsq, exportToCsv,
                printBoard, centerComponent, flipBoard);
        statesThread = new Thread(stateController);

        statesThread.start();

    }

    private void onCellEdit(TableColumn.CellEditEvent<Components, ?> event) {
        // Enable the save button when a cell is edited
        isModifying = true;
        saveButton.setText("Modify");
        saveButton.setDisable(false);
        cancelButton.setDisable(false);
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
            dbName.setText("No se ha encontrado la base de datos");
        }
    }

    @FXML
    private void reloadDb() {
        // components.clear();
        // boards.clear();
        try {
            session = sessionFactory.openSession();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Board> criteriaQuery = builder.createQuery(Board.class);
            Root<Board> root = criteriaQuery.from(Board.class);
            criteriaQuery.select(root);

            boards.clear();
            boards = session.createQuery(criteriaQuery).getResultList();

            componentsTable.getItems().clear();
            components.clear();
            originalComponents.clear();

            NotificationController.informationMsg("Base de datos recargada", "Selecciona la placa que quieres ver.");
        } catch (Exception e) {
            NotificationController.errorMsg("Error", "No se han podido cargar los componentes.");
        }
    }

    @FXML
    private void loadInfoFromDb() {
        try {
            session = sessionFactory.openSession();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Board> criteriaQuery = builder.createQuery(Board.class);
            Root<Board> root = criteriaQuery.from(Board.class);
            criteriaQuery.select(root);

            boards.clear();
            boards = session.createQuery(criteriaQuery).getResultList();
            String boardName = comboBoxBoards.getSelectionModel().isEmpty() ? "" : comboBoxBoards.getValue();

            if (boardName != "") {
                components.clear();
                for (Board board : boards) {
                    if (boardName.equals(board.getBoardName())) {
                        components.addAll(board.getComponents());
                    }
                }

                if (components.size() != 0) {
                    componentsTable.setItems(FXCollections.<Components>observableArrayList(components));
                    originalComponents = new ArrayList<>();
                    for (Components component : MainController.components) {
                        originalComponents.add(new Components(component));
                    }
                } else {
                    NotificationController.warningMsg("Componentes no encontrados",
                            "No hemos podido extraer los componentes. Comprueba que la placa tiene componentes.");
                    // Sincroniza de nuevo la tabla con la lista de componentes
                    ObservableList<Components> observableComponents = componentsTable.getItems();
                    ArrayList<Components> componentsList = new ArrayList<>(observableComponents);
                    components = componentsList;
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
        int extensionIndex = file.getName().lastIndexOf('.');
        String fileExtension = file.getName().substring(extensionIndex);

        switch (fileExtension) {
            case ".txt":
                TxtFileReader.read(file, componentsTable, cancelButton, saveButton);
                break;

            case ".csv":
                CsvFileReader.read(file, componentsTable, cancelButton, saveButton);
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
            NotificationController.informationMsg("Proceso finalizado",
                    "El contenido de la tabla ha sido imprimido.");
        }
    }

    @FXML
    private void askBoardName() {
        Board board = components.get(0).getBoardFK();
        if (!isModifying) {
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
        } else {
            if (board != null) {
                saveToDb(board);
            } else {
                NotificationController.errorMsg("Error al actualizar",
                        "No hemos encontrado la placa en la base de datos.");
            }
        }
    }

    private void saveToDb(Board board) {
        Transaction transaction = null;

        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            if (isModifying) {
                ObservableList<Components> observableComponents = componentsTable.getItems();
                ArrayList<Components> componentsList = new ArrayList<>(observableComponents);
                board.setComponents(componentsList);

                session.update(board);

                NotificationController.informationMsg("Proceso finalizado",
                        "La placa " + board.getBoardName() + " y sus componentes se han actualizado correctamente.");

            } else {
                session.save(board);

                NotificationController.informationMsg("Proceso finalizado",
                        "La placa " + board.getBoardName() + " y sus componentes se han guardado correctamente.");
            }

            transaction.commit();

            saveButton.setDisable(true);
            cancelButton.setDisable(true);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            NotificationController.errorMsg("Error al guardar",
                    "Lo sentimos, no se ha podido guardar los datos en la base de datos.");
        }

        getBoardsFromDb();
    }

    @FXML
    private void centerOn() {
        originalComponents = new ArrayList<>();
        for (Components component : MainController.components) {
            originalComponents.add(new Components(component));
        }

        modifyComponents.centerComponents(componentsTable);
    }

    @FXML
    private void flipBoard() {
        originalComponents = new ArrayList<>();
        for (Components component : MainController.components) {
            originalComponents.add(new Components(component));
        }
        modifyComponents.flipBoard(componentsTable);
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
                if (isModifying) {
                    componentsTable.getItems().clear();
                    components = new ArrayList<>(originalComponents);
                    componentsTable.setItems(FXCollections.<Components>observableArrayList(components));

                } else {
                    // empty table
                    components.clear();
                    componentsTable.getItems().clear();
                }

                // Show notification message
                NotificationController.informationMsg("Proceso Cancelado", "El proceso ha sido cancelado.");
                saveButton.setDisable(true);
                cancelButton.setDisable(true);
            }
        });
    }

    public void dbConfig(){
        Dialog<Pair<String, Pair<String, String>>> dialog = new Dialog<>();
        dialog.setTitle("Información necesaria");
        dialog.setHeaderText("Introduce los credenciales de la base de datos");
        // Botones de confirmación
        ButtonType confirmButtonType = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        // Campos de texto para URL, usuario y contraseña
        TextField urlField = new TextField();
        urlField.setPromptText("URL");

        TextField userField = new TextField();
        userField.setPromptText("Usuario");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Contraseña");

        GridPane grid = new GridPane();
        grid.add(new Label("URL:"), 0, 0);
        grid.add(urlField, 1, 0);
        grid.add(new Label("Usuario:"), 0, 1);
        grid.add(userField, 1, 1);
        grid.add(new Label("Contraseña:"), 0, 2);
        grid.add(passwordField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Convertir el resultado en un Pair<String, Pair<String, String>> cuando se haga clic en Confirmar
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return new Pair<>(urlField.getText(), new Pair<>(userField.getText(), passwordField.getText()));
            }
            return null;
        });

        // Mostrar el diálogo y obtener el resultado
        Optional<Pair<String, Pair<String, String>>> result = dialog.showAndWait();
        // Si se proporcionan credenciales, intentar establecer la conexión
        if (result.isPresent()) {
            String url = result.get().getKey();
            String username = result.get().getValue().getKey();
            String password = result.get().getValue().getValue();

            // Extract the database name from the URL
            String dbNameString = parseDatabaseNameFromUrl(url);

            Configuration config = new Configuration().configure();
            config.setProperty("hibernate.connection.url", url);
            config.setProperty("hibernate.connection.username", username);
            config.setProperty("hibernate.connection.password", password);

            try {
                sessionFactory = config.buildSessionFactory();
                session = sessionFactory.openSession();
                getBoardsFromDb();
                dbName.setText("Nombre de la base de datos: " + dbNameString);
                // Mostrar mensaje de éxito
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Conexión exitosa");
                successAlert.setHeaderText("Conexión establecida correctamente");
                successAlert.showAndWait();

            } catch (Exception e) {
                e.printStackTrace();
                // Mostrar mensaje de error
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error de conexión");
                errorAlert.setHeaderText("No se pudo conectar a la base de datos");
                errorAlert.setContentText("Revise los credenciales de conexión y vuelva a intentarlo.");
                errorAlert.showAndWait();
            }
        }
    }

    private String parseDatabaseNameFromUrl(String dbUrl) {
        // Assuming the URL is of the format "jdbc:mysql://host:port/dbname"
        String[] parts = dbUrl.split("/");
        return parts.length > 3 ? parts[parts.length - 1] : "No se ha encontrado la base de datos";
    }

    public static void closeDb() {
        try {
            session.close();
            sessionFactory.close();
        } catch (Exception e) {
        }

        if (statesThread.isAlive()) {
            statesThread.interrupt();
        }
    }

}
