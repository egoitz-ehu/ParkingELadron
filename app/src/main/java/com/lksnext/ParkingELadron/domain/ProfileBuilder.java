package com.lksnext.ParkingELadron.domain;

import com.google.firebase.auth.UserProfileChangeRequest;

public interface ProfileBuilder {
    UserProfileChangeRequest buildProfile(String name, String surname);
}
