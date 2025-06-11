package com.lksnext.ParkingELadron.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.lksnext.ParkingELadron.data.DataRepository;
import com.lksnext.ParkingELadron.domain.Reserva;
import com.lksnext.ParkingELadron.util.LiveDataTestUtil;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

public class ReservasViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private DataRepository mockRepository;
    private ReservasViewModel viewModel;
    private MutableLiveData<String> errorMessageLiveData;
    private MutableLiveData<String> idWorkerId1;
    private MutableLiveData<String> idWorkerId2;

    @Before
    public void setUp() {
        mockRepository = Mockito.mock(DataRepository.class);
        errorMessageLiveData = new MutableLiveData<>();
        idWorkerId1 = new MutableLiveData<>();
        idWorkerId2 = new MutableLiveData<>();

        viewModel = new ReservasViewModel(mockRepository);
    }

    @Test
    public void testRemoveReservation_updatesWorkerIds() throws InterruptedException {
        // Arrange
        String expectedId1 = "workerId1";
        String expectedId2 = "workerId2";
        Reserva reserva = Mockito.mock(Reserva.class);
        Mockito.when(reserva.getNotificationWorkerId1()).thenReturn(expectedId1);
        Mockito.when(reserva.getNotificationWorkerId2()).thenReturn(expectedId2);

        doAnswer(invocation -> {
            DataRepository.OnReservationRemoveListener listener = invocation.getArgument(1);
            listener.onReservationRemoveSuccess();
            return null;
                }).when(mockRepository).deleteReservation(any(Reserva.class), any(DataRepository.OnReservationRemoveListener.class));

        // Act
        viewModel.removeReservation(reserva);

        // Assert
        assertEquals(expectedId1, LiveDataTestUtil.getValue(viewModel.getIdWorkerId1()));
        assertEquals(expectedId2, LiveDataTestUtil.getValue(viewModel.getIdWorkerId2()));
    }

    @Test
    public void testRemoveReservation_errorHandling() throws InterruptedException {
        // Arrange
        Reserva reserva = Mockito.mock(Reserva.class);
        doAnswer(invocation -> {
            DataRepository.OnReservationRemoveListener listener = invocation.getArgument(1);
            listener.onReservationRemoveFailed("Error removing reservation");
            return null;
        }).when(mockRepository).deleteReservation(any(Reserva.class), any(DataRepository.OnReservationRemoveListener.class));

        // Act
        viewModel.removeReservation(reserva);

        // Assert
        assertEquals("Error removing reservation", LiveDataTestUtil.getValue(viewModel.getErrorMessageLiveData()));
        assertNull(LiveDataTestUtil.getValue(viewModel.getIdWorkerId1()));
        assertNull(LiveDataTestUtil.getValue(viewModel.getIdWorkerId2()));
    }
}
