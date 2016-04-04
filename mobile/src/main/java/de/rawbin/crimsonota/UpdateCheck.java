package de.rawbin.crimsonota;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.RecoverySystem;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.rawbin.crimsonota.Exceptions.DownloadChecksumErrorException;
import de.rawbin.crimsonota.Exceptions.ObjectNotInitializedException;
import de.rawbin.crimsonota.Exceptions.UnableToSetCommandException;
import de.rawbin.crimsonota.Utils.ParamRunnable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdateCheck extends Activity {
    private Updater updater = null;
    private OkHttpClient client = new OkHttpClient();
    private AdditionalPackagesManager pacMan = null;
    private final String ONLINE_UPDATE_ROOT = "https://ariel.rawbin.de/img";
    private final String ONLINE_UPDATE_FILE = "update.zip";
    private final String ONLINE_VERSION = "VERSION";
    private final String VERSION_FILE = "VERSION";
    private final String UPDATE_FILE = "ota-update.zip";

    private final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_check);
        if(updater == null) {
            updater = new Updater();
            updater.powerMan = (PowerManager) getSystemService(Context.POWER_SERVICE);
            updater.context = getBaseContext();
            new Thread(new Runnable() {
                public void run() {
                    checkForUpdates();
                }
            }).start();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_update_check, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String getLocalVersion() {
        File versionFile = new File(Environment.getRootDirectory(), VERSION_FILE);
        Log.i("Crimson", versionFile.getAbsolutePath());
        String version;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(versionFile));
            version = reader.readLine().trim();
        } catch (FileNotFoundException e) {
            Log.i("Crimson", "We are not on an existing Crimson installation.");
            version = "";
        } catch (IOException e) {
            version = "";
            e.printStackTrace();
        }

        return version;
    }

    private void checkForUpdates(){
        try {
            Log.i("Crimson", "Getting version");
            String remoteVersion = getHttp(ONLINE_UPDATE_ROOT + "/" + ONLINE_VERSION).trim();
            Log.i("Crimson", "Got response");
            String localVersion = getLocalVersion();
            Log.i("Crimson", "Remote: " + remoteVersion);
            Log.i("Crimson", "Local: " + localVersion);
            runOnUiThread(new ParamRunnable(localVersion, remoteVersion) {
                @Override
                public void run() {
                    final TextView versionView = (TextView) findViewById(R.id.status_version);
                    String status = "Version " + ((String) params[0]) + " is installed.\nVersion " + ((String) params[1]) + " is available.";
                    if (params[0].equals(params[1])){
                        status += "\nSystem is up to date!";
                    }
                    else {
                        if(Integer.parseInt((String)params[0]) < Integer.parseInt((String)params[1])) {
                            status += "\nNew version available!";
                            Button updateButton = (Button) findViewById(R.id.btn_update);
                            updateButton.setEnabled(true);
                            updateButton.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    downloadUpdate();
                                }
                            });
                            pacMan = new AdditionalPackagesManager();
                            pacMan.layout = (LinearLayout)findViewById(R.id.additional);
                            pacMan.context = getBaseContext();
                            try {
                                pacMan.createCheckBoxes();
                            } catch (ObjectNotInitializedException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            status += "\nIt seems like the version you have installed is newer than the latest version available.";
                        }
                    }
                    versionView.setText(status);
                }
            });
        } catch(IOException ex) {
            Log.i("Crimson", "Failed");
        }
    }

    private String getHttp(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Log.i("Crimson", "Request created.");
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    private InputStream getBinaryHttp(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().byteStream();
    }

    private void downloadUpdate() {
        int permissionCheck = getBaseContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionCheck == PackageManager.PERMISSION_DENIED) {
            this.requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
        Button updateButton = (Button) findViewById(R.id.btn_update);
        updateButton.setEnabled(false);
        new Thread(new Runnable() {
            public void run() {
                String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + UPDATE_FILE;
                showMessage("Downloading to " + fileName);

                try {
                    File file = new File(Environment.getExternalStorageDirectory(), UPDATE_FILE);
                    if(file.exists()) file.delete();
                    FileOutputStream fileOut = new FileOutputStream(file);
                    BufferedInputStream in = new BufferedInputStream(getBinaryHttp(ONLINE_UPDATE_ROOT + "/" + ONLINE_UPDATE_FILE));

                    byte[] buffer = new byte[2048];
                    int count;
                    while ((count = in.read(buffer)) != -1) {
                        fileOut.write(buffer, 0, count);
                    }

                    fileOut.flush();
                    fileOut.close();

                    showMessage("Download completed. Checking file...");
                    if(!getFileChecksum(fileName).equals(getHttp(ONLINE_UPDATE_ROOT + "/checksum.md5").trim())) {
                        throw new DownloadChecksumErrorException();
                    }

                    showMessage("Success! Rebooting...");
                    //RecoverySystem.installPackage(getBaseContext(), file);
                    Updater updater = new Updater();
                    updater.context = getBaseContext();
                    updater.powerMan = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    updater.installUpdate(file.getPath(), pacMan.getAllPackages());
                } catch (FileNotFoundException e) {
                    showMessage("Error: File has not been saved");
                    e.printStackTrace();
                } catch (IOException e) {
                    showMessage("Error: Could not download file");
                    e.printStackTrace();
                } catch (DownloadChecksumErrorException e) {
                    showMessage("Error: Could not verify file integrity!");
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    showMessage("Error: Your version of android cannot verify the file integrity.");
                } catch (ObjectNotInitializedException e) {
                    e.printStackTrace();
                } catch (UnableToSetCommandException e) {
                    showMessage("Error: Could not set the command. Permission denied.");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    downloadUpdate();
                } else {
                    showMessage("Permission denied.");
                }
                return;
            }
        }
    }

    private void showMessage(String text){
        runOnUiThread(new ParamRunnable(text) {
            @Override
            public void run() {
                TextView versionView = (TextView) findViewById(R.id.status_version);
                versionView.setText(versionView.getText() + "\n" + params[0]);
            }
        });

    }

    private static char[] getCharArray(byte[] bytes) {
        char[] chars = new char[bytes.length];
        for(int i = 0; i < bytes.length; i++) {
            chars[i] = (char)bytes[i];
        }
        return chars;
    }

    private String getFileChecksum(String fileName) throws NoSuchAlgorithmException, IOException {
        File file = new File(Environment.getExternalStorageDirectory(), UPDATE_FILE);
        FileInputStream in = new FileInputStream(file);
        MessageDigest mdEnc = MessageDigest.getInstance("MD5");
        byte[] buffer = new byte[1024];
        int numRead = 0;
        while (numRead != -1) {
            numRead = in.read(buffer);
            if (numRead > 0) mdEnc.update(buffer, 0, numRead);
        }

        String md5 = getMd5String(mdEnc.digest());
        Log.i("Crimson", "Digest is " + md5);
        return md5;
    }

    private String getMd5String(byte[] md5Bytes) {
        String returnVal = "";
        for (int i = 0; i < md5Bytes.length; i++) {
            returnVal += Integer.toString(( md5Bytes[i] & 0xff ) + 0x100, 16).substring(1);
        }
        return returnVal;
    }
}
