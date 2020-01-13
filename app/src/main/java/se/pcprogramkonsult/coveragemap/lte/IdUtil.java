package se.pcprogramkonsult.coveragemap.lte;

public class IdUtil {
    public static int getENodeB(final int ci) {
        return ci >> 8;
    }

    public static int getCid(final int ci) {
        return ci & 0xFF;
    }

    public static int getCi(final int eNodeB, final int cid) {
        return eNodeB << 8 | cid & 0xFF;
    }

    static int getEarfcnPciPair(final int earfcn, final int pci) {
        return earfcn << 9 | pci & 0x1FF;
    }
}
