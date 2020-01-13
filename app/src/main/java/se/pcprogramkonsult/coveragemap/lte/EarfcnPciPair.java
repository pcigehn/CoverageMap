package se.pcprogramkonsult.coveragemap.lte;

import androidx.annotation.NonNull;

import java.util.Locale;

public class EarfcnPciPair {
    private final int mEarfcn;
    private final int mPci;

    public EarfcnPciPair(final int earfcn, final int pci) {
        mEarfcn = earfcn;
        mPci = pci;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof EarfcnPciPair) {
            EarfcnPciPair otherPair = (EarfcnPciPair) other;
            return mEarfcn == otherPair.mEarfcn && mPci == otherPair.mPci;
        }
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return IdUtil.getEarfcnPciPair(mEarfcn, mPci);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "EarfcnPciPair{earfcn=%d pci=%d}", mEarfcn, mPci);
    }

}
