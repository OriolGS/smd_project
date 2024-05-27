package com.smd.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

@Entity
@Table(name = "component")
public class Component implements Serializable {
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

    public Component() {
    }

    public Component(String identifier, Board boardFK, String type, String outline, String posX, String posY,
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

    public Component(String identifier, Board boardFK, String type, String outline, Float posX, Float posY,
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
        // TODO: modificar cómo se muestran
        VBox vbox = new VBox();
        vbox.getChildren().addAll(
                new Label("Identifier: " + identifier),
                new Label("Type: " + type),
                new Label("Outline: " + outline),
                new Label("Position X: " + posX),
                new Label("Position Y: " + posY),
                new Label("Rotation: " + rotation),
                new Label("Flip: " + flip));
        return vbox;
    }

}
