package javaedflib;

class Channel {
    private ChannelHeader channelHeader;
    private int timeSlotOffset;
    private float[] signals;
    private int[] trigger;

    Channel(ChannelHeader channelHeader, int timeSlotOffset) {
        this.channelHeader = channelHeader;
        this.timeSlotOffset = timeSlotOffset;
    }

    String getName() {
        return channelHeader.getLabelOfChannel();
    }

    int getNumberOfSamples() {
        return channelHeader.getNumberOfSamples();
    }

    ChannelHeader getChannelHeader() {
        return channelHeader;
    }

    float[] getSignals() {
        return signals;
    }

    void setSignals(float[] signals) {
        this.signals = signals;
    }

    int[] getTrigger() {
        return trigger;
    }

    void setTrigger(int[] trigger) {
        this.trigger = trigger;
    }

    int getTimeSlotOffset() {
        return this.timeSlotOffset;
    }

    void addSignals(float[] plusSignals) {
        float[] newSignals = new float[this.signals.length + plusSignals.length];
        System.arraycopy(this.signals,0,newSignals,0,this.signals.length);

        System.arraycopy(plusSignals, 0, newSignals, this.signals.length + 0, plusSignals.length);
        this.signals=newSignals;
    }

    float sampleFromDigitalToPhysical (float digitalSignal) {
       float value = (float) (this.channelHeader.getPhysicalMinimum()
               + (float) ((this.channelHeader.getPhysicalMaximum() - this.channelHeader.getPhysicalMinimum())
               * (digitalSignal - this.channelHeader.getDigitalMinimum()))
               / (float) ((this.channelHeader.getDigitalMaximum() - this.channelHeader.getDigitalMinimum())));
        return value;
    }

    float sampleFromPhysicalToDigital (float physicalSignal) {
        float value = (float) this.channelHeader.getDigitalMinimum()
                + (((float)(this.channelHeader.getDigitalMaximum()-this.channelHeader.getDigitalMinimum()))
                *(physicalSignal-(float) this.channelHeader.getPhysicalMinimum()))
                /(float) (this.channelHeader.getPhysicalMaximum()-this.channelHeader.getPhysicalMinimum());
        return value;
    }

    byte[] digitalSignalToBytes (float value, int signalByteSize){
        String hexData;
        byte[] backBytes=new byte[signalByteSize];
        hexData=Integer.toString((int)value,16);
        switch (signalByteSize) {
            case 2:
                hexData = "0000".substring(hexData.length()) + hexData;
                break;
            case 3:
                hexData = "000000".substring(hexData.length()) + hexData;
                break;
            default :
                hexData = "0000".substring(hexData.length()) + hexData;
                break;
        }
        for (int b=0;b<signalByteSize ;b++) {
            backBytes[b]=Byte.valueOf(hexData.substring(hexData.length()- signalByteSize));
            hexData=hexData.substring(0,signalByteSize);
        }
        return backBytes;
    }
}
