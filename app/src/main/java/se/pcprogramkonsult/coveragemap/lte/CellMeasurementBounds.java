package se.pcprogramkonsult.coveragemap.lte;

import android.telephony.CellSignalStrengthLte;

import androidx.annotation.NonNull;

import java.util.Locale;

import se.pcprogramkonsult.coveragemap.model.CellMeasurement;

public class CellMeasurementBounds {
    private final MinMax mRsrp = new MinMax();
    private final MinMax mRsrq = new MinMax();
    private final MinMax mTa = new MinMax();
    private final MinMax mRssi = new MinMax();
    private final MinMax mSnr = new MinMax();

    public void include(@NonNull final CellMeasurement cellMeasurement) {
        mRsrp.include(cellMeasurement.getRsrp());
        mRsrq.include(cellMeasurement.getRsrq());
        mTa.include(cellMeasurement.getTa());
        mRssi.include(cellMeasurement.getRssi());
        mSnr.include(cellMeasurement.getSnr());
    }

    public boolean doesNotInclude(@NonNull final CellSignalStrengthLte signalStrength, final int snr) {
        return mRsrp.doesNotInclude(signalStrength.getRsrp()) ||
                mRsrq.doesNotInclude(signalStrength.getRsrq()) ||
                mTa.doesNotInclude(signalStrength.getTimingAdvance()) ||
                mRssi.doesNotInclude(signalStrength.getRssnr()) ||
                mSnr.doesNotInclude(snr);
    }

    private class MinMax {
        private int mMinValue = Integer.MAX_VALUE;
        private int mMaxValue = Integer.MIN_VALUE;

        void include(final int value) {
            if (value != Integer.MAX_VALUE) {
                mMinValue = Math.min(mMinValue, value);
                mMaxValue = Math.max(mMaxValue, value);
            }
        }

        boolean doesNotInclude(final int value) {
            if (value != Integer.MAX_VALUE) {
                return value < mMinValue || value > mMaxValue;
            } else {
                return false;
            }
        }

        @NonNull
        @Override
        public String toString() {
            return String.format(Locale.ENGLISH, "MinMax{min=%d max=%d}",
                    mMinValue, mMaxValue);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "CellMeasurementBounds{rsrp=%s rsrq=%s ta=%s rssi=%s snr=%s}",
                mRsrp, mRsrq, mTa, mRssi, mSnr);
    }
}
