package de.rawbin.crimsonota.Exceptions;

/**
 * Created by ebova on 3/23/16.
 */

 public class UnableToSetCommandException extends Exception {
    public UnableToSetCommandException(){
        super("Could not set the update command.");
    }
 }