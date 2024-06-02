package com.smd.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.smd.controller.NotificationController;
import com.smd.gui.MainController;
import com.smd.model.Board;
import com.smd.model.Components;
import com.smd.model.ProgramType;

import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

/**
 * Recibe el archivo a abrir la tabla donde mostrar sus componentes y los
 * botones para
 * cancelar y guardar.
 * Comprueba que el archivo sea válido y que contengan componentes dentro, si lo
 * es, los componentes y prepara los botones para guardar y cancelar
 */
public class TxtFileReader {
    private static final String INITIAL_LINE_EXPECTED = ".PARTS";
    private static final String FINAL_LINE = ".ENDPARTS";

    public static void read(File file, TableView<Components> componentsTable, Button cancelButton, Button saveButton) {
        BufferedReader br = null;
        String line = "";

        Board board = generateBoard(file.getName());

        try {
            br = new BufferedReader(new FileReader(file));
            line = br.readLine();

            if (line.trim().equals(INITIAL_LINE_EXPECTED)) {
                // Aquí es pot cancelar i guardar

                if (MainController.dbConnected) {
                    saveButton.setDisable(false);
                }
                cancelButton.setDisable(false);
                MainController.components.clear();

                while ((line = br.readLine()) != null && !line.trim().equals(FINAL_LINE)) {
                    extractComponent(line, board);
                }
                board.setComponents(MainController.components);
                componentsTable.setItems(FXCollections.observableArrayList(MainController.components));
                MainController.isModifying = false;
                saveButton.setText("Save");

                MainController.originalComponents = new ArrayList<>();
                for (Components component : MainController.components) {
                    MainController.originalComponents.add(new Components(component));
                }
            } else {
                NotificationController.warningMsg("Estructura de archivo inválida",
                        "El archivo debe empezar con .PARTS");
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
     * @param line
     * @param board
     */
    private static void extractComponent(String line, Board board) {
        String[] component = new String[7];
        Components c;

        String[] data = line.split("  ");
        component[0] = data[0].trim();

        for (int i = 1; i < 5; i++) {
            data = line.substring(component[i - 1].length()).trim().split("  ");
            component[i] = data[0].trim();
            line = line.substring(component[i - 1].length()).trim();
        }

        data = line.substring(component[4].length()).trim().split(" ");

        if (data.length == 1) {
            c = new Components(component[0], board, component[1], component[2], component[3], component[4],
                    data[0].trim(), false);
        } else {
            c = new Components(component[0], board, component[1], component[2], component[3], component[4],
                    data[1].trim(), true);
        }

        MainController.components.add(c);
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
        board.setProgram(ProgramType.Seetrax);
        board.setFilesLeft(false);
        return board;
    }
}
