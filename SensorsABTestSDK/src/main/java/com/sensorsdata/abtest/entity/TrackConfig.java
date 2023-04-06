package com.sensorsdata.abtest.entity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TrackConfig implements Cloneable {
    public boolean itemSwitch = false;
    public boolean triggerSwitch = true;
    public boolean propertySetSwitch = false;
    public Set<String> triggerContentExtension = new HashSet<>(Arrays.asList("abtest_experiment_version", "abtest_experiment_result_id"));
    //NG 相关，本期不错
    public String itemContent;

    @Override
    public String toString() {
        return "TrackConfig{" +
                "itemSwitch=" + itemSwitch +
                ", triggerSwitch=" + triggerSwitch +
                ", propertySetSwitch=" + propertySetSwitch +
                ", triggerContentExtension=" + triggerContentExtension +
                ", itemContent='" + itemContent + '\'' +
                '}';
    }

    @Override
    public TrackConfig clone() {
        TrackConfig clone = new TrackConfig();
        clone.propertySetSwitch = this.propertySetSwitch;
        clone.itemSwitch = this.itemSwitch;
        clone.triggerSwitch = this.triggerSwitch;
        clone.itemContent = itemContent;
        clone.triggerContentExtension = new HashSet<>(triggerContentExtension);
        return clone;
    }
}
