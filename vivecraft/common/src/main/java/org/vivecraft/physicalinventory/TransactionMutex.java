package org.vivecraft.physicalinventory;

public class TransactionMutex
{
    public boolean approved;
    public Thread watchdog;
    public boolean consumed;
}
