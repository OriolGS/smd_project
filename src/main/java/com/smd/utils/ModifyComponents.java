package com.smd.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import com.smd.controller.NotificationController;
import com.smd.gui.MainController;
import com.smd.model.Components;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

public class ModifyComponents {

    private static float cumulativePosX = 0;
    private static float cumulativePosY = 0;
    private static float componentPosX = 0;
    private static float componentPosY = 0;

    @FXML
    private Button cancelButton, saveButton;

    /**
     * Constructor
     * @param cancelButton
     * @param saveButton
     */
    public ModifyComponents(Button cancelButton, Button saveButton) {
        this.cancelButton = cancelButton;
        this.saveButton = saveButton;
    }

    /**
     * Metodo para girar la placa cambiando el signo de la posición X de todos los componentes
     * @param componentsTable
     */
    public void flipBoard(TableView<Components> componentsTable) {
        for (Components component : MainController.components) {
            if(component.getPosX() == 0.0f){
                component.setPosX(0.0f);
            }else{
                component.setPosX(-round(component.getPosX(), 3));
            }
        }
        componentsTable.refresh();
        NotificationController.informationMsg("Proceso Completado", "La placa ha sido girada.");

        MainController.isModifying = true;
        saveButton.setText("Modify");
        if (MainController.dbConnected) {
            saveButton.setDisable(false);
        }
        cancelButton.setDisable(false);
    }

