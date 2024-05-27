package com.smd.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import com.smd.gui.MainController;
import com.smd.model.Board;
import com.smd.model.Component;
import com.smd.model.ProgramType;

import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

public class TxtFileReader {
    private static final String INITIAL_LINE_EXPECTED = ".PARTS";
    private static final String FINAL_LINE = ".ENDPARTS";

    public static void read(File file, Label wordName, TableView<Component> componentsTable) {
        BufferedReader br = null;
        String line = "";
        MainController.components.clear();

        Board board = generateBoard(file.getName());

        try {
            br = new BufferedReader(new FileReader(file));
            line = br.readLine();

            if (line.trim().equals(INITIAL_LINE_EXPECTED)) {
                while ((line = br.readLine()) != null && !line.trim().equals(FINAL_LINE)) {
                    extractComponent(line, board);
                }
                board.setComponents(MainController.components);
                componentsTable.setItems(FXCollections.observableArrayList(MainController.components));

            } else {
                // TODO: mostrar mensajes de otra forma
                wordName.setText("Estructura de archivo inválida: debe empezar con .PARTS");
            }

        } catch (IOException e) {
            // TODO: mostrar mensajes de otra forma
            wordName.setText("No se ha podido leer bien el archivo");
        } catch (SecurityException e) {
            // TODO: mostrar mensajes de otra forma
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

    private static void extractComponent(String line, Board board) {
        String[] component = new String[7];
        Component c;

        String[] data = line.split("  ");
        component[0] = data[0].trim();

        for (int i = 1; i < 5; i++) {
            data = line.substring(component[i - 1].length()).trim().split("  ");
            component[i] = data[0].trim();
            line = line.substring(component[i - 1].length()).trim();
        }

        data = line.substring(component[4].length()).trim().split(" ");

        if (data.length == 1) {
            c = new Component(component[0], board, component[1], component[2], component[3], component[4],
                    data[0].trim(), false);
        } else {
            c = new Component(component[0], board, component[1], component[2], component[3], component[4],
                    data[1].trim(), true);
        }

        MainController.components.add(c);
    }

    private static Board generateBoard(String fileName) {
        Board board = new Board();
        
        int extensionIndex = fileName.lastIndexOf('.');
        String boardName = fileName.substring(0, extensionIndex);

        board.setBoardName(boardName);
        board.setProgram(ProgramType.Seetrax);
        return board;
    }
}
