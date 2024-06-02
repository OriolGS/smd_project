package com.smd.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.smd.model.Components;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class CsvWriter {
    private static final String COLUMNS_HEADER = "Designator,NozzleNum,StackNum,Mid X,Mid";
    private static final String SEPARATOR = ",";
    private static String csvText1;
    private static String csvText2;

    /**
     * Recibe los componentes a exportar y crea un String para los que están en la
     * cara A y otro para los de la cara B.
     * 
     * @param components
     * @param path
     */
    public static void generate(ArrayList<Components> components, String path) {
        csvText1 = COLUMNS_HEADER;
        csvText1 += "\n";
        csvText2 = COLUMNS_HEADER;
        csvText2 += "\n";

        for (Components c : components) {
            if (!c.isFlip()) {
                csvText1 += c.getIdentifier() + SEPARATOR + "1/2,1," + c.getPosX() + SEPARATOR + c.getPosY() + SEPARATOR
                        + c.getRotation() + ",0,100,None,VERDADERO, " + c.getType() + " " + c.getOutline();
                csvText1 += "\n";

            } else {
                csvText2 += c.getIdentifier() + SEPARATOR + "1/2,1," + c.getPosX() + SEPARATOR + c.getPosY() + SEPARATOR
                        + c.getRotation() + ",0,100,None,VERDADERO, " + c.getType() + " " + c.getOutline();
                csvText2 += "\n";
            }
        }

        writeCsvFile(path, csvText1, false);
        writeCsvFile(path, csvText2, true);

    }

    /**
     * Crea dos archivos .asq, uno para cada cara, añadiendo "_fliped" al archivo de
     * la B para diferenciarlos.
     * 
     * @param path
     * @param text
     * @param fliped
     */
    private static void writeCsvFile(String path, String text, boolean fliped) {
        FileWriter fw = null;
        try {
            String extensioString = fliped ? "_fliped.csv" : ".csv";
            fw = new FileWriter(path + extensioString);
            fw.write(text);
            fw.close();

            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Archivo creado correctamente");
            alert.setHeaderText("El archivo se ha creado en:");
            alert.setContentText(path + extensioString);
            alert.getButtonTypes().remove(1);
            alert.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Lo sentimos, no se ha podido crear correctamente");
            alert.getButtonTypes().remove(1);
            alert.show();
        }
    }
}
