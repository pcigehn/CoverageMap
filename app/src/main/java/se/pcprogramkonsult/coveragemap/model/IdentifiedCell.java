package se.pcprogramkonsult.coveragemap.model;

public interface IdentifiedCell {
    int getENodeB();
    int getCid();
    int getPci();
    int getEarfcn();
    double getNeLat();
    double getNeLon();
    double getSwLat();
    double getSwLon();
    double getExtNeLat();
    double getExtNeLon();
    double getExtSwLat();
    double getExtSwLon();
}
