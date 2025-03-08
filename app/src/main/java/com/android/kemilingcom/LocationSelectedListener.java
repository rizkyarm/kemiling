package com.android.kemilingcom;

import com.google.android.gms.maps.model.LatLng;

public interface LocationSelectedListener {
    void onLocationSelected(LatLng latLng, String contextType);
}
