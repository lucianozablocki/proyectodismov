package com.proyfinaldismov;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class PopUpAddToCalendar extends Fragment {
    private Button vyes,vno;
    private Evento event_received;
    private LocalDateTime begin_date,end_date;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pop_up_calendar,container,false);

        vyes = view.findViewById(R.id.button_yes);
        vno = view.findViewById(R.id.button_no);

        vyes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEventToCalendar(event_received);
                quitFragment("PopUpCalendar");
            }
        });

        vno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quitFragment("PopUpCalendar");
            }
        });

        return view;
    }

    public void setData(Evento event){
        this.event_received = event;
    }

    public void addEventToCalendar(Evento event) {
        try {
            DateTimeFormatter form = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            begin_date = LocalDateTime.parse(event.getFechahora(),form);
            Calendar beginTime = Calendar.getInstance();
            beginTime.set(begin_date.getYear(), begin_date.getMonthValue(), begin_date.getDayOfMonth(), begin_date.getHour(), begin_date.getMinute());
            Calendar endTime = Calendar.getInstance();
            end_date = begin_date.plusHours(event.getDuracion());
            endTime.set(end_date.getYear(), end_date.getMonthValue(), end_date.getDayOfMonth(), end_date.getHour(), end_date.getMinute());
            Intent intent = new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                    .putExtra(CalendarContract.Events.TITLE, event.getNombre())
                    .putExtra(CalendarContract.Events.DESCRIPTION, event.getTipo())
                    .putExtra(CalendarContract.Events.EVENT_LOCATION, Double.toString(event.getLatitud()) + " " + Double.toString(event.getLongitud()))
                    .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
            startActivity(intent);
        }
        catch (Exception e){
            Log.d("LUCSI",e.toString());
        }

    }

    public void quitFragment(String tag) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment frag = fragmentManager.findFragmentByTag(tag);
        FragmentTransaction trans = fragmentManager.beginTransaction();
        trans.remove(frag);
        trans.commit();
    }

}
