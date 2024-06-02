package com.smd.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import com.smd.controller.NotificationController;
import com.smd.gui.MainController;
import com.smd.model.Board;
import com.smd.model.Components;
import com.smd.model.ProgramType;

import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

public class CsvFileReader {
    private static final String DELIMITER = ",";
    private static String COLUMNS_EXPECTED = "Ref,Val,Package,PosX,PosY,Rot,Side";

    /**
     * Recibe la lista con los archivos a abrir, la tabla donde mostrarlos y los
     * botones para cancelar y guardar.
     * Comprueba que los archivos sean válidos y que contengan componentes dentro,
     * si lo son, muestra los componentes de ambos archivos y prepara los botones
     * para guardar y cancelar
     * 
     * @param files
     * @param componentsTable
     * @param cancelButton
     * @param saveButton
     */
    public static void read(List<File> files, TableView<Components> componentsTable, Button cancelButton,
            Button saveButton) {
        BufferedReader br = null;

        Board board = generateBoard(files.getFirst().getName());
        boolean encounteredException = false;

        for (File file : files) {
            try {
                br = new BufferedReader(new FileReader(file));
                if (br.readLine().trim().equals(COLUMNS_EXPECTED)) {
                    extractComponents(br, board);

                } else {
                    NotificationController.warningMsg("Estructura de archivo inválida",
                            "Los campos deben ser: Ref,Val,Package,PosX,PosY,Rot,Side");
                }
            } catch (IOException e) {
                encounteredException = true;
                NotificationController.errorMsg("Error", "No se ha podido leer bien el archivo " + file.getName());
            } catch (SecurityException e) {
                NotificationController.warningMsg("Atención!",
                        "Problema de seguridad al acceder al archivo " + file.getName() + ". No se ha podido abrir.");
                encounteredException = true;
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        board.setFilesLeft(encounteredException);

        if (MainController.dbConnected) {
            saveButton.setDisable(false);
        }
        cancelButton.setDisable(false);

        MainController.components = (ArrayList<Components>) board.getComponents();
        componentsTable.setItems(FXCollections.<Components>observableArrayList(MainController.components));
        MainController.isModifying = false;
        saveButton.setText("Save");

        MainController.originalComponents = new ArrayList<>();
        for (Components component : MainController.components) {
            MainController.originalComponents.add(new Components(component));
        }

    }

    /**
     * Recibe el archivo a abrir, la placa a la que pertenece (si es null es que es
     * una nueva placa), la tabla donde mostrar sus componentes y los botones para
     * cancelar y guardar.
     * Comprueba que el archivo sea válido y que contengan componentes dentro, si lo
     * es, los componentes y prepara los botones para guardar y cancelar
     * 
     * @param file
     * @param board
     * @param componentsTable
     * @param cancelButton
     * @param saveButton
     */
    public static void read(File file, Board board, TableView<Components> componentsTable, Button cancelButton,
            Button saveButton) {
        BufferedReader br = null;
        boolean newBoard;

        if (board == null) {
            board = generateBoard(file.getName());
            newBoard = true;
        } else {
            newBoard = false;
        }

        board.setFilesLeft(newBoard);

        try {
            br = new BufferedReader(new FileReader(file));

            if (br.readLine().trim().equals(COLUMNS_EXPECTED)) {

                if (MainController.dbConnected) {
                    saveButton.setDisable(false);
                }
                cancelButton.setDisable(false);

                extractComponents(br, board);

                componentsTable.setItems(FXCollections.<Components>observableArrayList(MainController.components));

                if (newBoard && !componentsTable.getItems().isEmpty()) {
                    saveButton.setText("Save");
                    MainController.isModifying = false;
                    MainController.originalComponents = new ArrayList<>();
                    for (Components component : MainController.components) {
                        MainController.originalComponents.add(new Components(component));
                    }
                } else if (!newBoard && !componentsTable.getItems().isEmpty()) {
                    saveButton.setText("Modify");
                    MainController.isModifying = true;
                    MainController.originalComponents = new ArrayList<>();
                    for (Components component : MainController.components) {
                        MainController.originalComponents.add(new Components(component));
                    }
                } else {
                    saveButton.setDisable(true);
                    cancelButton.setDisable(true);
                    NotificationController.errorMsg("Archivo vacío",
                            "El archivo seleccionado no contiene componentes");
                }

            } else {
                NotificationController.warningMsg("Estructura de archivo inválida",
                        "Los campos deben ser: Ref,Val,Package,PosX,PosY,Rot,Side");
            }

        } catch (IOException e) {
            NotificationController.errorMsg("Error", "No se ha podido leer bien el archivo.");
        } catch (SecurityException e) {
            NotificationController.warningMsg("Atención!",
                    "Problema de seguridad al acceder al archivo. No se ha podido abrir.");
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Extrae los componentes del archivo y los almacena en la placa
     * 
     * @param br
     * @param board
     * @throws IOException
     */
    private static void extractComponents(BufferedReader br, Board board) throws IOException {
        String line;
        Components c;

        MainController.components.clear();

        if (board.getComponents() == null) {
            board.setComponents(new ArrayList<>());
        }

        while ((line = br.readLine()) != null) {
            String[] data = line.split(DELIMITER);

            if (data.length == 7) {
                c = new Components(
                        removeQuotes(data[0]),
                        board,
                        removeQuotes(data[1]),
                        removeQuotes(data[2]),
                        removeQuotes(data[3]),
                        removeQuotes(data[4]),
                        removeQuotes(data[5]),
                        removeQuotes(data[6]).equals("bottom"));

            } else {
                c = new Components(
                        removeQuotes(data[0]),
                        board,
                        removeQuotes(data[1] + "," + data[2]),
                        removeQuotes(data[3]),
                        removeQuotes(data[4]),
                        removeQuotes(data[5]),
                        removeQuotes(data[6]),
                        removeQuotes(data[7]).equals("bottom"));

            }
            MainController.components.add(c);
            board.addComponent(c);
        }
    }

    private static String removeQuotes(String word) {
        if (word.startsWith("\"") && word.endsWith("\"")) {
            return word.substring(1, word.length() - 1);
        } else {
            return word;
        }
    }

    /**
     * Crea la placa para poder almacenar los componentes en su lista
     * 
     * @param fileName
     * @return
     */
    private static Board generateBoard(String fileName) {
        Board board = new Board();

        int extensionIndex = fileName.lastIndexOf('.');
        String boardName = fileName.substring(0, extensionIndex);

        board.setBoardName(boardName);
        board.setProgram(ProgramType.KiCad);
        return board;
    }
}
