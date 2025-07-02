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
    public void loginUserWithEmail_userAlreadyLoggedIn_doesNotCallRepository() {
        FirebaseUser fakeUser = Mockito.mock(FirebaseUser.class);
        userLiveData.setValue(fakeUser);

        viewModel.loginUserWithEmail("test@mail.com", "password");

        verify(mockRepository, Mockito.never()).signInWithEmailAndPassword(anyString(), anyString());
    }

    @Test
    public void loginUserWithEmail_emptyEmail_doesNotCallRepository() {
        viewModel.loginUserWithEmail("", "password");
        verify(mockRepository, Mockito.never()).signInWithEmailAndPassword(anyString(), anyString());
    }

    @Test
    public void loginUserWithEmail_emptyPassword_doesNotCallRepository() {
        viewModel.loginUserWithEmail("test@mail.com", "");
        verify(mockRepository, Mockito.never()).signInWithEmailAndPassword(anyString(), anyString());
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
        String errorMsg = "Login failed";
        errorLiveData.postValue(errorMsg);

        String result = LiveDataTestUtil.getValue(viewModel.getErrorLiveData());
        assertEquals(errorMsg, result);
    }
}
