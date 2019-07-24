package app.Controller;

public enum Panes {
    USER_PANE("usersPane.fxml"),
    MEDICS_PANE("medicsPane.fxml"),
    INTERVENTIONS_PANE("interventionsPane.fxml"),
    ENTRY_PANE("main.fxml"),
    ADMIN_PANE("admin.fxml"),
    MEDIC_PANE("user.fxml"),
    PATIENTS_PANE("patientsPane.fxml"),
    ADMIN_DASHBOARD("adminDashboard.fxml"),
    MEDIC_DASHBOARD("medicDashboard.fxml"),
    PATIENT_APPOINTMENTS_WINDOW("patientAppointmentsWindow.fxml"),
    APPOINTMENT_INTERVENTIONS_WINDOW("appointmentInterventionsWindow.fxml"),
    MEDIC_INTERVENTIONS_PANE("medicInterventionsPane.fxml"),;

    private final String pane;
    Panes(String s) {
        this.pane = s;
    }

    public String getPane() {
        return pane;
    }
}
