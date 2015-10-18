package de.axxepta.oxygen.core;

/**
 * @author Max on 01.09.2015.
 */
public interface ObserverInterface {


        //method to update the observer, used by subject
        void update(String type, String message);

        //attach with subject to observe
       //public void setSubject(SubjectInterface sub);
}
