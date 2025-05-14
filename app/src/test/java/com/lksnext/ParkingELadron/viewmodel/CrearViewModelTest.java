package com.lksnext.ParkingELadron.viewmodel;

import static org.junit.Assert.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.lksnext.ParkingELadron.domain.TiposPlaza;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

public class CrearViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private CrearViewModel viewModel;

    @Before
    public void setUp(){
        viewModel = new CrearViewModel();
    }

    @Test
    public void testCrearReserva_allValid_returnsTrue() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 3);
        Date date = cal.getTime();

        viewModel.setDate(date);
        viewModel.setHoraInicio("10:00");
        viewModel.setHoraFin("11:00");
        viewModel.setType(TiposPlaza.NORMAL);

        assertTrue(viewModel.crearReserva());
    }

    @Test
    public void testCrearReserva_dateInPast_returnsFalse() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR,-1);
        viewModel.setDate(cal.getTime());
        viewModel.setHoraInicio("10:00");
        viewModel.setHoraFin("11:00");
        viewModel.setType(TiposPlaza.NORMAL);

        assertFalse(viewModel.crearReserva());
    }

    @Test
    public void testCrearReserva_date_veryDistantDate_returnsFalse(){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH,10);
        viewModel.setDate(cal.getTime());
        viewModel.setHoraInicio("10:00");
        viewModel.setHoraFin("11:00");
        viewModel.setType(TiposPlaza.NORMAL);

        assertFalse(viewModel.crearReserva());
    }

    @Test
    public void testCrearReserva_dateToday_returnsTrue(){
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();

        viewModel.setDate(date);
        viewModel.setHoraInicio("10:00");
        viewModel.setHoraFin("11:00");
        viewModel.setType(TiposPlaza.NORMAL);

        assertTrue(viewModel.crearReserva());
    }

    @Test
    public void testCrearReserva_dateOneDayAfterLimit_returnsFalse(){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH,8);
        viewModel.setDate(cal.getTime());
        viewModel.setHoraInicio("10:00");
        viewModel.setHoraFin("11:00");
        viewModel.setType(TiposPlaza.NORMAL);

        assertFalse(viewModel.crearReserva());
    }

    @Test
    public void testCrearReserva_nullValues_returnsFalse(){
        assertFalse(viewModel.crearReserva());
    }
}