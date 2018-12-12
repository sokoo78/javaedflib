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

    float[] getSignalsTimeFrame(int start, int length) {
        /*
        * gets signals within a specified timeframe.
        * @param start starting second
        * @param length data number to be get from starting point.
        * @return returns a float array with signal data.
        * */
        float[] retSignals=new float[length];
        for (int i=0;i<length; i++) {
            retSignals[i]=signals[start+i];
        }
        return retSignals;
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
        /*
        * adds signal data to existing channel.
        * @param plusSignals float array with signals for add to existing channel
        * */
        float[] newSignals = new float[this.signals.length + plusSignals.length];
        System.arraycopy(this.signals,0,newSignals,0,this.signals.length);

        System.arraycopy(plusSignals, 0, newSignals, this.signals.length + 0, plusSignals.length);
        this.signals=newSignals;
    }

    float sampleFromDigitalToPhysical (float digitalSignal) {
        /*
        * Converts signal to digital to physical value.
        * @param digitalSignal signal in digital value
        * @return returns signal in physical value
        * */
       float value = (float) (this.channelHeader.getPhysicalMinimum()
               + (float) ((this.channelHeader.getPhysicalMaximum() - this.channelHeader.getPhysicalMinimum())
               * (digitalSignal - this.channelHeader.getDigitalMinimum()))
               / (float) ((this.channelHeader.getDigitalMaximum() - this.channelHeader.getDigitalMinimum())));
        return value;
    }

    float sampleFromPhysicalToDigital (float physicalSignal) {
        /*
         * Converts signal to physical to digital value.
         * @param physicalSignal signal in physical value
         * @return returns signal in digital value
         * */

        float value = (float) this.channelHeader.getDigitalMinimum()
                + (((float)(this.channelHeader.getDigitalMaximum()-this.channelHeader.getDigitalMinimum()))
                *(physicalSignal-(float) this.channelHeader.getPhysicalMinimum()))
                /(float) (this.channelHeader.getPhysicalMaximum()-this.channelHeader.getPhysicalMinimum());
        return value;
    }


}
