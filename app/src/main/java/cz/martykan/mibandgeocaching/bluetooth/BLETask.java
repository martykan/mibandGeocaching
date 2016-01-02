package cz.martykan.mibandgeocaching.bluetooth;


import java.util.List;

public class BLETask
{
    private final List<BLEAction> actions;

    public BLETask(final List<BLEAction> actions)
    {
        this.actions = actions;
    }

    public List<BLEAction> getActions()
    {
        return actions;
    }
}