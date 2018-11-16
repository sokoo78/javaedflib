package javaedflib;

class ChannelHeader {

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
    private int numberOfSamples;
    /** EDF/BDF Reserved (32 bytes) */
    private String reserved;


    String getLabelOfChannel() {
        return LabelOfChannel;
    }

    void setLabelOfChannel(String labelOfChannel) {
        LabelOfChannel = labelOfChannel;
    }

    String getTransducerType() {
        return transducerType;
    }

    void setTransducerType(String transducerType) {
        this.transducerType = transducerType;
    }

    String getPhysicalDimension() {
        return physicalDimension;
    }

    void setPhysicalDimension(String physicalDimension) {
        this.physicalDimension = physicalDimension;
    }

    double getPhysicalMinimum() {
        return physicalMinimum;
    }

    void setPhysicalMinimum(double physicalMinimum) {
        this.physicalMinimum = physicalMinimum;
    }

    double getPhysicalMaximum() {
        return physicalMaximum;
    }

    void setPhysicalMaximum(double physicalMaximum) {
        this.physicalMaximum = physicalMaximum;
    }

    int getDigitalMinimum() {
        return digitalMinimum;
    }

    void setDigitalMinimum(int digitalMinimum) {
        this.digitalMinimum = digitalMinimum;
    }

    int getDigitalMaximum() {
        return digitalMaximum;
    }

    void setDigitalMaximum(int digitalMaximum) {
        this.digitalMaximum = digitalMaximum;
    }

    String getPreFilteringInfo() {
        return preFilteringInfo;
    }

    void setPreFilteringInfo(String preFilteringInfo) {
        this.preFilteringInfo = preFilteringInfo;
    }

    int getNumberOfSamples() {
        return numberOfSamples;
    }

    void setNumberOfSamples(int numberOfSamples) {
        this.numberOfSamples = numberOfSamples;
    }

    String getReserved() {
        return reserved;
    }

    void setReserved(String reserved) {
        this.reserved = reserved;
    }

}
