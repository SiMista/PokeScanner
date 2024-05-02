package com.example.pokescanner.ui.settings;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

import com.example.pokescanner.R;

import java.util.Locale;

public class SettingsFragment extends Fragment {
    private String selectedLanguageCode = "en"; // Langue par défaut

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Spinner languageSpinner = view.findViewById(R.id.language_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.language_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                changeLanguage(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Button applyButton = view.findViewById(R.id.apply_button);
        applyButton.setOnClickListener(v -> applyLanguageChange());
        return view;
    }

    private void changeLanguage(int position) {
        switch (position) {
            case 0:
                selectedLanguageCode = "en"; // English
                break;
            case 1:
                selectedLanguageCode = "fr"; // French
                break;
        }
        Locale locale = new Locale(selectedLanguageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getContext().getResources().updateConfiguration(config, getContext().getResources().getDisplayMetrics());
    }

    private void applyLanguageChange() {
        Locale locale = new Locale(selectedLanguageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getContext().getResources().updateConfiguration(config, getContext().getResources().getDisplayMetrics());
        getActivity().recreate(); // Recreate the activity to apply the language change.
    }
}
