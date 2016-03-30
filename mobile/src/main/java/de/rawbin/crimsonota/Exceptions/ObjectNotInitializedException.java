package de.rawbin.crimsonota.Exceptions;

/**
 * Created by ebova on 3/23/16.
 */
public class ObjectNotInitializedException extends Exception {
    public ObjectNotInitializedException() {
        super("Object has not been initialized correctly.");
    }
}
