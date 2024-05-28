package com.smd.controller;

import org.controlsfx.control.Notifications;

public class NotificationController {
    public static void informationMsg(String titulo, String mensaje) {
        Notifications.create()
            .title(titulo)
            .text(mensaje)
            .showInformation();
    }
    
    public static void warningMsg(String titulo, String mensaje) {
        Notifications.create()
            .title(titulo)
            .text(mensaje)
            .showWarning();
    }
    
    public static void errorMsg(String titulo, String mensaje) {
        Notifications.create()
            .title(titulo)
            .text(mensaje)
            .showError();
    }
}
