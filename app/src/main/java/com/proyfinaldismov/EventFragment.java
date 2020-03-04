package com.proyfinaldismov;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


public class EventFragment extends Fragment {
    private Double lat,lng;
    private Button save,out;
    private EditText vnombre,vfechahora,vtipo,vduracion,vdescripcion;
    private String nombre,tipo,fechahora_str,descripcion,user_email,user_id;
    private LocalDateTime fechahora;
    private Integer duracion;
    private Map<String,Boolean> suscriptos = new HashMap<>();
    private Map<String,Boolean> interesados = new HashMap<>();
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_event,container,false);

        save = view.findViewById(R.id.save_event);
        out = view.findViewById(R.id.back);

        vnombre = view.findViewById(R.id.nombre);
        vfechahora = view.findViewById(R.id.fechahora);
        vtipo = view.findViewById(R.id.tipo);
        vduracion = view.findViewById(R.id.duracion);
        vdescripcion = view.findViewById(R.id.descripcion);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    nombre = vnombre.getText().toString();
                    fechahora_str = vfechahora.getText().toString();
                    DateTimeFormatter form = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    fechahora = LocalDateTime.parse(fechahora_str,form);
                    tipo = vtipo.getText().toString();
                    duracion = Integer.valueOf(vduracion.getText().toString());
                    descripcion = vdescripcion.getText().toString();
                    suscriptos.put("0A",false);
                    interesados.put("0A",false);

                    Evento event = new Evento(nombre,lat,lng,fechahora_str,tipo,duracion,descripcion,suscriptos,interesados,user_id,0);
                    saveEvent(event);
                }
                catch (Exception e){
                    Log.d("LUCSI",e.toString());
                }
            }
        });

        out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                Fragment frag = fragmentManager.findFragmentByTag("EventFrag");
                FragmentTransaction trans = fragmentManager.beginTransaction();
                trans.remove(frag);
                trans.commit();
            }
        });
        return view;
    }

    private void saveEvent(Evento event) {
        try{
            database = FirebaseDatabase.getInstance();
            myRef = database.getReference("eventos");
            DatabaseReference newEventRef = myRef.push();
            newEventRef.setValue(event);
            Toast.makeText(getActivity(),"Se creo el evento " + event.getNombre(),1).show();
            quitFragment("EventFrag");
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

    public void setData(Double latitude, Double longitude, String user_email,String user_id) {
        this.lat = latitude;
        this.lng = longitude;
        this.user_email = user_email;
        this.user_id = user_id;
    }
}
