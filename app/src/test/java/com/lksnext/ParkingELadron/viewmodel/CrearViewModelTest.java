package com.lksnext.ParkingELadron.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import com.lksnext.ParkingELadron.data.DataRepository;
import com.lksnext.ParkingELadron.domain.EstadoReserva;
import com.lksnext.ParkingELadron.domain.Plaza;
import com.lksnext.ParkingELadron.domain.Reserva;
import com.lksnext.ParkingELadron.domain.TiposPlaza;
import com.lksnext.ParkingELadron.util.LiveDataTestUtil;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Date;

public class CrearViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private DataRepository mockRepository;
    private CrearViewModel viewModel;

    @Before
    public void setUp() {
        // Mock del repositorio
        mockRepository = Mockito.mock(DataRepository.class);

        // Instancia real del ViewModel
        viewModel = new CrearViewModel(mockRepository);
    }

    @Test
    public void testCrearReserva_datosNoValidos() throws InterruptedException {
        viewModel.setDate(null);
        viewModel.setHoraFin(null);
        viewModel.setHoraInicio(null);
        viewModel.crearReserva("prueba");
        assertEquals("Por favor, completa todos los campos antes de crear la reserva.", LiveDataTestUtil.getValue(viewModel.getErrorMessage()));
        assertNull(LiveDataTestUtil.getValue(viewModel.getReservaCreada()));
        verifyNoInteractions(mockRepository);
    }

    @Test
    public void testCrearReserva_exito() throws InterruptedException {
        // Configurar valores completos para los campos requeridos
        viewModel.setDate(new Date());
        viewModel.setHoraInicio("10:00");
        viewModel.setHoraFin("12:00");
        viewModel.setPlazaSeleccionada(new Plaza("spot123", TiposPlaza.NORMAL));

        // Simular comportamiento exitoso del repositorio
        doAnswer(invocation -> {
            DataRepository.OnReservationCompleteListener listener = invocation.getArgument(7);
            listener.onReservationSuccess("parking123", "spot123", "reservation123");
            return null;
        }).when(mockRepository).createReservation(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(DataRepository.OnReservationCompleteListener.class)
        );

        // Llamar al método
        viewModel.crearReserva("user123");

        // Verificar que la reserva fue creada
        assertNotNull(LiveDataTestUtil.getValue(viewModel.getReservaCreada()));

        // Verificar que no hay mensaje de error
        assertNull(LiveDataTestUtil.getValue(viewModel.getErrorMessage()));

        // Verificar que el repositorio se llamara una vez con los valores correctos
        verify(mockRepository, times(1)).createReservation(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(DataRepository.OnReservationCompleteListener.class)
        );
    }

    @Test
    public void testEditarReserva_datosNoValidos() throws InterruptedException {
        viewModel.setDate(null);
        viewModel.setHoraFin(null);
        viewModel.setHoraInicio(null);
        viewModel.editarReserva("prueba", "prueba", null);
        assertEquals("Por favor, completa todos los campos antes de crear la reserva.", LiveDataTestUtil.getValue(viewModel.getErrorMessage()));
        assertNull(LiveDataTestUtil.getValue(viewModel.getReservaCreada()));
        verifyNoInteractions(mockRepository);
    }

    @Test
    public void testEditarReserva_exito() throws InterruptedException {
        viewModel.setDate(new Date());
        viewModel.setHoraInicio("10:00");
        viewModel.setHoraFin("12:00");
        Plaza plaza = new Plaza("a",  TiposPlaza.NORMAL);
        Reserva reserva = new Reserva(new Date(), "10:00", "12:00", plaza, "user", EstadoReserva.Reservado, "1234", "defaultParking");
        java.util.List<Reserva> reservas = new java.util.ArrayList<>();
        reservas.add(reserva);
        androidx.lifecycle.MutableLiveData<java.util.List<Reserva>> reservasLiveData = new androidx.lifecycle.MutableLiveData<>(reservas);
        Mockito.when(mockRepository.getReservationsLiveData()).thenReturn(reservasLiveData);

        doAnswer(invocation -> {
            DataRepository.OnReservationCompleteListener listener = invocation.getArgument(10);
            listener.onReservationSuccess("parking123", "spot123", "reservation123");
            return null;
        }).when(mockRepository).editReservation(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(Plaza.class),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(DataRepository.OnReservationCompleteListener.class)
        );

        viewModel.setPlazaSeleccionada(plaza);
        viewModel.editarReserva("1234", "2", reserva);

        assertNotNull(LiveDataTestUtil.getValue(viewModel.getReservaCreada()));
        assertNull(LiveDataTestUtil.getValue(viewModel.getErrorMessage()));

        verify(mockRepository, times(1)).editReservation(
                Mockito.eq("1234"),
                anyString(), // formattedDate
                anyString(), // finIso
                Mockito.eq("defaultParking"),
                any(Plaza.class),
                anyString(), // inicioIso
                Mockito.eq("2"),
                anyString(),
                anyString(),
                anyString(),
                any(DataRepository.OnReservationCompleteListener.class)
        );
    }

    @Test
    public void testEditarReserva_error() {
        viewModel.setDate(new Date());
        viewModel.setHoraInicio("10:00");
        viewModel.setHoraFin("12:00");
        viewModel.setPlazaSeleccionada(new Plaza("a", TiposPlaza.NORMAL));

        doAnswer(invocation -> {
            DataRepository.OnReservationCompleteListener listener = invocation.getArgument(10);
            listener.onReservationFailed("No hay plazas disponibles para hacer el cambio.");
            return null;
        }).when(mockRepository).editReservation(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(Plaza.class),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(DataRepository.OnReservationCompleteListener.class)
        );

        viewModel.editarReserva("1234", "2", new Reserva(new Date(), "10:00", "12:00", new Plaza("a",  TiposPlaza.NORMAL), "user", EstadoReserva.Reservado, "1234", "defaultParking"));

        verify(mockRepository, times(1)).editReservation(
                Mockito.eq("1234"),
                anyString(), // formattedDate
                anyString(), // finIso
                Mockito.eq("defaultParking"),
                any(Plaza.class),
                anyString(), // inicioIso
                Mockito.eq("2"),
                anyString(), // oldDay
                anyString(), // oldHoraInicio
                anyString(), // oldHoraFin
                any(DataRepository.OnReservationCompleteListener.class)
        );
    }
}

