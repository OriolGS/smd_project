package com.smd.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import com.smd.controller.NotificationController;
import com.smd.gui.MainController;
import com.smd.model.Components;

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

    public static void flipBoard(TableView<Components> componentsTable) {
        // Flip the board by changing the sign of the X position of all components
        for (Components component : MainController.components) {
            component.setPosX(-round(component.getPosX(),3));
        }
        // Refresh the components table
        componentsTable.refresh();
        // Show notification message
        NotificationController.informationMsg("Proceso Completado", "La placa ha sido girada.");
    }

    public static void centerComponents(TableView<Components> componentsTable) {
        // Create the dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Información necesaria");
        dialog.setHeaderText("¿Respecto a qué quieres centrar la placa?");
        // Create the radio buttons and toggle group
        RadioButton rbComponent = new RadioButton("Centrar respecto a un componente");
        RadioButton rbPosition = new RadioButton("Centrar respecto a una posición");
        ToggleGroup group = new ToggleGroup();
        rbComponent.setToggleGroup(group);
        rbPosition.setToggleGroup(group);
        rbComponent.setSelected(true);

        // Add radio buttons to a VBox
        VBox vbox = new VBox(rbComponent, rbPosition);

        dialog.getDialogPane().setContent(vbox);
        // Add OK and Cancel buttons to the dialog
        ButtonType nextButton = new ButtonType("SIGUIENTE", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(nextButton, ButtonType.CANCEL);

        Optional<ButtonType> response = dialog.showAndWait();

        if (response.get().equals(nextButton) && rbComponent.isSelected()) {
            showDialogComponents(componentsTable);
        } else if (response.get().equals(nextButton) && rbPosition.isSelected()) {
            showDialogPositions(componentsTable);

        }
    }

    private static void showDialogPositions(TableView<Components> componentsTable) {
        // Dialog for selecting a position
        Dialog<ButtonType> dialogPosition = new Dialog<>();
        dialogPosition.setTitle("Información necesaria");
        dialogPosition.setHeaderText("Introduce la posición a la que quieres centrar la placa");
        // Create the text input fields
        TextInputDialog dialog2 = new TextInputDialog();
        dialog2.setTitle("Información necesaria");
        dialog2.setHeaderText("Introduce la posición a la que quieres centrar la placa");
        dialog2.setContentText("Introduce la posición X:");
        Optional<String> resultX = dialog2.showAndWait();
        dialog2.setContentText("Introduce la posición Y:");
        Optional<String> resultY = dialog2.showAndWait();
        if (resultX.isPresent() && resultY.isPresent()) {
            try {
                float posX = Float.parseFloat(resultX.get());
                float posY = Float.parseFloat(resultY.get());
                // Move the components based on the new cumulative positions
                for (Components component : MainController.components) {
                    component.setPosX(round(component.getPosX() - posX + cumulativePosX + componentPosX, 3));
                    component.setPosY(round(component.getPosY() - posY + cumulativePosY + componentPosY, 3));
                }
                //Reset the component positions
                componentPosX = 0;
                componentPosY = 0;
                // Update the cumulative positions
                cumulativePosX = posX;
                cumulativePosY = posY;
                // Refresh the components table
                componentsTable.refresh();
                // Show notification message
                NotificationController.informationMsg("Proceso Completado",
                        "La placa ha sido centrada en la posición (" + posX + ", " + posY + ").");
            } catch (NumberFormatException e) {
                NotificationController.errorMsg("Error", "Las posiciones deben ser números.");
            }
        }
    }

    private static void showDialogComponents(TableView<Components> componentsTable) {
        // Dialog for selecting a component
        Dialog<ButtonType> dialogComponent = new Dialog<>();
        dialogComponent.setTitle("Información necesaria");
        dialogComponent.setHeaderText("Selecciona un componente para centrar la placa");
        // Create the combo box
        ComboBox<String> comboBoxComponents = new ComboBox<>();
        for (Components component : MainController.components) {
            comboBoxComponents.getItems().add(component.getIdentifier());
        }

        comboBoxComponents.getSelectionModel().selectFirst();
        // Add the combo box to the dialog
        dialogComponent.getDialogPane().setContent(comboBoxComponents);
        // Add OK and Cancel buttons to the dialog
        ButtonType okButton = new ButtonType("OK", ButtonData.OK_DONE);
        dialogComponent.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        Optional<ButtonType> response = dialogComponent.showAndWait();

        String selectedComponentId = comboBoxComponents.getValue();

        if (response.get().equals(okButton)) {
            centerFromComponent(componentsTable, selectedComponentId);
        }
        
    }

    private static void centerFromComponent(TableView<Components> componentsTable, String selectedComponentId) {
        Components selectedComponent = null;
        for (Components component : MainController.components) {
            if (component.getIdentifier().equals(selectedComponentId)) {
                selectedComponent = component;
                break;
            }
        }
        // TODO: hay alguna posibilidad de que sea null?
        if (selectedComponent != null) {
            // Save the previous position of the selected component
            float oldPosX = selectedComponent.getPosX();
            float oldPosY = selectedComponent.getPosY();
            // Move the selected component to the center of the board (0,0)
            selectedComponent.setPosX(0.0f);
            selectedComponent.setPosY(0.0f);
            // Adjust the positions of the other components relative to the centered
            // component
            for (Components otherComponent : MainController.components) {
                if (!otherComponent.getIdentifier().equals(selectedComponentId)) {
                    otherComponent.setPosX(round(otherComponent.getPosX() - oldPosX,3));
                    otherComponent.setPosY(round(otherComponent.getPosY() - oldPosY,3));
                }
            }
            componentPosX += oldPosX;
            componentPosY += oldPosY;
            // Refresh the components table
            componentsTable.refresh();
            // Show notification message
            NotificationController.informationMsg("Proceso Completado",
                    "El componente " + selectedComponentId + " ha sido centrado en la placa.");
        }
    }

    // Utility method to round a float to a specific number of decimal places
    private static float round(float value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Float.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.floatValue();
    }
}
