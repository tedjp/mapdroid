package au.id.tedp.mapdroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.lang.String;
import java.util.concurrent.FutureTask;
import java.util.LinkedList;
import java.net.URL;

/*
   Class for fetching tiles from a web server.
*/


public class TileFetcher {
    private LinkedList<String> requestedUris;
    private boolean requestInProgress = false;
    private Bitmap currentBmp;

    public TileFetcher() {


    }

    private void startRequest() {
	requestInProgress = true;
//	new Thread(new Download(requestedUris.remove(), this));
    }

    public synchronized void requestUri(String uri) {
	requestedUris.add(uri);
	if (!requestInProgress)
	    startRequest();
    }

    /*
    private class Download extends FutureTask<Bitmap> {
	private String sUrl;
	private Bitmap bmp;

	public Download(String url, TileFetcher notify) {
	    sUrl = url;
	}

	public void run() {
	    URL u = new URL(sUrl);
	    bmp = BitmapFactory.decodeStream(u.openStream());
	}

	protected void done() {

	}
    }
*/
}
