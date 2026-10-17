package com.quantech.filscore.wear;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

public final class MainActivity extends Activity implements DataClient.OnDataChangedListener {
    private static final String SNAPSHOT_PATH = "/financial-health-snapshot";
    private WearDashboardView dashboardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        dashboardView = new WearDashboardView(this);
        setContentView(dashboardView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Wearable.getDataClient(this).addListener(this);
    }

    @Override
    protected void onPause() {
        Wearable.getDataClient(this).removeListener(this);
        super.onPause();
    }

    @Override
    public void onDataChanged(DataEventBuffer events) {
        for (DataEvent event : events) {
            if (event.getType() != DataEvent.TYPE_CHANGED
                    || !SNAPSHOT_PATH.equals(event.getDataItem().getUri().getPath())) {
                continue;
            }
            byte[] payload = DataMapItem.fromDataItem(event.getDataItem())
                    .getDataMap()
                    .getByteArray("snapshot");
            if (payload != null && WearSnapshotStore.save(this, payload)) {
                runOnUiThread(() -> dashboardView.refreshSnapshot());
            }
        }
    }
}