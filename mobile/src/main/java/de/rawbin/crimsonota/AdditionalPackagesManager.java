package de.rawbin.crimsonota;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.rawbin.crimsonota.Exceptions.ObjectNotInitializedException;
import de.rawbin.crimsonota.Utils.ParamRunnable;

/**
 * Created by ebova on 4/4/16.
 */
public class AdditionalPackagesManager {
    LinearLayout layout = null;
    Context context = null;

    public void createCheckBoxes() throws ObjectNotInitializedException {
        if(layout == null || context == null) throw new ObjectNotInitializedException();
        Log.i("Crimson", "Looking for additional files");
        File f = Environment.getExternalStorageDirectory();
        File file[] = f.listFiles();
        for (int i = 0; i < file.length; i++) {
            String fileName = file[i].getName();
            if(!fileName.endsWith(".zip")) continue;
            Log.d("Crimson", "New Package found: " + fileName);
            CheckBox cb = new CheckBox(context);
            cb.setText(fileName);
            cb.setTextColor(Color.BLACK);
            layout.addView(cb);
        }
    }

    public ArrayList<String> getAllPackages() throws ObjectNotInitializedException {
        if (layout == null || context == null) throw new ObjectNotInitializedException();
        ArrayList<String> files = new ArrayList<String>();

        for (int i = 0; i < layout.getChildCount(); i++) {
            CheckBox cb = (CheckBox)layout.getChildAt(i);
            if(!cb.isChecked()) continue;
            String file = cb.getText().toString();
            files.add(file);
        }
        return files;
    }
}
