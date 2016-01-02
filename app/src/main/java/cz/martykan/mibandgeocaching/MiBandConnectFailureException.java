package cz.martykan.mibandgeocaching;

public class MiBandConnectFailureException extends Exception
{
    public MiBandConnectFailureException(String detailMessage)
    {
        super(detailMessage);
    }
}