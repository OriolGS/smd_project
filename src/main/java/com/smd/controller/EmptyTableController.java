package com.smd.controller;

import com.smd.model.Components;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;

public class EmptyTableController implements Runnable {
    @FXML
    private TableView<Components> componentsTable;

    @FXML
    private MenuItem exportToAsq, exportToCsv, printBoard, centerComponent, flipBoard;

    public EmptyTableController(TableView<Components> componentsTable, MenuItem exportToAsq, MenuItem exportToCsv,
            MenuItem printBoard, MenuItem centerComponent, MenuItem flipBoard) {
        this.componentsTable = componentsTable;
        this.exportToAsq = exportToAsq;
        this.exportToCsv = exportToCsv;
        this.printBoard = printBoard;
        this.centerComponent = centerComponent;
        this.flipBoard = flipBoard;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            checkTable();
        }

        System.out.println("Hilo interrumpido");
    }

    private void checkTable() {
        if (componentsTable.getItems().isEmpty()) {
            exportToAsq.setDisable(true);
            exportToCsv.setDisable(true);
            printBoard.setDisable(true);
            centerComponent.setDisable(true);
            flipBoard.setDisable(true);
        } else {
            exportToAsq.setDisable(false);
            exportToCsv.setDisable(false);
            printBoard.setDisable(false);
            centerComponent.setDisable(false);
            flipBoard.setDisable(false);
        }
    }
}
