package com.lksnext.ParkingELadron.view.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.lksnext.ParkingELadron.R;
import com.lksnext.ParkingELadron.databinding.FragmentProfileBinding;
import com.lksnext.ParkingELadron.domain.LanguageItem;
import com.lksnext.ParkingELadron.view.activity.WelcomeActivity;
import com.lksnext.ParkingELadron.view.adapters.LanguageSpinnerAdapter;
import com.lksnext.ParkingELadron.viewmodel.ProfileViewModel;

import java.util.Arrays;
import java.util.List;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    private ProfileViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnExit.setOnClickListener(v -> viewModel.logout());

        viewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if(user==null) {
                Intent intent = new Intent(requireContext(), WelcomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            } else {
                binding.tvName.setText(getString(R.string.perfil_name)+user.getDisplayName().split(" ")[0]);
                binding.tvSurname.setText(getString(R.string.perfil_surname)+user.getDisplayName().split(" ")[1]);
                binding.tvEmail.setText(getString(R.string.perfil_email)+user.getEmail());
            }
        });

        List<LanguageItem> languages = Arrays.asList(
                new LanguageItem("es", "Español", R.drawable.ic_flag_es),
                new LanguageItem("en", "English", R.drawable.ic_flag_en),
                new LanguageItem("eu", "Euskara", R.drawable.ic_flag_eu)
        );

        LanguageSpinnerAdapter adapter = new LanguageSpinnerAdapter(requireContext(), languages);
        Spinner spinner = binding.spinnerLanguage;
        spinner.setAdapter(adapter);

        String lang = requireContext().getSharedPreferences("settings", MODE_PRIVATE).getString("lang", "es");
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
                SharedPreferences prefs = requireContext().getSharedPreferences("settings", MODE_PRIVATE);
                if (!prefs.getString("lang", "es").equals(selectedLang)) {
                    prefs.edit().putString("lang", selectedLang).apply();
                    requireActivity().recreate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}