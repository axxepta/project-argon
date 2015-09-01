package de.axxepta.oxygen.core;

/**
 * Created by Max on 01.09.2015.
 */
public interface ObserverInterface {


        //method to update the observer, used by subject
        public void update(String message);

        //attach with subject to observe
       //public void setSubject(SubjectInterface sub);
}
