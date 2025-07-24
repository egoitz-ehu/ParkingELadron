package com.lksnext.ParkingELadron.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.lksnext.ParkingELadron.domain.ProfileBuilder;
import com.lksnext.ParkingELadron.util.LiveDataTestUtil;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AuthRepositoryTest {
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    FirebaseAuth mockAuth;

    @Mock
    FirebaseUser mockUser;

    @Mock
    Task<AuthResult> mockTask;

    @Mock
    Task<Void> mockUpdateProfileTask;

    @Mock
    ProfileBuilder mockProfileBuilder;

    private AuthRepository authRepository;

    @Before
    public void setUp() {
        authRepository = new AuthRepository(mockAuth, mockProfileBuilder);
    }

    @Test
    public void testSignInWithEmailAndPassword_Success() throws InterruptedException {
        String email = "test@example.com";
        String password = "123456";

        when(mockAuth.signInWithEmailAndPassword(email, password)).thenReturn(mockTask);
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
        when(mockTask.isSuccessful()).thenReturn(true);

        doAnswer(invocation -> {
            OnCompleteListener<AuthResult> listener = invocation.getArgument(0);
            listener.onComplete(mockTask);
            return null;
        }).when(mockTask).addOnCompleteListener(any());

        authRepository.signInWithEmailAndPassword(email, password);

        FirebaseUser result = LiveDataTestUtil.getValue(authRepository.getUserLiveData());
        assertEquals(mockUser, result);
    }

    @Test
    public void testRegisterUserWithEmail_Success() throws InterruptedException {
        String email = "test@example.com";
        String password = "password123";
        String name = "Juan";
        String surname = "Pérez";
        
        when(mockAuth.createUserWithEmailAndPassword(email, password)).thenReturn(mockTask);
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
        when(mockTask.isSuccessful()).thenReturn(true);

        UserProfileChangeRequest mockProfileRequest = mock(UserProfileChangeRequest.class);
        when(mockProfileBuilder.buildProfile(name, surname)).thenReturn(mockProfileRequest);

        when(mockUser.updateProfile(mockProfileRequest)).thenReturn(mockUpdateProfileTask);
        when(mockUpdateProfileTask.isSuccessful()).thenReturn(true);

        doAnswer(invocation -> {
            OnCompleteListener<AuthResult> listener = invocation.getArgument(0);
            listener.onComplete(mockTask);
            return null;
        }).when(mockTask).addOnCompleteListener(any());

        doAnswer(invocation -> {
            OnCompleteListener<Void> listener = invocation.getArgument(0);
            listener.onComplete(mockUpdateProfileTask);
            return null;
        }).when(mockUpdateProfileTask).addOnCompleteListener(any());

        authRepository.registerUserWithEmail(email, password, name, surname);

        FirebaseUser result = LiveDataTestUtil.getValue(authRepository.getUserLiveData());

        assertEquals(mockUser, result);
        assertNull(LiveDataTestUtil.getValue(authRepository.getErrorLiveData()));
    }

    @Test
    public void testRegisterUserWithEmail_ErrorOnCreateUser() throws InterruptedException {
        String email = "test@example.com";
        String password = "password123";
        String name = "Juan";
        String surname = "Pérez";

        when(mockAuth.createUserWithEmailAndPassword(email, password)).thenReturn(mockTask);
        when(mockTask.isSuccessful()).thenReturn(false);
        Exception exception = new Exception("Error al crear usuario");
        when(mockTask.getException()).thenReturn(exception);

        doAnswer(invocation -> {
            OnCompleteListener<AuthResult> listener = invocation.getArgument(0);
            listener.onComplete(mockTask);
            return null;
        }).when(mockTask).addOnCompleteListener(any());

        authRepository.registerUserWithEmail(email, password, name, surname);

        String error = LiveDataTestUtil.getValue(authRepository.getErrorLiveData());
        assertEquals("Error al crear usuario", error);
    }

    @Test
    public void testRegisterUserWithEmail_ErrorOnUpdateProfile() throws InterruptedException {
        String email = "test@example.com";
        String password = "password123";
        String name = "Juan";
        String surname = "Pérez";

        when(mockAuth.createUserWithEmailAndPassword(email, password)).thenReturn(mockTask);
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
        when(mockTask.isSuccessful()).thenReturn(true);

        UserProfileChangeRequest mockProfileRequest = mock(UserProfileChangeRequest.class);
        when(mockProfileBuilder.buildProfile(name, surname)).thenReturn(mockProfileRequest);
        when(mockUser.updateProfile(mockProfileRequest)).thenReturn(mockUpdateProfileTask);
        when(mockUpdateProfileTask.isSuccessful()).thenReturn(false);
        Exception exception = new Exception("Error al actualizar perfil");
        when(mockUpdateProfileTask.getException()).thenReturn(exception);

        doAnswer(invocation -> {
            OnCompleteListener<AuthResult> listener = invocation.getArgument(0);
            listener.onComplete(mockTask);
            return null;
        }).when(mockTask).addOnCompleteListener(any());

        doAnswer(invocation -> {
            OnCompleteListener<Void> listener = invocation.getArgument(0);
            listener.onComplete(mockUpdateProfileTask);
            return null;
        }).when(mockUpdateProfileTask).addOnCompleteListener(any());

        authRepository.registerUserWithEmail(email, password, name, surname);

        String error = LiveDataTestUtil.getValue(authRepository.getErrorLiveData());
        assertEquals("Error al actualizar perfil", error);
    }

    @Test
    public void testSignInWithEmailAndPassword_Error() throws InterruptedException {
        String email = "test@example.com";
        String password = "123456";

        when(mockAuth.signInWithEmailAndPassword(email, password)).thenReturn(mockTask);
        when(mockTask.isSuccessful()).thenReturn(false);
        Exception exception = new Exception("Error de login");
        when(mockTask.getException()).thenReturn(exception);

        doAnswer(invocation -> {
            OnCompleteListener<AuthResult> listener = invocation.getArgument(0);
            listener.onComplete(mockTask);
            return null;
        }).when(mockTask).addOnCompleteListener(any());

        authRepository.signInWithEmailAndPassword(email, password);

        String error = LiveDataTestUtil.getValue(authRepository.getErrorLiveData());
        assertEquals("Error de login", error);
    }

    @Test
    public void testSignOut() throws InterruptedException {
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
        authRepository.getUserFromDatabase();
        FirebaseUser beforeSignOut = LiveDataTestUtil.getValue(authRepository.getUserLiveData());
        assertEquals(mockUser, beforeSignOut);

        authRepository.signOut();
        FirebaseUser afterSignOut = LiveDataTestUtil.getValue(authRepository.getUserLiveData());
        assertNull(afterSignOut);
    }

    @Test
    public void testGetUserFromDatabase() throws InterruptedException {
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
        authRepository.getUserFromDatabase();
        FirebaseUser result = LiveDataTestUtil.getValue(authRepository.getUserLiveData());
        assertEquals(mockUser, result);
    }

    @Test
    public void testSendPasswordResetEmail_Success() throws InterruptedException {
        String email = "test@example.com";
        Task<Void> mockResetTask = mock(Task.class);
        when(mockAuth.sendPasswordResetEmail(email)).thenReturn(mockResetTask);
        when(mockResetTask.isSuccessful()).thenReturn(true);

        doAnswer(invocation -> {
            OnCompleteListener<Void> listener = invocation.getArgument(0);
            listener.onComplete(mockResetTask);
            return null;
        }).when(mockResetTask).addOnCompleteListener(any());

        authRepository.sendPasswordResetEmail(email);
        String success = LiveDataTestUtil.getValue(authRepository.getSuccessLiveData());
        assertEquals("Correo de restablecimiento enviado", success);
    }

    @Test
    public void testSendPasswordResetEmail_Error() throws InterruptedException {
        String email = "test@example.com";
        Task<Void> mockResetTask = mock(Task.class);
        when(mockAuth.sendPasswordResetEmail(email)).thenReturn(mockResetTask);
        when(mockResetTask.isSuccessful()).thenReturn(false);
        Exception exception = new Exception("Error al enviar correo");
        when(mockResetTask.getException()).thenReturn(exception);

        doAnswer(invocation -> {
            OnCompleteListener<Void> listener = invocation.getArgument(0);
            listener.onComplete(mockResetTask);
            return null;
        }).when(mockResetTask).addOnCompleteListener(any());

        authRepository.sendPasswordResetEmail(email);
        String error = LiveDataTestUtil.getValue(authRepository.getErrorLiveData());
        assertEquals("Error al enviar correo", error);
    }
}

