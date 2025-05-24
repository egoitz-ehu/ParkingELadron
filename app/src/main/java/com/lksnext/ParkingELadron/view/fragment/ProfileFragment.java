package com.lksnext.ParkingELadron.view.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lksnext.ParkingELadron.R;
import com.lksnext.ParkingELadron.databinding.FragmentProfileBinding;
import com.lksnext.ParkingELadron.view.activity.WelcomeActivity;
import com.lksnext.ParkingELadron.viewmodel.ProfileViewModel;

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
    }
}