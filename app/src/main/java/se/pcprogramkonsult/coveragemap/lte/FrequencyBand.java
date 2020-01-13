package se.pcprogramkonsult.coveragemap.lte;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FrequencyBand implements Comparable<FrequencyBand> {
    private static final List<FrequencyBand> frequencyBands = new ArrayList<>();

    static {
        frequencyBands.add(new FrequencyBand(1,   0,      599,    2110.0f));
        frequencyBands.add(new FrequencyBand(2,   600,    1199,   1930.0f));
        frequencyBands.add(new FrequencyBand(3,   1200,   1949,   1805.0f));
        frequencyBands.add(new FrequencyBand(4,   1950,   2399,   2110.0f));
        frequencyBands.add(new FrequencyBand(5,   2400,   2649,   869.0f));
        frequencyBands.add(new FrequencyBand(6,   2650,   2749,   875.0f));
        frequencyBands.add(new FrequencyBand(7,   2750,   3449,   2620.0f));
        frequencyBands.add(new FrequencyBand(8,   3450,   3799,   925.0f));
        frequencyBands.add(new FrequencyBand(9,   3800,   4149,   1844.9f));
        frequencyBands.add(new FrequencyBand(10,  4150,   4749,   2110.0f));
        frequencyBands.add(new FrequencyBand(11,  4750,   4949,   1475.9f));
        frequencyBands.add(new FrequencyBand(12,  5010,   5179,   729.0f));
        frequencyBands.add(new FrequencyBand(13,  5180,   5279,   746.0f));
        frequencyBands.add(new FrequencyBand(14,  5280,   5379,   758.0f));
        frequencyBands.add(new FrequencyBand(17,  5730,   5849,   734.0f));
        frequencyBands.add(new FrequencyBand(18,  5850,   5999,   860.0f));
        frequencyBands.add(new FrequencyBand(19,  6000,   6149,   875.0f));
        frequencyBands.add(new FrequencyBand(20,  6150,   6449,   791.0f));
        frequencyBands.add(new FrequencyBand(21,  6450,   6599,   1495.9f));
        frequencyBands.add(new FrequencyBand(22,  6600,   7399,   3510.0f));
        frequencyBands.add(new FrequencyBand(23,  7500,   7699,   2180.0f));
        frequencyBands.add(new FrequencyBand(24,  7700,   8039,   1525.0f));
        frequencyBands.add(new FrequencyBand(25,  8040,   8689,   1930.0f));
        frequencyBands.add(new FrequencyBand(26,  8690,   9039,   859.0f));
        frequencyBands.add(new FrequencyBand(27,  9040,   9209,   852.0f));
        frequencyBands.add(new FrequencyBand(28,  9210,   9659,   758.0f));
        frequencyBands.add(new FrequencyBand(29,  9660,   9769,   717.0f));
        frequencyBands.add(new FrequencyBand(30,  9770,   9869,   2350.0f));
        frequencyBands.add(new FrequencyBand(31,  9870,   9919,   462.5f));
        frequencyBands.add(new FrequencyBand(32,  9920,   10359,  1452.0f));
        frequencyBands.add(new FrequencyBand(33,  36000,  36199,  1900.0f));
        frequencyBands.add(new FrequencyBand(34,  26200,  36349,  2010.0f));
        frequencyBands.add(new FrequencyBand(35,  36350,  36949,  1850.0f));
        frequencyBands.add(new FrequencyBand(36,  36950,  37549,  1930.0f));
        frequencyBands.add(new FrequencyBand(37,  37550,  37749,  1910.0f));
        frequencyBands.add(new FrequencyBand(38,  37750,  38249,  2570.0f));
        frequencyBands.add(new FrequencyBand(39,  38250,  38649,  1880.0f));
        frequencyBands.add(new FrequencyBand(40,  38650,  39649,  2300.0f));
        frequencyBands.add(new FrequencyBand(41,  39650,  41589,  2496.0f));
        frequencyBands.add(new FrequencyBand(42,  41590,  43589,  3400.0f));
        frequencyBands.add(new FrequencyBand(43,  43590,  44589,  3600.0f));
        frequencyBands.add(new FrequencyBand(44,  45590,  46589,  703.0f));
        frequencyBands.add(new FrequencyBand(45,  46590,  46789,  1447.0f));
        frequencyBands.add(new FrequencyBand(46,  46790,  54539,  5150.0f));
        frequencyBands.add(new FrequencyBand(47,  54540,  55239,  5855.0f));
        frequencyBands.add(new FrequencyBand(48,  55240,  56739,  3550.0f));
        frequencyBands.add(new FrequencyBand(49,  56740,  58239,  3550.0f));
        frequencyBands.add(new FrequencyBand(50,  58240,  59089,  1432.0f));
        frequencyBands.add(new FrequencyBand(51,  59090,  59139,  1427.0f));
        frequencyBands.add(new FrequencyBand(52,  59140,  60139,  3300.0f));
        frequencyBands.add(new FrequencyBand(65,  65536,  66435,  2110.0f));
        frequencyBands.add(new FrequencyBand(66,  66436,  67335,  2110.0f));
        frequencyBands.add(new FrequencyBand(67,  67336,  67535,  738.0f));
        frequencyBands.add(new FrequencyBand(68,  67536,  67835,  753.0f));
        frequencyBands.add(new FrequencyBand(69,  67836,  68335,  2570.0f));
        frequencyBands.add(new FrequencyBand(70,  68336,  68585,  1995.0f));
        frequencyBands.add(new FrequencyBand(71,  68586,  68935,  617.0f));
        frequencyBands.add(new FrequencyBand(72,  68936,  68985,  461.0f));
        frequencyBands.add(new FrequencyBand(73,  68986,  69035,  460.0f));
        frequencyBands.add(new FrequencyBand(74,  69036,  69465,  1475.0f));
        frequencyBands.add(new FrequencyBand(75,  69466,  70315,  1432.0f));
        frequencyBands.add(new FrequencyBand(76,  70316,  70365,  1427.0f));
        frequencyBands.add(new FrequencyBand(85,  70366,  70545,  728.0f));
        frequencyBands.add(new FrequencyBand(252, 255144, 256143, 5150.0f));
        frequencyBands.add(new FrequencyBand(255, 260894, 262143, 5725.0f));
    }

    private final int mBand;
    private final int mEarfcnLow;
    private final int mEarfcnHigh;
    private final float mFreqLow;

    private FrequencyBand(final int band, final int earfcnLow, final int earfcnHigh, final float freqLow) {
        mBand = band;
        mEarfcnLow = earfcnLow;
        mEarfcnHigh = earfcnHigh;
        mFreqLow = freqLow;
    }

    @Nullable
    static FrequencyBand getBand(final int earfcn) {
        for (FrequencyBand frequencyBand : frequencyBands) {
            if (frequencyBand.isInRange(earfcn)) {
                return frequencyBand;
            }
        }
        return null;
    }

    float calculateFrequency(final int earfcn) {
        if (isInRange(earfcn)) {
            return mFreqLow + 0.1f * (earfcn - mEarfcnLow);
        }
        return -1.0f;
    }

    private boolean isInRange(final int earfcn) {
        return earfcn >= mEarfcnLow && earfcn <= mEarfcnHigh;
    }

    @Override
    public int compareTo(@NonNull final FrequencyBand other) {
        if (mBand == other.mBand) {
            return 0;
        } else if (mFreqLow < other.mFreqLow) {
            return -1;
        } else {
            return 1;
        }
    }
}
