package cz.martykan.mibandgeocaching.bluetooth;


public class WaitAction implements BLEAction
{
    private final long duration;

    public WaitAction(final long duration)
    {
        this.duration = duration;
    }

    public void run()
    {
        threadWait(duration);
    }

    private void threadWait(final long duration)
    {
        try
        {
            Thread.sleep(duration);
        }
        catch(InterruptedException e)
        {
            threadWait(duration);
        }
    }
}