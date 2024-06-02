package com.smd.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.smd.model.Components;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class AsqWriter {
    private static final String HASH = "#";
    private static final String SEPARATOR = ",";
    private static final String CHUCK = HASH + "PXY" + HASH + SEPARATOR;

    private static String asqText1 = "";
    private static String asqText2 = "";

    /**
     * Recibe los componentes a exportar y crea un String para los que están en la
     * cara A y otro para los de la cara B.
     * 
     * @param components
     * @param path
     */
    public static void generate(ArrayList<Components> components, String path) {
        for (Components c : components) {
            if (!c.isFlip()) {
                asqText1 += HASH + c.getIdentifier() + HASH + SEPARATOR + c.getPosX() + SEPARATOR + " " + c.getPosY()
                        + " "
                        + c.getRotation() + SEPARATOR + CHUCK + HASH + HASH + SEPARATOR + HASH + c.getType() + " "
                        + c.getOutline() + " " + HASH + "1,T,#1#,0,F,#TAPE#,#X#,#" + c.isFlip() + "#,##,##,F";

                asqText1 += "\n";
            } else {
                asqText2 += HASH + c.getIdentifier() + HASH + SEPARATOR + c.getPosX() + SEPARATOR + " " + c.getPosY()
                        + " "
                        + c.getRotation() + SEPARATOR + CHUCK + HASH + HASH + SEPARATOR + HASH + c.getType() + " "
                        + c.getOutline() + " " + HASH + "1,T,#1#,0,F,#TAPE#,#X#,#" + c.isFlip() + "#,##,##,F";

                asqText2 += "\n";
            }
        }

        writeAsqFile(path, asqText1, false);
        writeAsqFile(path, asqText2, true);
    }

    /**
     * Crea dos archivos .asq, uno para cada cara, añadiendo "_fliped" al archivo de la B para diferenciarlos. 
     * @param path
     * @param text
     * @param fliped
     */
    private static void writeAsqFile(String path, String text, boolean fliped) {
        FileWriter fw = null;
        try {
            String extensioString = fliped ? "_fliped.asq" : ".asq";
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
