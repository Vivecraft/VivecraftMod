package org.vivecraft.physicalinventory;

public class Semaphore
{
    private boolean locked = true;
    private final Object lock = new Object();
    long timeout = -1L;

    public Semaphore()
    {
    }

    public Semaphore(long timeout)
    {
        this.timeout = timeout;
    }

    public void waitFor()
    {
        synchronized (this.lock)
        {
            if (this.locked)
            {
                Thread thread = null;

                if (this.timeout != -1L)
                {
                    thread = new Thread(new Runnable()
                    {
                        public void run()
                        {
                            try
                            {
                                Thread.sleep(Semaphore.this.timeout);
                                Semaphore.this.wakeUp();
                            }
                            catch (InterruptedException interruptedexception1)
                            {
                            }
                        }
                    });
                    thread.start();
                }

                try
                {
                    this.lock.wait();

                    if (thread != null)
                    {
                        thread.interrupt();
                    }
                }
                catch (InterruptedException interruptedexception)
                {
                }
            }
        }
    }

    public void wakeUp()
    {
        synchronized (this.lock)
        {
            if (this.locked)
            {
                this.locked = false;
                this.lock.notifyAll();
            }
        }
    }

    public void reactivate()
    {
        synchronized (this.lock)
        {
            this.locked = true;
        }
    }

    public boolean isActive()
    {
        return this.locked;
    }

    public long getTimeout()
    {
        synchronized (this.lock)
        {
            return this.timeout;
        }
    }

    public void setTimeout(long timeout)
    {
        synchronized (this.lock)
        {
            this.timeout = timeout;
        }
    }
}
