package de.rawbin.crimsonota.Exceptions;

/**
 * Created by ebova on 3/23/16.
 */
public class DownloadChecksumErrorException  extends Exception {
    public DownloadChecksumErrorException() {
        super("The checksum of the downloaded file doesn't match the expected checksum.");
    }
}
