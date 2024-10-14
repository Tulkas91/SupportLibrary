package it.mm.supportlibrary.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.HashMap;

public class Account {

    private final Object sync = new Object();

    private String authToken;

    private HashMap<Integer, Long> avatarUpdatesTiny;
    private HashMap<Integer, Long> avatarUpdatesSmall;
    private HashMap<Integer, Long> avatarUpdatesMedium;
    private HashMap<Integer, Long> avatarUpdatesOriginal;

    private MutableLiveData<String> authTokenLiveData = new MutableLiveData<>();
    private MutableLiveData<HashMap<Integer, Long>> avatarUpdatesSmallLiveData = new MutableLiveData<>();
    private MutableLiveData<HashMap<Integer, Long>> avatarUpdatesOriginalLiveData = new MutableLiveData<>();

    public Account() {
        avatarUpdatesTiny = new HashMap<>();
        avatarUpdatesSmall = new HashMap<>();
        avatarUpdatesMedium = new HashMap<>();
        avatarUpdatesOriginal = new HashMap<>();

        authTokenLiveData.setValue(null);
        avatarUpdatesSmallLiveData.setValue(avatarUpdatesSmall);
        avatarUpdatesOriginalLiveData.setValue(avatarUpdatesOriginal);
    }

    public void logout() {
        authToken = null;
        authTokenLiveData.setValue(null);  // Notifica i cambiamenti
    }

    public LiveData<HashMap<Integer, Long>> getAvatarUpdatesSmall() {
        return avatarUpdatesSmallLiveData;
    }

    public LiveData<HashMap<Integer, Long>> getAvatarUpdatesOriginal() {
        return avatarUpdatesOriginalLiveData;
    }

    public String getAccessToken() {
        synchronized (sync) {
            return authToken;
        }
    }

    public LiveData<String> getAuthToken() {
        return authTokenLiveData;
    }

    public void setAuthToken(String authToken) {
        synchronized (sync) {
            if (this.authToken == null || !this.authToken.equals(authToken)) {
                this.authToken = authToken;
                authTokenLiveData.setValue(authToken);  // Notifica i cambiamenti
            }
        }
    }
}
