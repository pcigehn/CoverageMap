package se.pcprogramkonsult.coveragemap.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.HashMap;
import java.util.Map;

import se.pcprogramkonsult.coveragemap.R;

class HandoverBitmap {
    @SuppressLint("UseSparseArrays")
    private static final Map<Integer, BitmapDescriptor> mHandoverBitmaps = new HashMap<>();
    @SuppressLint("UseSparseArrays")
    private static final Map<Integer, BitmapDescriptor> mDetailedHandoverBitmaps = new HashMap<>();

    private static final int INTRA      = 0b001;
    private static final int INTER_UP   = 0b010;
    private static final int INTER_DOWN = 0b100;

    private static final int INCOMING_INTRA      = 0b000001;
    private static final int INCOMING_INTER_UP   = 0b000010;
    private static final int INCOMING_INTER_DOWN = 0b000100;
    private static final int OUTGOING_INTRA      = 0b001000;
    private static final int OUTGOING_INTER_UP   = 0b010000;
    private static final int OUTGOING_INTER_DOWN = 0b100000;

    private final boolean isDetailedHandover;
    private int mHandoverBitmask = 0b000;
    private int mDetailedHandoverBitmask = 0b000000;

    HandoverBitmap(boolean isDetailedHandover) {
        this.isDetailedHandover = isDetailedHandover;
    }

    static void updateBitmaps(@NonNull Context context) {
        for (int i1 = 0; i1 < 2; i1++) {
            for (int i2 = 0; i2 < 2; i2++) {
                for (int i3 = 0; i3 < 2; i3++) {
                    Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_handover_base);
                    if (drawable != null) {
                        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        int bitmask = 0b000;
                        if (i1 > 0) {
                            bitmask |= INTRA;
                            addToCanvas(context, canvas, R.drawable.ic_incoming_intra);
                            addToCanvas(context, canvas, R.drawable.ic_outgoing_intra);
                        }
                        if (i2 > 0) {
                            bitmask |= INTER_UP;
                            addToCanvas(context, canvas, R.drawable.ic_incoming_inter_up);
                            addToCanvas(context, canvas, R.drawable.ic_outgoing_inter_up);
                        }
                        if (i3 > 0) {
                            bitmask |= INTER_DOWN;
                            addToCanvas(context, canvas, R.drawable.ic_incoming_inter_down);
                            addToCanvas(context, canvas, R.drawable.ic_outgoing_inter_down);
                        }
                        mHandoverBitmaps.put(bitmask, BitmapDescriptorFactory.fromBitmap(bitmap));
                    }
                }
            }
        }

        for (int i1 = 0; i1 < 2; i1++) {
            for (int i2 = 0; i2 < 2; i2++) {
               for (int i3 = 0; i3 < 2; i3++) {
                   for (int i4 = 0; i4 < 2; i4++) {
                       for (int i5 = 0; i5 < 2; i5++) {
                           for (int i6 = 0; i6 < 2; i6++) {
                               Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_handover_base);
                               if (drawable != null) {
                                   Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                                           drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                                   Canvas canvas = new Canvas(bitmap);
                                   addToCanvas(context, canvas, R.drawable.ic_handover_base);
                                   int bitmask = 0b000000;
                                   if (i1 > 0) {
                                       bitmask |= INCOMING_INTRA;
                                       addToCanvas(context, canvas, R.drawable.ic_incoming_intra);
                                   }
                                   if (i2 > 0) {
                                       bitmask |= INCOMING_INTER_UP;
                                       addToCanvas(context, canvas, R.drawable.ic_incoming_inter_up);
                                   }
                                   if (i3 > 0) {
                                       bitmask |= INCOMING_INTER_DOWN;
                                       addToCanvas(context, canvas, R.drawable.ic_incoming_inter_down);
                                   }
                                   if (i4 > 0) {
                                       bitmask |= OUTGOING_INTRA;
                                       addToCanvas(context, canvas, R.drawable.ic_outgoing_intra);
                                   }
                                   if (i5 > 0) {
                                       bitmask |= OUTGOING_INTER_UP;
                                       addToCanvas(context, canvas, R.drawable.ic_outgoing_inter_up);
                                   }
                                   if (i6 > 0) {
                                       bitmask |= OUTGOING_INTER_DOWN;
                                       addToCanvas(context, canvas, R.drawable.ic_outgoing_inter_down);
                                   }
                                   mDetailedHandoverBitmaps.put(bitmask, BitmapDescriptorFactory.fromBitmap(bitmap));
                               }
                           }
                       }
                   }
               }
            }
        }
    }

    static private void addToCanvas(@NonNull Context context, @NonNull Canvas canvas, int id) {
        Drawable drawable = ContextCompat.getDrawable(context, id);
        if (drawable instanceof VectorDrawable) {
            VectorDrawable vectorDrawable = (VectorDrawable) drawable;
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.draw(canvas);
        }
    }

    void addIntra() {
        mHandoverBitmask |= INTRA;
    }

    void addInterUp() {
        mHandoverBitmask |= INTER_UP;
    }

    void addInterDown() {
        mHandoverBitmask |= INTER_DOWN;
    }

    void addIncomingIntra() {
        mDetailedHandoverBitmask |= INCOMING_INTRA;
    }

    void addIncomingInterUp() {
        mDetailedHandoverBitmask |= INCOMING_INTER_UP;
    }

    void addIncomingInterDown() {
        mDetailedHandoverBitmask |= INCOMING_INTER_DOWN;
    }

    void addOutgoingIntra() {
        mDetailedHandoverBitmask |= OUTGOING_INTRA;
    }

    void addOutgoingInterUp() {
        mDetailedHandoverBitmask |= OUTGOING_INTER_UP;
    }

    void addOutgoingInterDown() {
        mDetailedHandoverBitmask |= OUTGOING_INTER_DOWN;
    }

    @Nullable
    BitmapDescriptor get() {
        if (isDetailedHandover) {
            return mDetailedHandoverBitmaps.get(mDetailedHandoverBitmask);
        } else {
            return mHandoverBitmaps.get(mHandoverBitmask);
        }
    }
}
