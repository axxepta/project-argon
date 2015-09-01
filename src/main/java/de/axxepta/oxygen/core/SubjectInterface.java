package de.axxepta.oxygen.core;

/**
 * Created by Max on 01.09.2015.
 */
public interface SubjectInterface {

    //methods to register and unregister observers
    public void register(ObserverInterface obj);
    public void unregister(ObserverInterface obj);

    //method to notify observers of change
    public void notifyObservers();

    //method to get updates from subject
    //public Object getUpdate(ObserverInterface obj);
}
