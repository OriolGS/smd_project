package com.smd.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.smd.controller.NotificationController;
import com.smd.gui.MainController;
import com.smd.model.Board;
import com.smd.model.Components;
import com.smd.model.ProgramType;

import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

public class CsvFileReader {
    private static final String DELIMITER = ",";
    private static String COLUMNS_EXPECTED = "Ref,Val,Package,PosX,PosY,Rot,Side";

    public static void read(File file, Label wordName, TableView<Components> componentsTable) {
        BufferedReader br = null;
        MainController.components.clear();

        Board board = generateBoard(file.getName());

        try {
            br = new BufferedReader(new FileReader(file));

            if (br.readLine().trim().equals(COLUMNS_EXPECTED)) {
                extractComponents(br, board);

                board.setComponents(MainController.components);
                componentsTable.setItems(FXCollections.<Components>observableArrayList(MainController.components));
            } else {
                NotificationController.warningMsg("Estructura de archivo inválida", "Los campos deben ser: Ref,Val,Package,PosX,PosY,Rot,Side");
            }

        } catch (IOException e) {
            NotificationController.errorMsg("Error", "No se ha podido leer bien el archivo.");
        } catch (SecurityException e) {
            NotificationController.warningMsg("Atención!", "Problema de seguridad al acceder al archivo. No se ha podido abrir.");
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private static void extractComponents(BufferedReader br, Board board) throws IOException {
        String line;
        Components c;

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
        }
    }

    private static String removeQuotes(String word) {
        if (word.startsWith("\"") && word.endsWith("\"")) {
            return word.substring(1, word.length() - 1);
        } else {
            return word;
        }
    }

    private static Board generateBoard(String fileName) {
        Board board = new Board();

        int extensionIndex = fileName.lastIndexOf('.');
        String boardName = fileName.substring(0, extensionIndex);

        board.setBoardName(boardName);
        board.setProgram(ProgramType.KiCad);
        return board;
    }
}
