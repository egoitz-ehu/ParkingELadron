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

public class RegisterViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private AuthRepository mockRepository;
    private RegisterViewModel viewModel;
    private MutableLiveData<FirebaseUser> userLiveData;
    private MutableLiveData<String> errorLiveData;

    @Before
    public void setUp() {
        mockRepository = Mockito.mock(AuthRepository.class);
        userLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();

        when(mockRepository.getUserLiveData()).thenReturn(userLiveData);
        when(mockRepository.getErrorLiveData()).thenReturn(errorLiveData);

        viewModel = new RegisterViewModel(mockRepository);
    }

    @Test
    public void registerUserWithEmail_userAlreadyLoggedIn_doesNotCallRepository() {
        FirebaseUser fakeUser = Mockito.mock(FirebaseUser.class);
        userLiveData.setValue(fakeUser);

        viewModel.registerUserWithEmail("test@mail.com", "password", "John", "Doe");

        verify(mockRepository, Mockito.never()).registerUserWithEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void registerUserWithEmail_emptyEmail_doesNotCallRepository() {
        viewModel.registerUserWithEmail("", "password", "John", "Doe");
        verify(mockRepository, Mockito.never()).registerUserWithEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void registerUserWithEmail_emptyPassword_doesNotCallRepository() {
        viewModel.registerUserWithEmail("test@mail.com", "", "John", "Doe");
        verify(mockRepository, Mockito.never()).registerUserWithEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void registerUserWithEmail_emptyName_doesNotCallRepository() {
        viewModel.registerUserWithEmail("test@mail.com", "password", "", "Doe");
        verify(mockRepository, Mockito.never()).registerUserWithEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void registerUserWithEmail_emptySurname_doesNotCallRepository() {
        viewModel.registerUserWithEmail("test@mail.com", "password", "John", "");
        verify(mockRepository, Mockito.never()).registerUserWithEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void getUserLiveData_returnsRepositoryLiveData() throws InterruptedException {
        FirebaseUser fakeUser = Mockito.mock(FirebaseUser.class);
        userLiveData.postValue(fakeUser);

        FirebaseUser result = LiveDataTestUtil.getValue(viewModel.getUserLiveData());
        assertEquals(fakeUser, result);
    }

    @Test
    public void getErrorLiveData_returnsRepositoryErrorLiveData() throws InterruptedException {
        String errorMsg = "Register failed";
        errorLiveData.postValue(errorMsg);

        String result = LiveDataTestUtil.getValue(viewModel.getErrorLiveData());
        assertEquals(errorMsg, result);
    }
}
