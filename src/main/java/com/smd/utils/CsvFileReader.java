package com.smd.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.smd.gui.MainController;
import com.smd.model.Board;
import com.smd.model.Component;
import com.smd.model.ProgramType;

import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

public class CsvFileReader {
    private static final String DELIMITER = ",";
    private static String COLUMNS_EXPECTED = "Ref,Val,Package,PosX,PosY,Rot,Side";
    private static ArrayList<Component> components = new ArrayList<>();

    public static void read(File file, Label wordName, TableView<Component> componentsTable) {
        Board board = generateBoard(file.getName());
        
        
        BufferedReader br = null;
        MainController.components.clear();
        try {
            br = new BufferedReader(new FileReader(file));

            if (br.readLine().trim().equals(COLUMNS_EXPECTED)) {

                extractComponents(br, board);

                componentsTable.setItems(FXCollections.<Component>observableArrayList(MainController.components));

            } else {
                wordName.setText("Estructura de archivo inválida.");
            }

        } catch (IOException e) {
            wordName.setText("No se ha podido leer bien el archivo");
        } catch (SecurityException e) {
            wordName.setText("Problema de seguridad al acceder al archivo");
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

        while ((line = br.readLine()) != null) {
            String[] data = line.split(DELIMITER);
            Component c;
            if (data.length == 7) {
                c = new Component(
                        removeQuotes(data[0]),
                        board,
                        removeQuotes(data[1]),
                        removeQuotes(data[2]),
                        removeQuotes(data[3]),
                        removeQuotes(data[4]),
                        removeQuotes(data[5]),
                        removeQuotes(data[6]).equals("bottom"));

            } else {
                c = new Component(
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
        // TODO: extract .csv
        board.setBoardName(fileName);
        board.setProgram(ProgramType.KiCad);

        return board;
    }
}
