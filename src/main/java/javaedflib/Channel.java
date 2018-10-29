package javaedflib;

import java.util.TreeMap;

class Channel {
    private ChannelHeader channelHeader;
    int startTimeSlot;
    int endTimeSlot;
    float[] signals;
    int[] trigger;
    TreeMap<Integer,Float[]> ChannelData;

    Channel(ChannelHeader channelHeader) {
        this.channelHeader = channelHeader;
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

    public int getStartTimeSlot() {
        return startTimeSlot;
    }

    public void setStartTimeSlot(int startTimeSlot) {
        this.startTimeSlot = startTimeSlot;
    }

    public int getEndTimeSlot() {
        return this.endTimeSlot;
    }

    public void setEndTimeSlot(int endTimeSlot) {
        this.endTimeSlot=endTimeSlot;
    }

    public float[] getSignals() {
        return signals;
    }

    public void setSignals(float[] signals) {
        this.signals = signals;
    }

    public int[] getTrigger() {
        return trigger;
    }

    public void setTrigger(int[] trigger) {
        this.trigger = trigger;
    }


}
