package com.lksnext.ParkingELadron.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseUser;
import com.lksnext.ParkingELadron.data.AuthRepository;
import com.lksnext.ParkingELadron.util.LiveDataTestUtil;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

public class LoginViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private AuthRepository mockRepository;
    private LoginViewModel viewModel;
    private MutableLiveData<FirebaseUser> userLiveData;
    private MutableLiveData<String> errorLiveData;

    @Before
    public void setUp() {
        mockRepository = Mockito.mock(AuthRepository.class);
        userLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();

        when(mockRepository.getUserLiveData()).thenReturn(userLiveData);
        when(mockRepository.getErrorLiveData()).thenReturn(errorLiveData);

        viewModel = new LoginViewModel(mockRepository);
    }

    @Test
    public void registerUserWithValidData_callsRepositoryAndUpdatesLiveData() throws InterruptedException {
        String email = "test@mail.com";
        String password = "password";
        FirebaseUser fakeUser = Mockito.mock(FirebaseUser.class);

        viewModel.loginUserWithEmail(email, password);

        verify(mockRepository).signInWithEmailAndPassword(email, password);

        userLiveData.postValue(fakeUser);

        FirebaseUser result = LiveDataTestUtil.getValue(viewModel.getUserLiveData());
        assertEquals(fakeUser, result);
    }

    @Test
    public void registerUserWithEmptyData_doesNotCallRepository(){
        viewModel.loginUserWithEmail("", "");
        verify(mockRepository, Mockito.never()).signInWithEmailAndPassword(anyString(), anyString());
    }

    @Test
    public void errorLiveData_emitsError() throws InterruptedException {
        String errorMsg = "Error msg";
        errorLiveData.postValue(errorMsg);
        String result = LiveDataTestUtil.getValue(viewModel.getErrorLiveData());
        assertEquals(errorMsg, result);
    }
}
