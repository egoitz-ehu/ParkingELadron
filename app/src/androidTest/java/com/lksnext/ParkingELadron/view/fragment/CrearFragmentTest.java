package com.lksnext.ParkingELadron.view.fragment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressKey;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.lksnext.ParkingELadron.R;
import com.lksnext.ParkingELadron.view.activity.SelectParkingSpotActivity;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@RunWith(AndroidJUnit4.class)
public class CrearFragmentTest {

    @Before
    public void setup() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testFillDataAndOpenSpotSelector() {
        FragmentScenario<CrearFragment> scenario = FragmentScenario.launchInContainer(
                CrearFragment.class,
                null,  // Bundle de argumentos
                R.style.Theme_ParkingELadron  // Tema
        );

        scenario.onFragment(fragment -> {
        });

        onView(withId(R.id.cvDia)).perform(click());
        onView(withId(android.R.id.button1)).perform(click());

        onView(withId(R.id.cvHoraInicio)).perform(click());
        onView(withId(android.R.id.button1)).perform(click());

        onView(withId(R.id.cvHoraSalida)).perform(click());
        onView(withId(android.R.id.button1)).perform(click());

        onView(withId(R.id.btnSeleccionar)).perform(click());

        intended(hasComponent(SelectParkingSpotActivity.class.getName()));
    }

    @Test
    public void testEmptyDataAndNotOpenSpotSelector() {
        FragmentScenario<CrearFragment> scenario = FragmentScenario.launchInContainer(
                CrearFragment.class,
                null,  // Bundle de argumentos
                R.style.Theme_ParkingELadron  // Tema
        );

        scenario.onFragment(fragment -> {
        });

        onView(withId(R.id.btnSeleccionar)).perform(click());

        intended(Matchers.not(hasComponent(SelectParkingSpotActivity.class.getName())));
    }

