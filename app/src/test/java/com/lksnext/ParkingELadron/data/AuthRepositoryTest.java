package com.lksnext.ParkingELadron.data;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.lksnext.ParkingELadron.util.LiveDataTestUtil;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class AuthRepositoryTest {
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private FirebaseAuth mockAuth;
    private AuthRepository repository;

    @Before
    public void setUp() {
        mockAuth = mock(FirebaseAuth.class);
        repository = new AuthRepository(mockAuth);
    }

    @Test
    public void registerUserWithEmail_callsFirebaseCreateUser() {
        String email = "jola@prueba.com";
        String password = "password";
        String name = "John";
        String surname = "Doe";

        Task<AuthResult> mockTask = mock(Task.class);
        when(mockAuth.createUserWithEmailAndPassword(email,password)).thenReturn(mockTask);

        repository.registerUserWithEmail(email, password, name, surname);

        verify(mockAuth).createUserWithEmailAndPassword(email,password);
    }

    @Test
    public void registerUserWithEmail_whenFirebaseFails() throws InterruptedException {
        String email = "jola@prueba.com";
        String password = "password";
        String name = "John";
        String surname = "Doe";

        Exception fakeException = new Exception("Registration error");

        Task<AuthResult> mockTask = mock(Task.class);
        when(mockAuth.createUserWithEmailAndPassword(email,password)).thenReturn(mockTask);
        when(mockTask.isSuccessful()).thenReturn(false);
        when(mockTask.getException()).thenReturn(fakeException);

        ArgumentCaptor<OnCompleteListener<AuthResult>> captor = ArgumentCaptor.forClass(OnCompleteListener.class);
        when(mockTask.addOnCompleteListener(captor.capture())).thenReturn(mockTask);

        repository.registerUserWithEmail(email, password, name, surname);

        verify(mockAuth).createUserWithEmailAndPassword(email,password);

        captor.getValue().onComplete(mockTask);

        assertEquals("Registration error", LiveDataTestUtil.getValue(repository.getErrorLiveData()));
    }
}
