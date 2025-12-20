package com.megaproject.passwordgenerator;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText tvPassword;
    private TextView tvStrengthValue;
    private TextView tvLengthValue;
    private SeekBar sliderLength;
    private SwitchCompat switchUppercase, switchLowercase, switchNumbers, switchSymbols;
    private View bar1, bar2, bar3, bar4;
    private Button btnGenerate;
    private ImageButton btnCopy, btnHistory, btnTheme, btnSave;
    private HistoryManager historyManager;

    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String CHAR_DIGITS = "0123456789";
    private static final String CHAR_SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,.<>?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        
        setContentView(R.layout.activity_main);
        
        historyManager = new HistoryManager(this);

        tvPassword = findViewById(R.id.tv_password);
        tvStrengthValue = findViewById(R.id.tv_strength_value);
        tvLengthValue = findViewById(R.id.tv_length_value);
        sliderLength = findViewById(R.id.slider_length);
        switchUppercase = findViewById(R.id.switch_uppercase);
        switchLowercase = findViewById(R.id.switch_lowercase);
        switchNumbers = findViewById(R.id.switch_numbers);
        switchSymbols = findViewById(R.id.switch_symbols);
        bar1 = findViewById(R.id.bar1);
        bar2 = findViewById(R.id.bar2);
        bar3 = findViewById(R.id.bar3);
        bar4 = findViewById(R.id.bar4);
        btnGenerate = findViewById(R.id.btn_generate);
        btnCopy = findViewById(R.id.btn_copy);
        btnSave = findViewById(R.id.btn_save);
        btnHistory = findViewById(R.id.btn_history);
        btnTheme = findViewById(R.id.btn_theme_toggle);
        
        btnTheme.setOnClickListener(v -> {
            int currentMode = androidx.appcompat.app.AppCompatDelegate.getDefaultNightMode();
            int newMode = (currentMode == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES) ?
                          androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO :
                          androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;
            
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(newMode);
        });

        btnHistory.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        // Initial setup
        updateLengthLabel(sliderLength.getProgress() + 4);
        
        sliderLength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = progress + 4;
                updateLengthLabel(value);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        View.OnClickListener switchListener = v -> {
            if (!switchUppercase.isChecked() && !switchLowercase.isChecked() && 
                !switchNumbers.isChecked() && !switchSymbols.isChecked()) {
                ((SwitchCompat) v).setChecked(true);
            }
        };
        switchUppercase.setOnClickListener(switchListener);
        switchLowercase.setOnClickListener(switchListener);
        switchNumbers.setOnClickListener(switchListener);
        switchSymbols.setOnClickListener(switchListener);

        btnGenerate.setOnClickListener(v -> generatePassword(false));

        btnCopy.setOnClickListener(v -> {
            String password = tvPassword.getText().toString();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Generated Password", password);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, R.string.copy_success_message, Toast.LENGTH_SHORT).show();
        });
        
        btnSave.setOnClickListener(v -> showSaveDialog());

        generatePassword(false);
    }

    private void updateLengthLabel(int length) {
        tvLengthValue.setText(String.valueOf(length));
    }

    private void showSaveDialog() {
        android.view.LayoutInflater inflater = android.view.LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_save_password, null);
        
        final EditText etName = view.findViewById(R.id.et_name);
        Button btnSave = view.findViewById(R.id.btn_save);
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(true)
            .create();
            
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(MainActivity.this, "Name is required!", Toast.LENGTH_SHORT).show();
                etName.setError("Required");
                return;
            }
            savePassword(name);
            dialog.dismiss();
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }

    private void savePassword(String name) {
         String password = tvPassword.getText().toString();
         HistoryItem item = new HistoryItem(
             name,
             password, 
             "Just now", 
             R.drawable.ic_shield, 
             R.color.history_icon_green_bg, 
             R.color.history_icon_green
         );
         historyManager.addHistoryItem(item);
         Toast.makeText(this, "Saved " + name, Toast.LENGTH_SHORT).show();
    }

    private void generatePassword(boolean ignored) {
        int length = sliderLength.getProgress() + 4;
        
        boolean useUpper = switchUppercase.isChecked();
        boolean useLower = switchLowercase.isChecked();
        boolean useDigits = switchNumbers.isChecked();
        boolean useSymbols = switchSymbols.isChecked();

        StringBuilder charPool = new StringBuilder();
        List<String> requiredChars = new ArrayList<>();

        if (useUpper) {
            charPool.append(CHAR_UPPER);
            requiredChars.add(CHAR_UPPER);
        }
        if (useLower) {
            charPool.append(CHAR_LOWER);
            requiredChars.add(CHAR_LOWER);
        }
        if (useDigits) {
            charPool.append(CHAR_DIGITS);
            requiredChars.add(CHAR_DIGITS);
        }
        if (useSymbols) {
            charPool.append(CHAR_SYMBOLS);
            requiredChars.add(CHAR_SYMBOLS);
        }

        if (charPool.length() == 0) return;

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(length);

        for (String type : requiredChars) {
            if (password.length() < length) {
                password.append(type.charAt(random.nextInt(type.length())));
            }
        }

        while (password.length() < length) {
            password.append(charPool.charAt(random.nextInt(charPool.length())));
        }

        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            char a = passwordArray[index];
            passwordArray[index] = passwordArray[i];
            passwordArray[i] = a;
        }

        String finalPassword = new String(passwordArray);
        tvPassword.setText(finalPassword);
        updateStrength(length, useUpper, useLower, useDigits, useSymbols);
    }

    private void updateStrength(int length, boolean upper, boolean lower, boolean digits, boolean symbols) {
        int varietyCount = (upper ? 1 : 0) + (lower ? 1 : 0) + (digits ? 1 : 0) + (symbols ? 1 : 0);
        int score = 0;
        
        if (length >= 8) score++;
        if (length >= 12) score++;
        if (varietyCount >= 2) score++;
        if (varietyCount >= 3) score++;
        
        String strengthText;
        int activeBars;
        int colorRes = R.color.strength_filled;

        if (length < 8 || varietyCount < 2) {
            strengthText = "Weak";
            activeBars = 1;
            colorRes = android.R.color.holo_red_light;
        } else if (length < 10 || varietyCount < 3) {
            strengthText = "Fair";
            activeBars = 2;
            colorRes = android.R.color.holo_orange_light;
        } else if (length < 12) {
            strengthText = "Strong";
            activeBars = 3;
            colorRes = R.color.strength_filled;
        } else {
            strengthText = "Very Strong";
            activeBars = 3; 
            if (length > 16) activeBars = 4;
            colorRes = R.color.strength_filled;
        }

        tvStrengthValue.setText(strengthText);
        
        int color = ContextCompat.getColor(this, colorRes);
        tvStrengthValue.setTextColor(color);

        int emptyColor = ContextCompat.getColor(this, R.color.strength_empty);

        setBarState(bar1, activeBars >= 1, color, emptyColor);
        setBarState(bar2, activeBars >= 2, color, emptyColor);
        setBarState(bar3, activeBars >= 3, color, emptyColor);
        setBarState(bar4, activeBars >= 4, color, emptyColor);
    }

    private void setBarState(View bar, boolean active, int color, int emptyColor) {
        if (active) {
            bar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
        } else {
            bar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(emptyColor));
        }
    }
}