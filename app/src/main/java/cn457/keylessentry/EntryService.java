package cn457.keylessentry;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class EntryService extends Service {
    public EntryService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
