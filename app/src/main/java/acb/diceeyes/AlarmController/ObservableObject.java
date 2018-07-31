package acb.diceeyes.AlarmController;

import java.util.Observable;


public class ObservableObject extends Observable {
    private static ObservableObject instance = new ObservableObject();

    private int reminderPeriod = 0;

    public static ObservableObject getInstance() {
        return instance;
    }

    private ObservableObject() {
    }

    public void updateValue(Object data) {
        synchronized (this) {
            setChanged();
            notifyObservers(data);
        }
    }

    public void setReminderPeriod(int reminderPeriod) {
        this.reminderPeriod = reminderPeriod;
    }
    public int getReminderPeriod() {
        return reminderPeriod;
    }

}