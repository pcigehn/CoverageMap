package se.pcprogramkonsult.coveragemap.model;

public interface Handover {
    long getId();

    long getCoord();

    int getSourceENodeB();
    int getSourceCid();
    int getSourceEarfcn();
    int getSourcePci();

    int getSourceRsrp();
    int getSourceRsrq();

    int getTargetENodeB();
    int getTargetCid();
    int getTargetEarfcn();
    int getTargetPci();

    int getTargetRsrp();
    int getTargetRsrq();
}
