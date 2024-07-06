
package com.atakmap.android.soothsayer;

import android.content.Context;
import android.content.Intent;

import com.atakmap.android.soothsayer.layers.PluginMapOverlay;
import com.atakmap.android.soothsayer.plugin.R;
import com.atakmap.android.dropdown.DropDownMapComponent;
import com.atakmap.android.ipc.AtakBroadcast.DocumentedIntentFilter;
import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.log.Log;

public class PluginMapComponent extends DropDownMapComponent {

    private static final String TAG = "PluginTemplateMapComponent";

    private Context pluginContext;

    private PluginDropDownReceiver ddr;
    private PluginMapOverlay mapOverlay;

    public void onCreate(final Context context, Intent intent,
            final MapView view) {

        context.setTheme(R.style.ATAKPluginTheme);
        super.onCreate(context, intent, view);
        pluginContext = context;

        //Plugin MapOverlay added to Overlay Manager.
        this.mapOverlay = new PluginMapOverlay(view, pluginContext);
        view.getMapOverlayManager().addOverlay(this.mapOverlay);
        ddr = new PluginDropDownReceiver(
                view, context, mapOverlay);
        // below filters are used to handle custom menu options.
        Log.d(TAG, "registering the plugin filter");
        DocumentedIntentFilter ddFilter = new DocumentedIntentFilter();
        ddFilter.addAction(PluginDropDownReceiver.SHOW_PLUGIN);
        ddFilter.addAction(PluginDropDownReceiver.GRG_TOGGLE_VISIBILITY,
                "Toggle visibility of kmz layer");
        ddFilter.addAction(PluginDropDownReceiver.GRG_DELETE,
                "Delete kmz layer");
        ddFilter.addAction(PluginDropDownReceiver.RADIO_EDIT,
                "Edit marker");
        ddFilter.addAction(PluginDropDownReceiver.RADIO_DELETE,
                "Delete marker");
        registerDropDownReceiver(ddr, ddFilter);
    }

    @Override
    protected void onDestroyImpl(Context context, MapView view) {
        super.onDestroyImpl(context, view);
        view.getMapOverlayManager().removeOverlay(mapOverlay);
    }
}
