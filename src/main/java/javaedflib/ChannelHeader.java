package javaedflib;

public class ChannelHeader {

    /** EDF/BDF Labels of the channels (16 bytes)
        label has a 'type' 'space' 'sensor specification' format. */
    private String LabelOfChannel;
    /** EDF/BDF Transducer type (80 bytes) */
    private String transducerType;
    /** EDF/BDF  Physical dimension of channels (e.g uV, Ohm, etc) */
    private String physicalDimension;
    /** EDF/BDF Physical minimum in units of physical dimension (8 bytes) (BDF: -262144, EDF: -32768) */
    private double physicalMinimum;
    /** EDF/BDF Physical maximum in units of physical dimension (8 bytes) (BDF: 262143, EDF: 32767) */
    private double physicalMaximum;
    /** EDF/BDF Digital minimum (8 bytes) BDF: "-8388608" (ASCII), EDF: "-32768" (ASCII)*/
    private int digitalMinimum;
    /** EDF/BDF Digital maximum (8 bytes) BDF: "8388607" (ASCII), EDF: "32767" (ASCII)*/
    private int digitalMaximum;
    /** EDF/BDF Pre-filtering description (80 bytes), e.g. BDF: "HP:DC; LP:410", EDF: "HP:0,16; LP:500" */
    private String preFilteringInfo;
    /** EDF/BDF  Number of samples in each data record. (Sample-rate if Duration of data record = "1") */
    private long numberOfSamples;
    /** EDF/BDF Reserved (32 bytes) */
    private String reserved;

    public String getLabelOfChannel() {
        return LabelOfChannel;
    }

    public void setLabelOfChannel(String labelOfChannel) {
        LabelOfChannel = labelOfChannel;
    }

    public String getTransducerType() {
        return transducerType;
    }

    public void setTransducerType(String transducerType) {
        this.transducerType = transducerType;
    }

    public String getPhysicalDimension() {
        return physicalDimension;
    }

    public void setPhysicalDimension(String physicalDimension) {
        this.physicalDimension = physicalDimension;
    }

    public double getPhysicalMinimum() {
        return physicalMinimum;
    }

    public void setPhysicalMinimum(double physicalMinimum) {
        this.physicalMinimum = physicalMinimum;
    }

    public double getPhysicalMaximum() {
        return physicalMaximum;
    }

    public void setPhysicalMaximum(double physicalMaximum) {
        this.physicalMaximum = physicalMaximum;
    }

    public int getDigitalMinimum() {
        return digitalMinimum;
    }

    public void setDigitalMinimum(int digitalMinimum) {
        this.digitalMinimum = digitalMinimum;
    }

    public int getDigitalMaximum() {
        return digitalMaximum;
    }

    public void setDigitalMaximum(int digitalMaximum) {
        this.digitalMaximum = digitalMaximum;
    }

    public String getPreFilteringInfo() {
        return preFilteringInfo;
    }

    public void setPreFilteringInfo(String preFilteringInfo) {
        this.preFilteringInfo = preFilteringInfo;
    }

    public long getNumberOfSamples() {
        return numberOfSamples;
    }

    public void setNumberOfSamples(long numberOfSamples) {
        this.numberOfSamples = numberOfSamples;
    }

    public String getReserved() {
        return reserved;
    }

    public void setReserved(String reserved) {
        this.reserved = reserved;
    }

}
