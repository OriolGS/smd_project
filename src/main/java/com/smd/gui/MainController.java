package com.smd.gui;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
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
import com.smd.model.ProgramType;
import com.smd.utils.AsqWriter;
import com.smd.utils.ModifyComponents;
import com.smd.utils.CsvFileReader;
import com.smd.utils.CsvWriter;
import com.smd.utils.Help;
import com.smd.utils.TxtFileReader;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.print.PrinterJob;
import javafx.scene.control.ButtonBar.ButtonData;
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

    public static boolean isModifying = false, dbConnected;

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
        // Inicializa las columnas de la tabla
        identifier.setCellValueFactory(new PropertyValueFactory<>("identifier"));
        type.setCellValueFactory(new PropertyValueFactory<>("type"));
        outline.setCellValueFactory(new PropertyValueFactory<>("outline"));
        posX.setCellValueFactory(new PropertyValueFactory<>("posX"));
        posY.setCellValueFactory(new PropertyValueFactory<>("posY"));
        rotation.setCellValueFactory(new PropertyValueFactory<>("rotation"));
        flip.setCellValueFactory(new PropertyValueFactory<>("flip"));

        // Hace las columnas editables y crea sus EventListeners
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

        // Crea el directorio predeterminado
        String userHome = System.getProperty("user.home");
        String downloadsFolder = userHome + File.separator + "Downloads";
        selectedFolder = new File(downloadsFolder);

        // Intenta establecer conexión con la base de datos
        try {
            startDb();
            getBoardsFromDb();
            dbConnected = true;
        } catch (Exception e) {
            dbConnected = false;
            dbName.setText("No se pudo conectar a la base de datos.");
            reloadDb.setDisable(true);
            comboBoxBoards.setDisable(true);
            loadBoard.setDisable(true);
        }

        // Desactica los botones de guardar y cancelar
        saveButton.setDisable(true);
        cancelButton.setDisable(true);

        modifyComponents = new ModifyComponents(cancelButton, saveButton);

        // Inicializa el thread que comprueba si la tabla está vacía o no
        stateController = new EmptyTableController(componentsTable, exportToAsq, exportToCsv,
                printBoard, centerComponent, flipBoard);
        statesThread = new Thread(stateController);
        statesThread.start();
    }

    /**
     * Recibe el @param event de la tabla para hacer la persitencia de modificación
     * del componente y activa los botones de cancelar y guardar (en este caso
     * modificar)
     */
    private void onCellEdit(TableColumn.CellEditEvent<Components, ?> event) {
        Components component = event.getRowValue();
        String columnName = event.getTableColumn().getText();
        Object newValue = event.getNewValue();

        switch (columnName) {
            case "Identifier":
                component.setIdentifier((String) newValue);
                break;
            case "Type":
                component.setType((String) newValue);
                break;
            case "Outline":
                component.setOutline((String) newValue);
                break;
            case "PosX":
                component.setPosX((Float) newValue);
                break;
            case "PosY":
                component.setPosY((Float) newValue);
                break;
            case "Rotation":
                component.setRotation((Float) newValue);
                break;
            case "Flip":
                component.setFlip((Boolean) newValue);
                break;
        }

        isModifying = true;
        saveButton.setText("Modify");
        if (dbConnected) {
            saveButton.setDisable(false);
        }
        cancelButton.setDisable(false);
    }

    private void startDb() throws Exception {
        configuration = new Configuration().configure();
        sessionFactory = configuration.buildSessionFactory();
        session = sessionFactory.openSession();
        String dbNameString = parseDatabaseNameFromUrl(configuration.getProperty("hibernate.connection.url"));
        dbName.setText("Nombre de la base de datos: " + dbNameString);
    }

    /**
     * Rellena el comboBoxBoards con las placas que hay almacenadas en la base de
     * datos
     */
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
            dbName.setText("No se han encontrado placas en la base de datos");
        }
    }

    @FXML
    private void reloadDb() {
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

    /**
     * Carga los componentes que hay en la base de datos de la placa que hay
     * seleccionada en el comboBoxBoards
     */
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
            System.out.println(e.getMessage());
            NotificationController.errorMsg("Error", "No se han podido cargar los componentes.");
        }
    }

    /**
     * Abre un fileChooser con los filtros csv y txt para que sólo pueda abrir
     * archivos con estas extensiones.
     * Si el usuario ha seleccionado dos archivos, lo manda a checkFilesList, si
     * solo selecciona uno, lo manda al método que lo abre.
     */
    @FXML
    private void handleOpenFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        FileChooser.ExtensionFilter txtFilter = new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt");
        FileChooser.ExtensionFilter csvFilter = new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().addAll(txtFilter, csvFilter);

        fileChooser.setInitialDirectory(selectedFolder);

        List<File> files = fileChooser.showOpenMultipleDialog(primaryStage);

        if (files == null || files.isEmpty()) {
            // No ha seleccionado nada
        } else if (files.size() > 2) {
            NotificationController.warningMsg("Selección de archivos",
                    "Por favor, seleccione un máximo de dos archivos.");
        } else if (files.size() == 2) {
            checkFilesList(files);
        } else {
            openFile(files.get(0));
        }
    }

    /**
     * Recibe una lista con los archivos seleccionados por el usuario y comprueba
     * que ambos se puedan abrir y que no estén vacíos.
     * Si ambos están bien, los manda al método que los abre; si alguno no pasa las
     * comprobaciones, no lo manda.
     * 
     * @param files
     */
    private void checkFilesList(List<File> files) {
        ArrayList<File> filesCopy = new ArrayList<File>();
        for (File file : files) {
            if (!file.exists()) {
                NotificationController.warningMsg("Problema con archivo",
                        "El archivo seleccionado no existe: " + file.getName());

            } else if (file.length() == 0) {
                NotificationController.warningMsg("Problema con archivo",
                        "El archivo seleccionado está vacío: " + file.getName());
            } else {
                filesCopy.add(file);
            }
        }

        if (filesCopy.size() == 2) {
            openFiles(filesCopy);
        } else if (filesCopy.size() == 1) {
            openFile(filesCopy.get(0));
        }

    }

    private void openFile(File file) {
        int extensionIndex = file.getName().lastIndexOf('.');
        String fileExtension = file.getName().substring(extensionIndex);

        // Comprueba qué extensión tiene el archivo recibido
        switch (fileExtension) {
            case ".txt":
                // Trata el archivo como uno de Seetrax
                TxtFileReader.read(file, componentsTable, cancelButton, saveButton);
                break;

            case ".csv":
                // Recibe todas las placas de la base de datos hechas con KiCad y que no se
                // hayan introducido dos csv's
                ArrayList<Board> kiCadBoards = getKiCadBoards();

                // Si hay alguna, pregunta si el csv tiene relación con alguna de ellas
                if (kiCadBoards != null && kiCadBoards.size() > 0) {
                    Dialog<ButtonType> dialog = new Dialog<>();
                    dialog.setTitle("Selecciona una opción:");
                    dialog.setHeaderText("¿Nueva placa o placa almacenada?");

                    RadioButton newBoard = new RadioButton("El archivo es de una placa nueva");
                    RadioButton boardInDB = new RadioButton("El archivo es de una placa de la base de datos");
                    ToggleGroup group = new ToggleGroup();
                    newBoard.setToggleGroup(group);
                    boardInDB.setToggleGroup(group);
                    newBoard.setSelected(true);

                    VBox vbox = new VBox(newBoard, boardInDB);
                    dialog.getDialogPane().setContent(vbox);

                    ButtonType okButton = new ButtonType("OK", ButtonData.OK_DONE);
                    dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

                    Optional<ButtonType> response = dialog.showAndWait();

                    if (response.get().equals(okButton) && newBoard.isSelected()) {
                        CsvFileReader.read(file, null, componentsTable, cancelButton, saveButton);
                    } else if (response.get().equals(okButton) && boardInDB.isSelected()) {
                        // Si el usuario quiere unir el archivo a una de las placas existentes, se llama
                        // a selecteBoardFromDb para que seleccione la placa a unir
                        selectBoardFromDb(file, kiCadBoards);
                    }

                } else {
                    NotificationController.informationMsg("No hay placas KiCad", "Que pena!");
                    CsvFileReader.read(file, null, componentsTable, cancelButton, saveButton);
                }
                break;

            default:
                NotificationController.errorMsg("Error con archivo",
                        "Lo sentimos. Solo puedes abrir archivos .txt o .csv");
                break;
        }
    }

    /**
     * Muestra diálogo para que seleccione la placa que con la que quiere unir el
     * csv a abrir
     * 
     * @param file
     * @param kiCadBoards
     */
    private void selectBoardFromDb(File file, ArrayList<Board> kiCadBoards) {
        Dialog<ButtonType> dialogComponent = new Dialog<>();
        dialogComponent.setTitle("Información necesaria");
        dialogComponent.setHeaderText("Selecciona la placa a la que pertenece:");

        ComboBox<String> comboBoxBoards = new ComboBox<>();
        for (Board board : kiCadBoards) {
            comboBoxBoards.getItems().add(board.getBoardName());
        }
        comboBoxBoards.getSelectionModel().selectFirst();
        dialogComponent.getDialogPane().setContent(comboBoxBoards);

        ButtonType okButton2 = new ButtonType("OK", ButtonData.OK_DONE);
        dialogComponent.getDialogPane().getButtonTypes().addAll(okButton2, ButtonType.CANCEL);

        Optional<ButtonType> response2 = dialogComponent.showAndWait();

        if (response2.get().equals(okButton2)) {
            CsvFileReader.read(file,
                    kiCadBoards.get(comboBoxBoards.getSelectionModel().getSelectedIndex()),
                    componentsTable, cancelButton, saveButton);
        }
    }

    /**
     * Recibe una lista de archivos. Si la extensión es .txt avisa que sólo se debe
     * poder abrir uno y abre el primero de la lista.
     * Si los archivos son csv, manda a abrir los dos.
     * 
     * @param files
     */
    private void openFiles(List<File> files) {
        int extensionIndex = files.get(0).getName().lastIndexOf('.');
        String fileExtension = files.get(0).getName().substring(extensionIndex);

        switch (fileExtension) {
            case ".txt":
                NotificationController.warningMsg("Sólo un .txt permitido a la vez",
                        "Hemos cargado el primero.");
                TxtFileReader.read(files.get(0), componentsTable, cancelButton, saveButton);
                break;

            case ".csv":
                CsvFileReader.read(files, componentsTable, cancelButton, saveButton);
                break;

            default:
                NotificationController.errorMsg("Error con archivo",
                        "Lo sentimos. Solo puedes abrir archivos .txt o .csv");
                break;
        }
    }

    /**
     * Selecciona todas las placas que hay en la base de datos que sean del programa
     * KiCad y que sólo se hayan introducido un archivo csv.
     * Al tener ya una lista actualizada de todas las placas de la base de datos, se
     * recorre esta, ahorrando tiempo y consultas a la base de datos
     * 
     * @return ArrayList con las placas que cumplen ambas condiciones
     */
    private ArrayList<Board> getKiCadBoards() {
        ArrayList<Board> kiCadBoards = new ArrayList<>();
        for (Board board : boards) {
            if (board.getProgram().equals(ProgramType.KiCad) && board.isFilesLeft()) {
                kiCadBoards.add(board);
            }
        }
        return kiCadBoards;
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

    /**
     * Abre un diálogo para introducir el nombre con el que se quiere exportar los
     * archivos. Si el usuario deja el campo en blanco, no se le permite darle al
     * botón de continuar.
     * 
     * @return
     */
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
            boolean success = true;

            GridPane pageGrid = null;
            int count = 0;

            for (Components component : components) {
                if (count % 4 == 0) {
                    if (pageGrid != null && !printerJob.printPage(pageGrid)) {
                        success = false;
                        break;
                    }
                    pageGrid = new GridPane();
                    pageGrid.setPadding(new Insets(20));
                    pageGrid.setHgap(20);
                    pageGrid.setVgap(20);
                }

                Node node = component.getNode();
                pageGrid.add(node, count % 2, (count / 2) % 2);

                count++;
            }

            // Print any remaining page with less than 4 components
            if (pageGrid != null && !printerJob.printPage(pageGrid)) {
                success = false;
            }

            if (success) {
                printerJob.endJob();
                NotificationController.informationMsg("Proceso finalizado",
                        "El contenido de la tabla ha sido imprimido.");
            } else {
                NotificationController.informationMsg("Error",
                        "La impresión falló.");
            }
        }
    }

    /**
     * Al apretar el botón de guardar, se abre un diálogo para introducir un nombre
     * para la placa si esta no está almacenada
     */
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

    /**
     * Almacena o modifica la placa recibida junto a todos sus componentes. Una vez
     * termina, recarga el comboBox con las placas que hay en la base de datos.
     * 
     * @param board
     */
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

    /**
     * Al apretar el botón de cancelar, abre un diálogo para que el usuario
     * confirme. Si lo hace pueden pasar dos cosas:
     * 1. Que desahaga los cambios de la tabla si es que había hecho algunos
     * 2. Si el usuario no había hecho cambios, lo que hace es vaciar la tabla
     */
    @FXML
    public void askForCancel() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Información necesaria");
        dialog.setHeaderText("¿Estás seguro de que quieres cancelar?");

        ButtonType okButtonType = new ButtonType("OK", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == okButtonType) {
                if (isModifying) {
                    componentsTable.getItems().clear();
                    components = new ArrayList<>(originalComponents);
                    componentsTable.setItems(FXCollections.<Components>observableArrayList(components));

                } else {
                    components.clear();
                    componentsTable.getItems().clear();
                }

                NotificationController.informationMsg("Proceso Cancelado", "El proceso ha sido cancelado.");
                saveButton.setDisable(true);
                cancelButton.setDisable(true);
            }
        });
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

    public void dbConfig() {
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

        // Convertir el resultado en un Pair<String, Pair<String, String>> cuando se
        // haga clic en Confirmar
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
                reloadDb.setDisable(false);
                comboBoxBoards.setDisable(false);
                loadBoard.setDisable(false);
                dbConnected = true;

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

    public void helpManual() {
        Help.helpManual();
    }

    public void helpTutorials() {
        Help.helpTutorials();
    }

    public void helpAbout() {
        Help.helpAbout();
    }

    public static void closeDb() {
        try {
            session.close();
            sessionFactory.close();
        } catch (Exception e) {
        }
    }

    public static void closeEmptyTableThread() {
        if (statesThread.isAlive()) {
            statesThread.interrupt();
        }
    }

}
