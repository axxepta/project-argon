package de.axxepta.oxygen.api;

/**
 * @author Max on 01.09.2015.
 */
import de.axxepta.oxygen.core.ObserverInterface;
import de.axxepta.oxygen.core.SubjectInterface;

import java.util.ArrayList;
import java.util.List;

public class MsgTopic implements SubjectInterface {

    private List<ObserverInterface> observers;
    private String message;
    private String type;
    private boolean changed;
    private final Object MUTEX= new Object();

    MsgTopic(String msgType){
        this.observers=new ArrayList<>();
        this.type = msgType;
    }
    @Override
    public void register(ObserverInterface obj) {
        if(obj == null) throw new NullPointerException("Null Observer");
        synchronized (MUTEX) {
            if(!observers.contains(obj)) observers.add(obj);
        }
    }

/*    @Override
    public void unregister(ObserverInterface obj) {
        synchronized (MUTEX) {
            observers.remove(obj);
        }
    }*/

    @Override
    public void notifyObservers() {
        List<ObserverInterface> observersLocal;
        //synchronization is used to make sure any observer registered after message is received is not notified
        synchronized (MUTEX) {
            if (!changed)
                return;
            observersLocal = new ArrayList<>(this.observers);
            this.changed=false;
        }
        for (ObserverInterface obj : observersLocal) {
            obj.update(this.type, this.message);
        }

    }

    /*@Override
    public Object getUpdate(ObserverInterface obj) {
        return this.message;
    }*/

    //method to post message to the topic
    public void postMessage(String msg){
        System.out.println("Message Posted to Topic: "+msg);
        this.message=msg;
        this.changed=true;
        notifyObservers();
    }

}
