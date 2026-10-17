package com.quantech.filscore;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.nio.charset.StandardCharsets;

@CapacitorPlugin(name = "WatchSync")
public final class WatchSyncPlugin extends Plugin {
    private static final String SNAPSHOT_PATH = "/financial-health-snapshot";

    @PluginMethod
    public void updateSnapshot(PluginCall call) {
        JSObject snapshot = call.getObject("snapshot");
        if (snapshot == null) {
            call.reject("Missing snapshot");
            return;
        }

        PutDataMapRequest dataMapRequest = PutDataMapRequest.create(SNAPSHOT_PATH);
        dataMapRequest.getDataMap().putByteArray(
                "snapshot",
                snapshot.toString().getBytes(StandardCharsets.UTF_8)
        );
        dataMapRequest.getDataMap().putLong("sentAt", System.currentTimeMillis());
        PutDataRequest request = dataMapRequest.asPutDataRequest().setUrgent();

        Wearable.getDataClient(getContext()).putDataItem(request)
                .addOnSuccessListener(dataItem -> {
                    JSObject result = new JSObject();
                    result.put("saved", true);
                    result.put("sent", true);
                    call.resolve(result);
                })
                .addOnFailureListener(error -> call.reject("Unable to sync Wear snapshot", error));
    }
}