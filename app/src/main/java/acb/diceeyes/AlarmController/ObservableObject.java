package acb.diceeyes.AlarmController;

import java.util.Observable;


public class ObservableObject extends Observable {
    private static ObservableObject instance = new ObservableObject();

    boolean isScreenOn = true;
    int period;

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

    public boolean isScreenOn() {
        return isScreenOn;
    }
    public void setScreenOn(boolean screenOn) {
        isScreenOn = screenOn;
    }

    public void setPeriod(int period){
        this.period = period;
    }

    public int getPeriod(){
        return period;
    }

}