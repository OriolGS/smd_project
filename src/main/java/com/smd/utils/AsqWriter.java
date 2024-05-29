package com.smd.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.smd.gui.MainController;
import com.smd.model.Component;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class AsqWriter {
    private static final String HASH = "#";
    private static final String SEPARATOR = ",";
    private static final String CHUCK = HASH + "PXY" + HASH + SEPARATOR;
    // TODO: cambiar cómo se consigue el nombre del archivo
    private static final String FILE_NAME = "production1Maquina1.asq";
    private static String asqText = "";

    public static void generate(ArrayList<Component> components) {
        for (Component c : components) {
            asqText += HASH + c.getIdentifier() + HASH + SEPARATOR + c.getPosX() + SEPARATOR + " " + c.getPosY() + " "
                    + c.getRotation() + SEPARATOR + CHUCK + HASH + HASH + SEPARATOR + HASH + c.getType() + " "
                    + c.getOutline() + " " + HASH + "1,T,#1#,0,F,#TAPE#,#X#,#" + c.isFlip() + "#,##,##,F";

            asqText += "\n";
        }

        FileWriter fw = null;
        String directory = FILE_NAME;
        try {
            if (MainController.exportDirectory != null) {
                 directory = MainController.exportDirectory + "\\" + FILE_NAME;
            }
            
            fw = new FileWriter(directory);
            fw.write(asqText);
            fw.close();

            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Archivo creado correctamente");
            alert.setHeaderText("El archivo se ha creado en:");
            alert.setContentText(directory);
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
