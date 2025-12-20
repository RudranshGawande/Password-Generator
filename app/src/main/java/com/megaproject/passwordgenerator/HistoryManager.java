package com.megaproject.passwordgenerator;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class HistoryManager {

    private static final String PREF_NAME = "password_history";
    private static final String KEY_HISTORY_LIST = "history_list";
    private SharedPreferences prefs;
    private Gson gson;

    public HistoryManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public List<HistoryItem> getHistory() {
        String json = prefs.getString(KEY_HISTORY_LIST, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<HistoryItem>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public void addHistoryItem(HistoryItem item) {
        List<HistoryItem> list = getHistory();
        // Add to top (after potential header?)
        // Since we have sections "Today", "Yesterday", managing simple list logic for now.
        // We will just add to index 0 (if no headers logic in storage).
        // Since HistoryItem contains Headers too in the Adapter list, we should probably store "Raw Password Entries" and reconstruct headers in the UI/Adapter.
        // But to keep it robust and simple given the short time:
        // Let's store raw entries only, and the Adapter/Activity can create the display list.
        // But I defined storage to use `HistoryItem` which has Types.
        // It's fine, I'll prepend.
        
        list.add(0, item);
        saveHistory(list);
    }
    
    public void deleteItem(HistoryItem target) {
        List<HistoryItem> list = getHistory();
        // Remove item with matching password & timestamp? IDs would be better.
        // Using object removal rely on equals(). I didn't override equals.
        // Let's iterate and remove match.
        for (int i = 0; i < list.size(); i++) {
            HistoryItem current = list.get(i);
            if (current.getType() == HistoryItem.TYPE_ITEM &&
                current.getPassword().equals(target.getPassword()) &&
                current.getTimestamp().equals(target.getTimestamp())) {
                list.remove(i);
                break;
            }
        }
        saveHistory(list);
    }

    public void clearAll() {
        saveHistory(new ArrayList<>());
    }

    public void saveHistory(List<HistoryItem> list) {
        String json = gson.toJson(list);
        prefs.edit().putString(KEY_HISTORY_LIST, json).apply();
    }
}
