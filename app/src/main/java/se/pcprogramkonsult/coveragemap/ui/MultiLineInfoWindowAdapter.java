package se.pcprogramkonsult.coveragemap.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

class MultiLineInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final Context mContext;

    MultiLineInfoWindowAdapter(Context context) {
        mContext = context;
    }

    @Nullable
    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @NonNull
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        LinearLayout info = new LinearLayout(mContext);
        info.setOrientation(LinearLayout.VERTICAL);

        String titleText = marker.getTitle();
        if (titleText != null && titleText.length() > 0) {
            TextView title = new TextView(mContext);
            title.setTextColor(Color.BLACK);
            title.setGravity(Gravity.CENTER);
            title.setTypeface(null, Typeface.BOLD);
            title.setText(titleText);
            info.addView(title);
        }

        String snippetText = marker.getSnippet();
        if (snippetText != null && snippetText.length() > 0) {
            TextView snippet = new TextView(mContext);
            snippet.setTextColor(Color.GRAY);
            snippet.setText(snippetText);
            info.addView(snippet);
        }

        return info;
    }
}
