package de.rawbin.crimsonota;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Example shell activity which simply broadcasts to our receiver and exits.
 */
public class UpdateStubBroadcastActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = new Intent();
        i.setAction("de.rawbin.crimsonota.SHOW_NOTIFICATION");
        i.putExtra(UpdateNotificationReceiver.CONTENT_KEY, getString(R.string.title));
        sendBroadcast(i);
        finish();
    }
}
