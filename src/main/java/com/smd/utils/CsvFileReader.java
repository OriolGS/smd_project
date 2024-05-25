package com.smd.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.smd.model.Component;

import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

public class CsvFileReader {
    private static final String DELIMITER = ",";
    private static String COLUMNS_EXPECTED = "Ref,Val,Package,PosX,PosY,Rot,Side";
    private static ArrayList<Component> components = new ArrayList<>();

    public static void read(File file, Label wordName, TableView<Component> componentsTable) {
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(file));

            if (br.readLine().trim().equals(COLUMNS_EXPECTED)) {

                extractComponents(br);

                componentsTable.setItems(FXCollections.<Component>observableArrayList(components));

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

    private static void extractComponents(BufferedReader br) throws IOException {
        String line;

        while ((line = br.readLine()) != null) {
            String[] data = line.split(DELIMITER);
            Component c;
            if (data.length == 7) {
                c = new Component(
                        removeQuotes(data[0]),
                        1,
                        removeQuotes(data[1]),
                        removeQuotes(data[2]),
                        removeQuotes(data[3]),
                        removeQuotes(data[4]),
                        removeQuotes(data[5]),
                        removeQuotes(data[6]).equals("bottom"));

            } else {
                c = new Component(
                        removeQuotes(data[0]),
                        1,
                        removeQuotes(data[1] + "," + data[2]),
                        removeQuotes(data[3]),
                        removeQuotes(data[4]),
                        removeQuotes(data[5]),
                        removeQuotes(data[6]),
                        removeQuotes(data[7]).equals("bottom"));

            }

            components.add(c);
        }
    }

    private static String removeQuotes(String word) {
        if (word.startsWith("\"") && word.endsWith("\"")) {
            return word.substring(1, word.length() - 1);
        } else {
            return word;
        }

    }
}
