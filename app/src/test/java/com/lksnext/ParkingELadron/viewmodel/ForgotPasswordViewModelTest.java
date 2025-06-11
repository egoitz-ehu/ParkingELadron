package com.lksnext.ParkingELadron.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.lksnext.ParkingELadron.data.AuthRepository;
import com.lksnext.ParkingELadron.util.LiveDataTestUtil;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

public class ForgotPasswordViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private AuthRepository mockRepository;

    private ForgotPasswordViewModel viewModel;

    private MutableLiveData<String> successLiveData;
    private MutableLiveData<String> errorLiveData;

    @Before
    public void setUp() {
        mockRepository = Mockito.mock(AuthRepository.class);
        successLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();

        when(mockRepository.getSuccessLiveData()).thenReturn(successLiveData);
        when(mockRepository.getErrorLiveData()).thenReturn(errorLiveData);

        viewModel = new ForgotPasswordViewModel(mockRepository);
    }

    @Test
    public void sendPasswordResetEmail_withValidEmail_callsRepository() throws InterruptedException {
        String email = "prueba@prueba.com";

        doAnswer(invocation -> {
            String argEmail = invocation.getArgument(0);
            successLiveData.postValue("Email sent to " + argEmail);
            return null;
        }).when(mockRepository).sendPasswordResetEmail(email);

        viewModel.sendPasswordResetEmail(email);

        assertEquals("Email sent to " + email, LiveDataTestUtil.getValue(viewModel.getSuccessLiveData()));
        assertEquals(null, LiveDataTestUtil.getValue(viewModel.getErrorLiveData()));
    }

    @Test
    public void sendPasswordResetEmail_withInvalidEmail_doesNotCallRepository() throws InterruptedException {
        String email = "";

        viewModel.sendPasswordResetEmail(email);

        assertEquals(null, LiveDataTestUtil.getValue(viewModel.getSuccessLiveData()));
        assertEquals(null, LiveDataTestUtil.getValue(viewModel.getErrorLiveData()));
    }

    @Test
    public void sendPasswordResetEmail_withError_callsRepository() throws InterruptedException {
        String email = "a";
        String errorMessage = "Invalid email format";
        doAnswer(invocation -> {
            String argEmail = invocation.getArgument(0);
            errorLiveData.postValue(errorMessage);
            return null;
        }).when(mockRepository).sendPasswordResetEmail(email);
        viewModel.sendPasswordResetEmail(email);
        assertEquals(null, LiveDataTestUtil.getValue(viewModel.getSuccessLiveData()));
        assertEquals(errorMessage, LiveDataTestUtil.getValue(viewModel.getErrorLiveData()));
    }
}
