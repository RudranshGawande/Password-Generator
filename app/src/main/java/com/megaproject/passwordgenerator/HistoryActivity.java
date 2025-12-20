package com.megaproject.passwordgenerator;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HistoryActivity extends AppCompatActivity implements HistoryAdapter.OnItemActionListener {

    private RecyclerView rvHistory;
    private HistoryAdapter adapter;
    private List<HistoryItem> fullList;
    private EditText etSearch;
    private TextView btnClearAll;
    private HistoryManager historyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        rvHistory = findViewById(R.id.rv_history);
        etSearch = findViewById(R.id.et_search);
        btnClearAll = findViewById(R.id.btn_clear_all);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        // Create Data
        historyManager = new HistoryManager(this);
        loadHistory();

        adapter = new HistoryAdapter(new ArrayList<>(fullList), this);
        rvHistory.setAdapter(adapter);

        // Clear All
        btnClearAll.setOnClickListener(v -> {
            historyManager.clearAll();
            fullList.clear();
            adapter.updateList(fullList);
        });
        // Search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadHistory() {
        fullList = historyManager.getHistory();
        if (fullList == null) fullList = new ArrayList<>();
    }

    private void filter(String query) {
        if (query.isEmpty()) {
            adapter.updateList(new ArrayList<>(fullList));
            return;
        }
        
        String lowerQuery = query.toLowerCase();
        List<HistoryItem> filtered = fullList.stream()
                .filter(item -> {
                    if (item.getType() == HistoryItem.TYPE_HEADER) return true;
                    // Search by Name primarily
                    return item.getName() != null && item.getName().toLowerCase().contains(lowerQuery);
                })
                .collect(Collectors.toList());
        adapter.updateList(filtered);
    }

    @Override
    public void onCopy(String password) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Password", password);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Copied: " + password, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDelete(HistoryItem item) {
        historyManager.deleteItem(item);
        fullList.remove(item);
        adapter.items.remove(item); 
        adapter.notifyDataSetChanged();
        
        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEdit(HistoryItem item) {
        final EditText input = new EditText(this);
        input.setHint("Update Name");
        input.setText(item.getName() != null ? item.getName() : "");
        input.setPadding(32, 32, 32, 32);
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Edit Name")
            .setView(input)
            .setPositiveButton("Save", (dialog, which) -> {
                String newName = input.getText().toString().trim();
                if (newName.isEmpty()) {
                    Toast.makeText(HistoryActivity.this, "Name cannot be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Update item
                item.setName(newName);
                
                // Save updated list
                historyManager.saveHistory(fullList);
                
                // Refresh list (adapter holds reference to fullList array subset if filtered, but let's notify)
                // If filtered, we need to refresh view. 
                // Currently adapter.items is what's displayed.
                adapter.notifyDataSetChanged();
                Toast.makeText(HistoryActivity.this, "Updated", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
