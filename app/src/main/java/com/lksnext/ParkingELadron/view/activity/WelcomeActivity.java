package com.lksnext.ParkingELadron.view.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.lksnext.ParkingELadron.R;
import com.lksnext.ParkingELadron.databinding.ActivityWelcomeBinding;
import com.lksnext.ParkingELadron.domain.LanguageItem;
import com.lksnext.ParkingELadron.view.adapters.LanguageSpinnerAdapter;
import com.lksnext.ParkingELadron.viewmodel.WelcomeViewModel;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class WelcomeActivity extends BaseActivity{

    private ActivityWelcomeBinding binding;
    private WelcomeViewModel welcomeViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar el ViewModel
        welcomeViewModel = new ViewModelProvider(this).get(WelcomeViewModel.class);

        // Verificar si la base de datos está inicializada
        checkDatabaseInitialization();

        // Configurar botones
        binding.btnCreate.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });

        binding.btnEnter.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });

        // Comprobar si el usuario ya está registrado
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish(); // Cerrar WelcomeActivity
        }

        List<LanguageItem> languages = Arrays.asList(
                new LanguageItem("es", "Español", R.drawable.ic_flag_es),
                new LanguageItem("en", "English", R.drawable.ic_flag_en),
                new LanguageItem("eu", "Euskara", R.drawable.ic_flag_eu)
        );

        LanguageSpinnerAdapter adapter = new LanguageSpinnerAdapter(this, languages);
        Spinner spinner = findViewById(R.id.spinnerLanguage);
        spinner.setAdapter(adapter);

        String lang = getSharedPreferences("settings", MODE_PRIVATE).getString("lang", "es");
        for (int i = 0; i < languages.size(); i++) {
            if (languages.get(i).getCode().equals(lang)) {
                spinner.setSelection(i);
                break;
            }
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLang = languages.get(position).getCode();
                SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
                if (!prefs.getString("lang", "es").equals(selectedLang)) {
                    prefs.edit().putString("lang", selectedLang).apply();
                    recreate();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void checkDatabaseInitialization() {
        welcomeViewModel.isDatabaseInitialized().observe(this, isInitialized -> {
            if (isInitialized != null && isInitialized) {
                Log.d("WelcomeActivity", "La base de datos ya está inicializada.");
            } else {
                Log.d("WelcomeActivity", "La base de datos no está inicializada. Inicializando...");
                welcomeViewModel.initializeDatabase();
            }
        });
    }

    public static Context setLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);
        return context.createConfigurationContext(config);
    }
}