    /**
     * Metodo para centrar la placa respecto a un componente o a una posición
     * @param componentsTable
     */
    public void centerComponents(TableView<Components> componentsTable) {
        //Creamos el dialogo
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Información necesaria");
        dialog.setHeaderText("¿Respecto a qué quieres centrar la placa?");

        //Creamos los radioButtons
        RadioButton rbComponent = new RadioButton("Centrar respecto a un componente");
        RadioButton rbPosition = new RadioButton("Centrar respecto a una posición");
        ToggleGroup group = new ToggleGroup();
        rbComponent.setToggleGroup(group);
        rbPosition.setToggleGroup(group);
        rbComponent.setSelected(true);

        VBox vbox = new VBox(rbComponent, rbPosition);

        dialog.getDialogPane().setContent(vbox);

        ButtonType nextButton = new ButtonType("SIGUIENTE", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(nextButton, ButtonType.CANCEL);

        Optional<ButtonType> response = dialog.showAndWait();
        // Comprovamos si el usuario ha seleccionado centrar respecto a un componente o a una posición
        if (response.get().equals(nextButton) && rbComponent.isSelected()) {
            showDialogComponents(componentsTable);
        } else if (response.get().equals(nextButton) && rbPosition.isSelected()) {
            showDialogPositions(componentsTable);

        }
    }

    /**
     * A partir de una posición introducida por el usuario, centra la placa en esa posición
     * @param componentsTable
     */
    private void showDialogPositions(TableView<Components> componentsTable) {
        Dialog<ButtonType> dialogPosition = new Dialog<>();
        dialogPosition.setTitle("Información necesaria");
        dialogPosition.setHeaderText("Introduce la posición a la que quieres centrar la placa");

        // Creamos los textInputDialog
        TextInputDialog dialog2 = new TextInputDialog();
        dialog2.setTitle("Información necesaria");
        dialog2.setHeaderText("Introduce la posición a la que quieres centrar la placa");

        // Pedimos la posición X
        dialog2.setContentText("Introduce la posición X:");
        Optional<String> resultX = dialog2.showAndWait();

        // Pedimos la posición Y
        dialog2.setContentText("Introduce la posición Y:");
        Optional<String> resultY = dialog2.showAndWait();

        // Centramos la placa en la posición introducida
        if (resultX.isPresent() && resultY.isPresent()) {
            try {
                float posX = Float.parseFloat(resultX.get());
                float posY = Float.parseFloat(resultY.get());

                for (Components component : MainController.components) {
                    component.setPosX(round(component.getPosX() - posX + cumulativePosX + componentPosX, 3));
                    component.setPosY(round(component.getPosY() - posY + cumulativePosY + componentPosY, 3));
                }

                componentPosX = 0;
                componentPosY = 0;

                cumulativePosX = posX;
                cumulativePosY = posY;

                componentsTable.refresh();
                // Mostrar mensaje de éxito
                NotificationController.informationMsg("Proceso Completado",
                        "La placa ha sido centrada en la posición (" + posX + ", " + posY + ").");

                MainController.isModifying = true;
                saveButton.setText("Modify");
                if (MainController.dbConnected) {
                    saveButton.setDisable(false);
                }
                cancelButton.setDisable(false);

            } catch (NumberFormatException e) {
                NotificationController.errorMsg("Error", "Las posiciones deben ser números.");
            }
        }
    }

    /**
     * Muestra un dialogo para seleccionar un componente y centrar la placa respecto a él
     * @param componentsTable
     */
    private void showDialogComponents(TableView<Components> componentsTable) {
        // Creamos el dialogo
        Dialog<ButtonType> dialogComponent = new Dialog<>();
        dialogComponent.setTitle("Información necesaria");
        dialogComponent.setHeaderText("Selecciona un componente para centrar la placa");

        // Creamos el comboBox con los componentes
        ComboBox<String> comboBoxComponents = new ComboBox<>();
        for (Components component : MainController.components) {
            comboBoxComponents.getItems().add(component.getIdentifier());
        }

        comboBoxComponents.getSelectionModel().selectFirst();
        // Añadimos el comboBox al dialogo
        dialogComponent.getDialogPane().setContent(comboBoxComponents);
        // Añadimos los botones
        ButtonType okButton = new ButtonType("OK", ButtonData.OK_DONE);
        dialogComponent.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        Optional<ButtonType> response = dialogComponent.showAndWait();

        String selectedComponentId = comboBoxComponents.getValue();

        if (response.get().equals(okButton)) {
            centerFromComponent(componentsTable, selectedComponentId);
        }

    }

    /**
     * Centra la placa respecto al componente seleccionado
     * @param componentsTable
     * @param selectedComponentId
     */
    private void centerFromComponent(TableView<Components> componentsTable, String selectedComponentId) {
        Components selectedComponent = null;
        // Encontrar el componente seleccionado
        for (Components component : MainController.components) {
            if (component.getIdentifier().equals(selectedComponentId)) {
                selectedComponent = component;
                break;
            }
        }
        // Centrar la placa respecto al componente seleccionado
        if (selectedComponent != null) {
            float oldPosX = selectedComponent.getPosX();
            float oldPosY = selectedComponent.getPosY();

            selectedComponent.setPosX(0.0f);
            selectedComponent.setPosY(0.0f);

            // Centrar el resto de componentes
            for (Components otherComponent : MainController.components) {
                if (!otherComponent.getIdentifier().equals(selectedComponentId)) {
                    otherComponent.setPosX(round(otherComponent.getPosX() - oldPosX, 3));
                    otherComponent.setPosY(round(otherComponent.getPosY() - oldPosY, 3));
                }
            }
            // Actualizar las variables de posición
            componentPosX += oldPosX;
            componentPosY += oldPosY;

            // Refrescar la tabla
            componentsTable.refresh();

            // Mostrar mensaje de éxito
            NotificationController.informationMsg("Proceso Completado",
                    "El componente " + selectedComponentId + " ha sido centrado en la placa.");

            MainController.isModifying = true;
            saveButton.setText("Modify");
            if (MainController.dbConnected) {
                saveButton.setDisable(false);
            }
            cancelButton.setDisable(false);
        }
    }

    /**
     * Redondea un número a un número de decimales determinado
     * @param value
     * @param places
     * @return
     */
    private float round(float value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Float.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.floatValue();
    }
}
