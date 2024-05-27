package com.smd.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "board")
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "BoardName", length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "Program")
    private ProgramType programType;

    @OneToMany(mappedBy = "boardFK", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Component> components;

    public Board() {
    }

    public Board(String name, ProgramType programType, ArrayList<Component> components) {
        this.name = name;
        this.programType = programType;
        this.components = components;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBoardName() {
        return name;
    }

    public void setBoardName(String name) {
        this.name = name;
    }

    public ProgramType getProgram() {
        return programType;
    }

    public void setProgram(ProgramType programType) {
        this.programType = programType;
    }

    public List<Component> getComponents() {
        return components;
    }

    public void setComponents(ArrayList<Component> components) {
        this.components = components;
    }

    @Override
    public String toString() {
        return "Board [id=" + id + ", name=" + name + ", programType=" + programType + "]";
    }

}
