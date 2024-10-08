/**
 * Created by Giovanni Accetta on 04/08/15.
 * Copyright (c) 2015 Dott. Ing. Giovanni Accetta. All rights reserved.
 */

package it.mm.supportlibrary.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class DispatchQueue extends Thread {
    public volatile Handler handler = null;
    private final Object handlerSyncObject = new Object();

    public DispatchQueue(final String threadName) {
        setName(threadName);
        start();
    }

    private void sendMessage(Message msg, int delay) {
        if (handler == null) {
            try {
                synchronized (handlerSyncObject) {
                    handlerSyncObject.wait();
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        if (handler != null) {
            if (delay <= 0) {
                handler.sendMessage(msg);
            } else {
                handler.sendMessageDelayed(msg, delay);
            }
        }
    }

    public void cancelRunnable(Runnable runnable) {
        if (handler == null) {
            synchronized (handlerSyncObject) {
                if (handler == null) {
                    try {
                        handlerSyncObject.wait();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        }

        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }

    public void postRunnable(Runnable runnable) {
        postRunnable(runnable, 0);
    }

    public void postRunnable(Runnable runnable, long delay) {
        if (handler == null) {
            synchronized (handlerSyncObject) {
                if (handler == null) {
                    try {
                        handlerSyncObject.wait();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        }

        if (handler != null) {
            if (delay <= 0) {
                handler.post(runnable);
            } else {
                handler.postDelayed(runnable, delay);
            }
        }
    }

    public void cleanupQueue() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    public void run() {
        Looper.prepare();
        synchronized (handlerSyncObject) {
            handler = new Handler();
            handlerSyncObject.notify();
        }
        Looper.loop();
    }
}
