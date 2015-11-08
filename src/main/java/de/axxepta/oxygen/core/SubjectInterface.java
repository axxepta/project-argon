package de.axxepta.oxygen.core;

/**
 * @author Max on 01.09.2015.
 */
public interface SubjectInterface {

    //methods to register and unregister observers
    void register(ObserverInterface obj);
    //void unregister(ObserverInterface obj);

    //method to notify observers of change
    void notifyObservers();

    //method to get updates from subject
    //public Object getUpdate(ObserverInterface obj);
}
