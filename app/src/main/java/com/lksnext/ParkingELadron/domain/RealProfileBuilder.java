package com.lksnext.ParkingELadron.domain;

import com.google.firebase.auth.UserProfileChangeRequest;

public class RealProfileBuilder implements ProfileBuilder {
    @Override
    public UserProfileChangeRequest buildProfile(String name, String surname) {
        return new UserProfileChangeRequest.Builder()
                .setDisplayName(name + " " + surname)
                .build();
    }
}
