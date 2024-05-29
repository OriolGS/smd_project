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
    // TODO: cambiar cómo se consigue el nombre del archivo
    private static final String FILE_NAME = "production1Maquina2.csv";
    private static String csvText;

    public static void generate(ArrayList<Components> components) {
        csvText = COLUMNS_HEADER;
        csvText += "\n";

        for (Components c : components) {
            csvText += c.getIdentifier() + SEPARATOR + "1/2,1," + c.getPosX() + SEPARATOR + c.getPosY() + SEPARATOR
                    + c.getRotation() + ",0,100,None,VERDADERO, " + c.getType() + " " + c.getOutline();
            csvText += "\n";
        }

        FileWriter fw = null;
        try {
            fw = new FileWriter(FILE_NAME);
            fw.write(csvText);
            fw.close();

            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Archivo creado correctamente");
            alert.setHeaderText("El archivo se ha creado en:");
            alert.setContentText(FILE_NAME);
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
