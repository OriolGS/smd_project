package com.smd.model;

public class Component {
    String identifier;
    int boardFK;
    String type;
    String outline;
    String posX;
    String posY;
    String rotation;
    Boolean flip;

    public Component() {
    }

    public Component(String identifier, int boardFK, String type, String outline, String posX, String posY,
            String rotation, Boolean flip) {
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

    public int getBoardFK() {
        return boardFK;
    }

    public void setBoardFK(int boardFK) {
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

    public String getPosX() {
        return posX;
    }

    public void setPosX(String posX) {
        this.posX = posX;
    }

    public String getPosY() {
        return posY;
    }

    public void setPosY(String posY) {
        this.posY = posY;
    }

    public String getRotation() {
        return rotation;
    }

    public void setRotation(String rotation) {
        this.rotation = rotation;
    }

    public Boolean isFlip() {
        return flip;
    }

    public void setFlip(Boolean flip) {
        this.flip = flip;
    }

}
