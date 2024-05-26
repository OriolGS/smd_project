package com.smd.utils;

import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.smd.model.Component;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class AsqWriter {
    private static final String HASH = "#";
    private static final String SEPARATOR = ",";
    private static final String CHUCK = HASH + "PXY" + HASH + SEPARATOR;
    private static final String FILE_NAME = "production1Maquina1.asq";
    private static String asqString = "";

    public static void generate(ArrayList<Component> components) {
        for (Component c : components) {
            asqString += HASH + c.getIdentifier() + HASH + SEPARATOR + c.getPosX() + SEPARATOR + " " + c.getPosY() + " "
                    + c.getRotation() + SEPARATOR + CHUCK + HASH + HASH + SEPARATOR + HASH + c.getType() + " "
                    + c.getOutline() + " " + HASH + "1,T,#1#,0,F,#TAPE#,#X#,#" + c.isFlip() + "#,##,##,F";

            asqString += "\n";
        }

        FileWriter fw = null;
        try {
            fw = new FileWriter(FILE_NAME);
            fw.write(asqString);
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
