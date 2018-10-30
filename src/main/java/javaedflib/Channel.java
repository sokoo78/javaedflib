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

}
