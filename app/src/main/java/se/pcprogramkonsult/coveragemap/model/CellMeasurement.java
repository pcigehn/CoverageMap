package se.pcprogramkonsult.coveragemap.model;

public interface CellMeasurement {
    long getId();
    long getLocationId();

    boolean isRegistered();

    int getENodeB();
    int getCid();
    int getEarfcn();
    int getPci();
    int getTac();

    int getRsrp();
    int getRsrq();
    int getTa();

    int getRssi();

    int getSnr();
}
