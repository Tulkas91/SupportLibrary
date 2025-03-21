/*
 * This is the source code of Telegram for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package it.mm.supportlibrary.core;

import java.util.ArrayList;
import java.util.HashMap;

public class NotificationCenter {

    public static int totalEvents = 1;
    
    public static final int networkChanged = totalEvents++;
    public static final int receiveSMS = totalEvents++;
    public static final int updatedDB = totalEvents++;
    public static final int locationChanged = totalEvents++;
    public static final int districtChanged = totalEvents++;
    public static final int openDialogManager = totalEvents++;
    public static final int moveTaskToFront = totalEvents++;
    public static final int receivePushNotification = totalEvents++;
    public static final int messageChange = totalEvents++;
    public static final int updateMenuFragment = totalEvents++;
    public static final int startCamera = totalEvents++;
    public static final int biometricAuthenticationSuccess = totalEvents++;
    public static final int enableButtonManagerClock = totalEvents++;
    public static final int setBeaconOffice = totalEvents++;
    public static final int actionSelected = totalEvents++;

    final private HashMap<Integer, ArrayList<Object>> observers = new HashMap<Integer, ArrayList<Object>>();

    final private HashMap<Integer, Object> removeAfterBroadcast = new HashMap<Integer, Object>();
    final private HashMap<Integer, Object> addAfterBroadcast = new HashMap<Integer, Object>();

    private int broadcasting = 0;

    private static volatile NotificationCenter Instance = null;

    public static NotificationCenter getInstance() {
        NotificationCenter localInstance = Instance;
        if (localInstance == null) {
            synchronized (NotificationCenter.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new NotificationCenter();
                }
            }
        }
        return localInstance;
    }

    public interface NotificationCenterDelegate {
        void didReceivedNotification(int id, Object... args);
    }

    public void postNotificationName(int id, Object... args) {
        synchronized (observers) {
            broadcasting++;
            ArrayList<Object> objects = observers.get(id);
            if (objects != null) {
                for (Object obj : objects) {
                    ((NotificationCenterDelegate) obj).didReceivedNotification(id, args);
                }
            }
            broadcasting--;
            if (broadcasting == 0) {
                if (!removeAfterBroadcast.isEmpty()) {
                    for (HashMap.Entry<Integer, Object> entry : removeAfterBroadcast.entrySet()) {
                        removeObserver(entry.getValue(), entry.getKey());
                    }
                    removeAfterBroadcast.clear();
                }
                if (!addAfterBroadcast.isEmpty()) {
                    for (HashMap.Entry<Integer, Object> entry : addAfterBroadcast.entrySet()) {
                        addObserver(entry.getValue(), entry.getKey());
                    }
                    addAfterBroadcast.clear();
                }
            }
        }
    }

    public void addObserver(Object observer, int id) {
        synchronized (observers) {
            if (broadcasting != 0) {
                addAfterBroadcast.put(id, observer);
                return;
            }
            ArrayList<Object> objects = observers.get(id);
            if (objects == null) {
                observers.put(id, (objects = new ArrayList<Object>()));
            }
            if (objects.contains(observer)) {
                return;
            }
            objects.add(observer);
        }
    }

    public void removeObserver(Object observer, int id) {
        synchronized (observers) {
            if (broadcasting != 0) {
                removeAfterBroadcast.put(id, observer);
                return;
            }
            ArrayList<Object> objects = observers.get(id);
            if (objects != null) {
                objects.remove(observer);
                if (objects.size() == 0) {
                    observers.remove(id);
                }
            }
        }
    }
}
