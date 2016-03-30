package de.rawbin.crimsonota;

import android.content.Context;
import android.os.PowerManager;

import java.io.File;
import java.io.FileOutputStream;

import de.rawbin.crimsonota.Exceptions.ObjectNotInitializedException;
import de.rawbin.crimsonota.Exceptions.UnableToSetCommandException;

/**
 * Created by ebova on 3/21/16.
 */
public class Updater {
    PowerManager powerMan = null;
    Context context = null;

    public void reboot() throws ObjectNotInitializedException {
        if(powerMan != null) {
            powerMan.reboot("Crimson OTA update");
        }
        else {
            throw new ObjectNotInitializedException();
        }
    }

    public void queueUpdateInstallation(String updatePath) throws UnableToSetCommandException, ObjectNotInitializedException {
        if(context != null) {
            FileOutputStream outputStream;
            String command = "boot-recovery --update_package=" + updatePath;

            try {
                File commandFile = new File("/cache/recovery", "command");
                outputStream = new FileOutputStream(commandFile);
                outputStream.write(command.getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                throw new UnableToSetCommandException();
            }
        }
        else {
            throw new ObjectNotInitializedException();
        }
    }

    public void installUpdate(String updatePath) throws UnableToSetCommandException, ObjectNotInitializedException {
        queueUpdateInstallation(updatePath);
        reboot();
    }
}