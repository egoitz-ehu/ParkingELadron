package com.lksnext.ParkingELadron.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
        viewModel.setType(null);
        viewModel.setHoraFin(null);
        viewModel.setHoraInicio(null);
        viewModel.crearReserva("prueba");
        assertEquals("Por favor, completa todos los campos antes de crear la reserva.", LiveDataTestUtil.getValue(viewModel.getErrorMessage()));
        assertFalse(LiveDataTestUtil.getValue(viewModel.getReservaCreada()));
        verifyNoInteractions(mockRepository);
    }

    @Test
    public void testCrearReserva_exito() throws InterruptedException {
        // Configurar valores completos para los campos requeridos
        viewModel.setDate(new Date());
        viewModel.setHoraInicio("10:00");
        viewModel.setHoraFin("12:00");
        viewModel.setType(TiposPlaza.NORMAL);

        // Simular comportamiento exitoso del repositorio
        doAnswer(invocation -> {
            DataRepository.OnReservationCompleteListener listener = invocation.getArgument(5);
            listener.onReservationSuccess("parking123", "spot123", "reservation123");
            return null;
        }).when(mockRepository).findAndCreateReservation(
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
        assertTrue(LiveDataTestUtil.getValue(viewModel.getReservaCreada()));

        // Verificar que no hay mensaje de error
        assertNull(LiveDataTestUtil.getValue(viewModel.getErrorMessage()));

        // Verificar que el repositorio se llamara una vez con los valores correctos
        verify(mockRepository, times(1)).findAndCreateReservation(
                Mockito.eq(TiposPlaza.NORMAL.toString()),                       // Tipo
                anyString(),                                // Fecha formateada
                Mockito.eq("10:00"),                        // Hora de inicio
                Mockito.eq("12:00"),                        // Hora de fin
                Mockito.eq("user123"),                      // Usuario ID
                any(DataRepository.OnReservationCompleteListener.class) // Callback
        );
    }

    @Test
    public void testCrearReserva_error() throws InterruptedException {
        viewModel.setDate(new Date());
        viewModel.setHoraInicio("10:00");
        viewModel.setHoraFin("12:00");
        viewModel.setType(TiposPlaza.NORMAL);
        doAnswer(invocation -> {
            DataRepository.OnReservationCompleteListener listener = invocation.getArgument(5);
            listener.onReservationFailed("Plazas no disponibles");
            return null;
        }).when(mockRepository).findAndCreateReservation(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(DataRepository.OnReservationCompleteListener.class)
        );
        viewModel.crearReserva("prueba");
        assertFalse(LiveDataTestUtil.getValue(viewModel.getReservaCreada()));
        assertEquals("Plazas no disponibles", LiveDataTestUtil.getValue(viewModel.getErrorMessage()));
        verify(mockRepository, times(1)).findAndCreateReservation(
                Mockito.eq(TiposPlaza.NORMAL.toString()),
                anyString(),
                Mockito.eq("10:00"),
                Mockito.eq("12:00"),
                Mockito.eq("prueba"),
                any(DataRepository.OnReservationCompleteListener.class)
        );
    }

    @Test
    public void testEditarReserva_datosNoValidos() throws InterruptedException {
        viewModel.setDate(null);
        viewModel.setType(null);
        viewModel.setHoraFin(null);
        viewModel.setHoraInicio(null);
        viewModel.editarReserva("prueba", "prueba");
        assertEquals("Por favor, completa todos los campos antes de crear la reserva.", LiveDataTestUtil.getValue(viewModel.getErrorMessage()));
        assertFalse(LiveDataTestUtil.getValue(viewModel.getReservaCreada()));
        verifyNoInteractions(mockRepository);
    }

    @Test
    public void testEditarReserva_exito() throws InterruptedException {
        viewModel.setDate(new Date());
        viewModel.setHoraInicio("10:00");
        viewModel.setHoraFin("12:00");
        viewModel.setType(TiposPlaza.NORMAL);

        doAnswer(invocation -> {
            DataRepository.OnReservationCompleteListener listener = invocation.getArgument(7);
            listener.onReservationSuccess("parking123", "spot123", "reservation123");
            return null;
        }).when(mockRepository).editReservation(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(TiposPlaza.class),
                anyString(),
                anyString(),
                any(DataRepository.OnReservationCompleteListener.class)
        );

        viewModel.editarReserva("1234", "2");

        assertTrue(LiveDataTestUtil.getValue(viewModel.getReservaCreada()));
        assertNull(LiveDataTestUtil.getValue(viewModel.getErrorMessage()));

        verify(mockRepository, times(1)).editReservation(
                Mockito.eq("1234"),
                anyString(),
                Mockito.eq("12:00"),
                Mockito.eq("defaultParking"),
                Mockito.eq(TiposPlaza.NORMAL),
                Mockito.eq("10:00"),
                Mockito.eq("2"),
                any(DataRepository.OnReservationCompleteListener.class)
        );
    }

    @Test
    public void testEditarReserva_error() {
        viewModel.setDate(new Date());
        viewModel.setHoraInicio("10:00");
        viewModel.setHoraFin("12:00");
        viewModel.setType(TiposPlaza.NORMAL);

        doAnswer(invocation -> {
            DataRepository.OnReservationCompleteListener listener = invocation.getArgument(7);
            listener.onReservationFailed("No hay plazas disponibles para hacer el cambio.");
            return null;
        }).when(mockRepository).editReservation(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(TiposPlaza.class),
                anyString(),
                anyString(),
                any(DataRepository.OnReservationCompleteListener.class)
        );

        viewModel.editarReserva("1234", "2");

        verify(mockRepository, times(1)).editReservation(
                Mockito.eq("1234"),
                anyString(),
                Mockito.eq("12:00"),
                Mockito.eq("defaultParking"),
                Mockito.eq(TiposPlaza.NORMAL),
                Mockito.eq("10:00"),
                Mockito.eq("2"),
                any(DataRepository.OnReservationCompleteListener.class)
        );
    }
}