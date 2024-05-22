package com.smd.model;

public class Component {
    String identifier;
    int boardFK;
    String type;
    String outline;
    float posX;
    float posY;
    float rotation;
    boolean flip;

    public Component() {
    }

    public Component(String identifier) {
        this.identifier = identifier;
    }

    public Component(String identifier, int boardFK, String type, String outline, float posX, float posY,
            float rotation, boolean flip) {
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

    public float getPosX() {
        return posX;
    }

    public void setPosX(float posX) {
        this.posX = posX;
    }

    public float getPosY() {
        return posY;
    }

    public void setPosY(float posY) {
        this.posY = posY;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public boolean isFlip() {
        return flip;
    }

    public void setFlip(boolean flip) {
        this.flip = flip;
    }

}