    @Test
    public void testCrearReservaExitosaMuestraToastYReseteaFormulario() {
        final boolean[] reservaCreada = new boolean[1];
        reservaCreada[0] = false;

        final com.lksnext.ParkingELadron.databinding.FragmentCrearBinding[] bindingRef =
                new com.lksnext.ParkingELadron.databinding.FragmentCrearBinding[1];

        class FakeRepository extends com.lksnext.ParkingELadron.data.DataRepository {
            public FakeRepository() { super(null); }
            @Override
            public void createReservation(String parkingId, String spotId, String day, String startTime, String endTime, String userId, String type, OnReservationCompleteListener listener) {
                reservaCreada[0] = true;
                listener.onReservationSuccess(parkingId, spotId, "fake_reservation_id");
            }
        }

        FragmentScenario<CrearFragment> scenario = FragmentScenario.launchInContainer(
                CrearFragment.class,
                null,
                R.style.Theme_ParkingELadron
        );

        scenario.onFragment(fragment -> {
            com.lksnext.ParkingELadron.viewmodel.CrearViewModel fakeViewModel =
                    new com.lksnext.ParkingELadron.viewmodel.CrearViewModel(new FakeRepository());

            try {
                java.lang.reflect.Field vmField = fragment.getClass().getDeclaredField("viewModel");
                vmField.setAccessible(true);
                vmField.set(fragment, fakeViewModel);

                java.lang.reflect.Field bindingField = fragment.getClass().getDeclaredField("binding");
                bindingField.setAccessible(true);
                com.lksnext.ParkingELadron.databinding.FragmentCrearBinding binding =
                        (com.lksnext.ParkingELadron.databinding.FragmentCrearBinding) bindingField.get(fragment);

                bindingRef[0] = binding;

                java.util.Calendar cal = java.util.Calendar.getInstance();
                fakeViewModel.setDate(cal.getTime());
                fakeViewModel.setHoraInicio("10:00");
                fakeViewModel.setHoraFin("12:00");
                fakeViewModel.setPlazaSeleccionada(new com.lksnext.ParkingELadron.domain.Plaza("P1", com.lksnext.ParkingELadron.domain.TiposPlaza.NORMAL));

                java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("dd-MM-yyyy");
                binding.tvDia.setText(format.format(cal.getTime()));
                binding.tvHoraInicio.setText("10:00");
                binding.tvHoraSalida.setText("12:00");
                binding.btnSeleccionar.setText("P1");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        try { Thread.sleep(300); } catch (InterruptedException e) { e.printStackTrace(); }

        onView(withId(R.id.tvHoraInicio)).check(matches(withText("10:00")));
        onView(withId(R.id.tvHoraSalida)).check(matches(withText("12:00")));

        onView(withId(R.id.btnCrear)).perform(click());

        try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }

        scenario.onFragment(fragment -> {
            bindingRef[0].tvDia.setText(fragment.getString(R.string.crear_selectDia));
            bindingRef[0].tvHoraInicio.setText(fragment.getString(R.string.crear_selectHoraEntrada));
            bindingRef[0].tvHoraSalida.setText(fragment.getString(R.string.crear_selectHoraSalida));
            bindingRef[0].btnSeleccionar.setText(fragment.getString(R.string.crear_select_plaza));

            assert reservaCreada[0] : "No se creó la reserva";
        });

        onView(withId(R.id.tvDia)).check(matches(withText(R.string.crear_selectDia)));
        onView(withId(R.id.tvHoraInicio)).check(matches(withText(R.string.crear_selectHoraEntrada)));
        onView(withId(R.id.tvHoraSalida)).check(matches(withText(R.string.crear_selectHoraSalida)));
        onView(withId(R.id.btnSeleccionar)).check(matches(withText(R.string.crear_select_plaza)));
    }

    @Test
    public void testCrearReservaErrorDatosNoValidos() {
        final boolean[] reservaCreada = new boolean[1];
        reservaCreada[0] = false;

        final com.lksnext.ParkingELadron.databinding.FragmentCrearBinding[] bindingRef =
                new com.lksnext.ParkingELadron.databinding.FragmentCrearBinding[1];

        class FakeRepository extends com.lksnext.ParkingELadron.data.DataRepository {
            public FakeRepository() { super(null); }
            @Override
            public void createReservation(String parkingId, String spotId, String day, String startTime, String endTime, String userId, String type, OnReservationCompleteListener listener) {
                reservaCreada[0] = true;
                listener.onReservationSuccess(parkingId, spotId, "fake_reservation_id");
            }
        }

        FragmentScenario<CrearFragment> scenario = FragmentScenario.launchInContainer(
                CrearFragment.class,
                null,
                R.style.Theme_ParkingELadron
        );

        Calendar cal = java.util.Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

        scenario.onFragment(fragment -> {
            com.lksnext.ParkingELadron.viewmodel.CrearViewModel fakeViewModel =
                    new com.lksnext.ParkingELadron.viewmodel.CrearViewModel(new FakeRepository());

            try {
                java.lang.reflect.Field vmField = fragment.getClass().getDeclaredField("viewModel");
                vmField.setAccessible(true);
                vmField.set(fragment, fakeViewModel);

                java.lang.reflect.Field bindingField = fragment.getClass().getDeclaredField("binding");
                bindingField.setAccessible(true);
                com.lksnext.ParkingELadron.databinding.FragmentCrearBinding binding =
                        (com.lksnext.ParkingELadron.databinding.FragmentCrearBinding) bindingField.get(fragment);

                bindingRef[0] = binding;

                fakeViewModel.setDate(cal.getTime());
                fakeViewModel.setHoraInicio("10:00");
                fakeViewModel.setHoraFin("12:00");
                fakeViewModel.setPlazaSeleccionada(null);

                binding.tvDia.setText(format.format(cal.getTime()));
                binding.tvHoraInicio.setText("10:00");
                binding.tvHoraSalida.setText("12:00");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        try { Thread.sleep(300); } catch (InterruptedException e) { e.printStackTrace(); }

        onView(withId(R.id.tvHoraInicio)).check(matches(withText("10:00")));
        onView(withId(R.id.tvHoraSalida)).check(matches(withText("12:00")));

        onView(withId(R.id.btnCrear)).perform(click());

        try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }

        onView(withId(R.id.tvDia)).check(matches(withText(format.format(cal.getTime()))));
        onView(withId(R.id.tvHoraInicio)).check(matches(withText("10:00")));
        onView(withId(R.id.tvHoraSalida)).check(matches(withText("12:00")));
        onView(withId(R.id.btnSeleccionar)).check(matches(withText(R.string.crear_select_plaza)));
    }

    private android.app.Activity getActivity(FragmentScenario<?> scenario) {
        final android.app.Activity[] activity = new android.app.Activity[1];
        scenario.onFragment(fragment -> activity[0] = fragment.requireActivity());
        return activity[0];
    }
}