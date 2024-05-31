package com.smd.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

@Entity
@Table(name = "components")
public class Components implements Serializable {
    @Id
    @Column(name = "Identifier", length = 20)
    private String identifier;

    @Id
    @ManyToOne
    @JoinColumn(name = "BoardFk", referencedColumnName = "ID", nullable = false)
    private Board boardFK;

    @Column(name = "Type", length = 50)
    private String type;

    @Column(name = "Outline", length = 50)
    private String outline;

    @Column(name = "PosX")
    private Float posX;

    @Column(name = "PosY")
    private Float posY;

    @Column(name = "Rotation")
    private Float rotation;

    @Column(name = "Flip")
    private Boolean flip;

    public Components() {
    }

    public Components(String identifier, Board boardFK, String type, String outline, String posX, String posY,
            String rotation, Boolean flip) {
        this.identifier = identifier;
        this.boardFK = boardFK;
        this.type = type;
        this.outline = outline;
        this.posX = Float.valueOf(posX.replace(',', '.'));
        this.posY = Float.valueOf(posY.replace(',', '.'));
        this.rotation = Float.valueOf(rotation.replace(',', '.'));
        this.flip = flip;
    }

    public Components(String identifier, Board boardFK, String type, String outline, Float posX, Float posY,
            Float rotation, Boolean flip) {
        this.identifier = identifier;
        this.boardFK = boardFK;
        this.type = type;
        this.outline = outline;
        this.posX = posX;
        this.posY = posY;
        this.rotation = rotation;
        this.flip = flip;
    }

    public Components(Components component) {
        this.identifier = component.identifier;
        this.boardFK = component.boardFK;
        this.type = component.type;
        this.outline = component.outline;
        this.posX = component.posX;
        this.posY = component.posY;
        this.rotation = component.rotation;
        this.flip = component.flip;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Board getBoardFK() {
        return boardFK;
    }

    public void setBoardFK(Board boardFK) {
        this.boardFK = boardFK;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOutline() {
        return outline;
    }

    public void setOutline(String outline) {
        this.outline = outline;
    }

    public Float getPosX() {
        return posX;
    }

    public void setPosX(Float posX) {
        this.posX = posX;
    }

    public Float getPosY() {
        return posY;
    }

    public void setPosY(Float posY) {
        this.posY = posY;
    }

    public Float getRotation() {
        return rotation;
    }

    public void setRotation(Float rotation) {
        this.rotation = rotation;
    }

    public Boolean isFlip() {
        return flip;
    }

    public void setFlip(Boolean flip) {
        this.flip = flip;
    }

    public Node getNode() {
        VBox vbox = new VBox();
        vbox.setSpacing(15);  // Espaiat entre els elements
        vbox.setPadding(new Insets(20));  // Padding al voltant del VBox
        vbox.setAlignment(Pos.CENTER_LEFT);  // Alineació dels elements al centre esquerra
        vbox.setStyle(
            "-fx-background-color: #f9f9f9; " +
            "-fx-border-color: #b3b3b3; " +
            "-fx-border-radius: 10px; " +
            "-fx-background-radius: 10px; " +
            "-fx-padding: 20px;"
        );
        Label identifierLabel = new Label("Identifier: " + identifier);
        identifierLabel.setStyle(
            "-fx-font-weight: bold; " +
            "-fx-font-size: 16px; " +
            "-fx-text-fill: #2a2a2a;"
        );
        Label typeLabel = new Label("Type: " + type);
        typeLabel.setStyle(
            "-fx-font-weight: bold; " +
            "-fx-font-size: 16px; " +
            "-fx-text-fill: #2a2a2a;"
        );
        Label outlineLabel = new Label("Outline: " + outline);
        outlineLabel.setStyle(
            "-fx-font-style: italic; " +
            "-fx-font-size: 14px; " +
            "-fx-text-fill: #4a4a4a;"
        );
        Label posXLabel = new Label("Position X: " + posX);
        posXLabel.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-text-fill: #2a2a2a;"
        );
        Label posYLabel = new Label("Position Y: " + posY);
        posYLabel.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-text-fill: #2a2a2a;"
        );
        Label rotationLabel = new Label("Rotation: " + rotation);
        rotationLabel.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-text-fill: #0066cc;"
        );
        Label flipLabel = new Label("Flip: " + flip);
        flipLabel.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-text-fill: #cc0000;"
        );
        vbox.getChildren().addAll(
            identifierLabel,
            typeLabel,
            outlineLabel,
            posXLabel,
            posYLabel,
            rotationLabel,
            flipLabel
        );
        return vbox;
    }

}
