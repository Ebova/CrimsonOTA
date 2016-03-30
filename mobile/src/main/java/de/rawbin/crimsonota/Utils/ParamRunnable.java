package de.rawbin.crimsonota.Utils;

/**
 * Created by ebova on 3/29/16.
 */
public abstract class ParamRunnable implements Runnable {
    public Object[] params;
    public ParamRunnable(Object... params) {
        this.params = new Object[params.length];
        for(int i = 0; i < params.length; i++) { // Explicitly write params to an array
            this.params[i] = params[i];
        }
    }
}
