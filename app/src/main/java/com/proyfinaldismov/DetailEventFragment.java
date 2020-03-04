package com.proyfinaldismov;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import static com.proyfinaldismov.MainActivity.max_dislikes;

public class DetailEventFragment extends Fragment {
    private EditText vnombre,vlat,vlong,vfechahora,vtipo,vduracion,vdescripcion;
    private TextView vcreador,vdislikes;
    private Spinner vsuscriptos,vinteresados;
    private Button save,out,going,interested,directions;
    private ImageButton dislike;
    private Evento event;
    private String user_email,nombre,tipo,descripcion,suscriptos_keys,fechahora_str,interesados_keys,creador_email,user_id;
    private Double lat,lng;
    private Integer duracion;
    private List<String> suscriptos,interesados;
    final private List<String> emails_interested = new ArrayList<>();
    final private List<String> emails_assistants= new ArrayList<>();
    private FirebaseDatabase database;
    private DatabaseReference myRef,userRef,userRefEmail,creadorRef;
    private LocalDateTime begin_date,end_date,fechahora;
    private SQLiteDatabase db_sqlite;
    private ArrayAdapter<String> adapter,adapter_interested;
    private EventsDBHelper helper;
    private List<String> tokens;
//    private List<String> uids = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.detailed_event, container, false);

        try{

            vcreador = view.findViewById(R.id.creador);
            vnombre = view.findViewById(R.id.nombre);
            vlat = view.findViewById(R.id.latitud);
            vlong = view.findViewById(R.id.longitud);
            vfechahora = view.findViewById(R.id.fechahora);
            vtipo = view.findViewById(R.id.tipo);
            vduracion = view.findViewById(R.id.duracion);
            vdescripcion = view.findViewById(R.id.descripcion);
            vsuscriptos = view.findViewById(R.id.suscriptos);
            vinteresados = view.findViewById(R.id.interesados);
            vdislikes = view.findViewById(R.id.dislikes);

            save = view.findViewById(R.id.save_event);
            out = view.findViewById(R.id.back);
            going = view.findViewById(R.id.going);
            dislike = view.findViewById(R.id.dislike);
            interested = view.findViewById(R.id.interested);
            directions = view.findViewById(R.id.directions);


            vnombre.setText(event.getNombre());
            vlat.setText(Double.valueOf(event.getLatitud()).toString());
            vlong.setText(Double.valueOf(event.getLongitud()).toString());
            vfechahora.setText(event.getFechahora());
            vtipo.setText(event.getTipo());
            vduracion.setText(Integer.valueOf(event.getDuracion()).toString());
            vdescripcion.setText(event.getDescripcion());
            vdislikes.setText(Integer.valueOf(event.getDislikes()).toString());

            database = FirebaseDatabase.getInstance();

            creadorRef = database.getReference("usuariostest").child(event.getCreador()).child("email");
            creadorRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    creador_email = dataSnapshot.getValue(String.class);
                    vcreador.setText(creador_email);
//                    Log.d("LUCSI","El creador de este evento es: " + creador_email);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            suscriptos = new ArrayList<>();
            suscriptos_keys = "";
//            emails_assistants = new ArrayList<>();
            tokens = new ArrayList<>();
            for (String key : event.getSuscriptos().keySet()) {
                if (event.getSuscriptos().get(key)){
                    userRef = database.getReference("usuariostest").child(key).child("token");
                    getRegistToken();
                    userRefEmail = database.getReference("usuariostest").child(key).child("email");
                    getAssistantEmails();
                    suscriptos.add(key); //suscriptos -> list of user uids strings (for spinner)}
                    suscriptos_keys = suscriptos_keys + key + ","; //suscriptos_keys -> comma-separated string containing user uids (for SQLite database)
                }
            }

//            Log.d("LUCSI","event id its " + event.getId());
//            userRefEmail = database.getReference("eventos").child(event.getId()).child("suscriptos");
//            userRefEmail.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    for (DataSnapshot keyNode : dataSnapshot.getChildren()){
//                        if (keyNode.getValue(Boolean.class)){
//                            Log.d("LUCSI","reading from database " + keyNode.getKey());
//                            emails_assistants.add(keyNode.getKey());
//                            suscriptos_keys = suscriptos_keys + keyNode.getKey() + ",";
//
//                        }
//
//                    }

//                }

//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });




            if (suscriptos.contains(user_id)){
//            if(emails_assistants.contains(user_email)){
                //ungoing feature
                going.setEnabled(false);
            }


//            for(int i = 0;i<suscriptos.size();++i){
//                userRef = database.getReference("usuarios").child(suscriptos.get(i));
//                getRegistToken();
//            }

            interesados = new ArrayList<>();
            interesados_keys = "";
//            emails_interested = new ArrayList<>();
            for (String key: event.getInteresados().keySet()){
                if(event.getInteresados().get(key)){
                    userRefEmail = database.getReference("usuariostest").child(key).child("email");
                        getInterestedEmails();
                    interesados.add(key);
                    interesados_keys = interesados_keys + key + ",";
                }
            }

            if(interesados.contains(user_id)){
                //uninterested feature
                interested.setEnabled(false);
            }

//            for(int k = 0;k<emails_assistants.size();++k){
//                Log.d("LUCSI","entre al for");
//            }


            out.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    quitFragment("DetailEventFrag");
                }
            });

            going.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    event.getSuscriptos().put(user_id,true);
                    updateAssistants(user_id);
                    going.setEnabled(false);
                    startPopUp();
                }
            });

            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        nombre = vnombre.getText().toString();
                        fechahora_str = vfechahora.getText().toString();
                        DateTimeFormatter form = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                        fechahora = LocalDateTime.parse(fechahora_str,form);
                        lat = Double.valueOf(vlat.getText().toString());
                        lng = Double.valueOf(vlong.getText().toString());
                        tipo = vtipo.getText().toString();
                        duracion = Integer.valueOf(vduracion.getText().toString());
                        descripcion = vdescripcion.getText().toString();

                        if (!fechahora_str.equals(event.getFechahora()) || Math.abs(lat - event.getLatitud()) > 0.0001 || Math.abs(lng -event.getLongitud()) > 0.0001) {
                            Log.d("LUCSI","CAMBIO INFO SENSIBLE");
//                        Log.d("LUCSI","BEFORE: " + event.getFechahora() + " " + event.getLatitud() + " " + event.getLongitud());
//                        Log.d("LUCSI","AFTER : " + fechahora_str + " " + lat + " " + lng);
                            for (int i = 0; i < tokens.size(); ++i) {
                                sendNotificationModifiedEvent(tokens.get(i), fechahora_str, nombre, lat, lng);
                            }
                        }
                        updateEvent(nombre, lat, lng, fechahora_str, tipo, duracion, descripcion);
                    }catch(Exception e){
                        Log.d("LUCSI",e.toString());
                    }
                }
            });

            dislike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (event.getDislikes() == max_dislikes - 1){
                        for(int i = 0;i<tokens.size();++i)
                            sendNotificationDeletedEvent(tokens.get(i),event);
                        if(isInLocal(event)){
                            deleteFromLocal(event);
                        }
                        deleteEvent(event);
                    }
                    else{
                        updateDislikes();
                    }
                }
            });

            interested.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    event.getInteresados().put(user_id,true); //HERE -> SAVE USER EMAIL INSTEAD OF USER_ID, THIS WAY POPULATE THE SPINNER WILL BE MUCH EASIER
                    //BECAUSE WE WILL ITERATE OVER THE eventid->interesados AND AFTER THIS ITERATION (INSIDE OF
                    //onDataChange) WE POPULATE THE SPINNER
                    updateInterested(user_id);
                    saveToLocal(event);
                    interested.setEnabled(false);
                    startPopUp();
                }
            });

            directions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startIntentGoogleMaps(event);
                }
            });

            return view;
        } catch (Exception e){
            Log.d("LUCSI",e.toString());
        }
        return view;
    }

    private void getInterestedEmails() {
        userRefEmail.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try{
                    emails_interested.add(dataSnapshot.getValue(String.class));
                    Log.d("LUCSI", "INTERESTED " + dataSnapshot.getValue(String.class));
                    adapter_interested = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, emails_interested);
                    adapter_interested.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    vinteresados.setAdapter(adapter_interested);
                }catch (Exception e){
                    Log.d("LUCSI", e.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }

    private void getAssistantEmails() {
        userRefEmail.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try{
                    emails_assistants.add(dataSnapshot.getValue(String.class));
                    Log.d("LUCSI", "ASSISTANT " + dataSnapshot.getValue(String.class));
                    adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, emails_assistants);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    vsuscriptos.setAdapter(adapter);
                }catch (Exception e){
                    Log.d("LUCSI", e.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }


    private void saveToLocal(Evento event) {
        Log.d("LUCSI","PRESSED ON INTERESTED, SAVING TO LOCAL DATABASE");
        try {
            ContentValues insertEvent = new ContentValues();
            insertEvent.put("id", event.getId().replace("-","")); //save id without dash symbols because its a reserved keyword in sql
//            Log.d("LUCSI",event.getId().replace("-",""));
            insertEvent.put("nombre", event.getNombre());
            insertEvent.put("latitud", event.getLatitud());
            insertEvent.put("longitud", event.getLongitud());
            insertEvent.put("fechahora", event.getFechahora());
            insertEvent.put("tipo", event.getTipo());
            insertEvent.put("duracion", event.getDuracion());
            insertEvent.put("descripcion", event.getDescripcion());
            insertEvent.put("creador", event.getCreador());
            insertEvent.put("suscriptos", suscriptos_keys);
            insertEvent.put("dislikes", event.getDislikes());
            insertEvent.put("interesados", interesados_keys);
            if (!db_sqlite.isOpen()) {
                db_sqlite = helper.getWritableDatabase();
            }
            db_sqlite.insert("Eventos", null, insertEvent);
            db_sqlite.close();

        }catch (Exception e){
            Log.d("LUCSI",e.toString());
        }
    }

    private void deleteFromLocal(Evento event) {
        try{
            if (!db_sqlite.isOpen()) {
                db_sqlite = helper.getWritableDatabase();
            }
            Log.d("LUCSI",event.getId());
            db_sqlite.delete("Eventos","id = " + "'" + event.getId().replace("-","")+ "'",null);
            db_sqlite.close();
        }catch (Exception e){
            Log.d("LUCSI",e.toString());
        }
    }

    private boolean isInLocal(Evento event) {
        return interesados.contains(user_email);
    }

    private void sendNotificationDeletedEvent(final String token,final Evento evento) {
        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        Log.d("LUCSI", "sending notification to: " + token);
                        URL url = new URL("https://fcm.googleapis.com/fcm/send");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setRequestProperty("Authorization", "key=AAAAUxCQhc8:APA91bEQFoG0G88yYG_AjyuDazObWHjdMUWDOeLl6sP3xhhHCjMmn1U_uauQR2Bed-b7zR7AMZ9d_xqIP-SI_V-BEKHVzNBNpvejcZrUHbFOMOq8D-8LA8ndZYGAIl3Q55Hw0MUGWJ9J");
                        conn.setDoOutput(true);
                        conn.setDoInput(true);

                        JSONObject jsonData = new JSONObject();
                        jsonData.put("title", "Evento eliminado");
                        jsonData.put("body", evento.getNombre());

                        JSONObject jsonParam = new JSONObject();
                        jsonParam.put("content_available", true);
                        jsonParam.put("priority", "high");
//                    jsonParam.put("registration_ids", tokens);
                        jsonParam.put("to", token);
                        jsonParam.put("notification", jsonData);


                        Log.d("LUCSI", jsonParam.toString());
                        DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                        //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                        os.writeBytes(jsonParam.toString());

                        os.flush();
                        os.close();

                        Log.d("LUCSI", String.valueOf(conn.getResponseCode()));
                        Log.d("LUCSI", conn.getResponseMessage());

                        conn.disconnect();
                    }
                    catch (IOException e) {
                        Log.d("LUCSI", e.toString());
                    }
                    catch (JSONException e) {
                        Log.d("LUCSI", e.toString());
                    }

                }
            });
            thread.start();
        }
        catch (Exception e){
            Log.d("LUCSI",e.toString());
        }
    }

    private void sendNotificationModifiedEvent(final String token, final String fechahora, final String nombre, final Double lat, final Double lng) {
        try {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Log.d("LUCSI", "sending notification to: " + token);
                    URL url = new URL("https://fcm.googleapis.com/fcm/send");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Authorization", "key=AAAAUxCQhc8:APA91bEQFoG0G88yYG_AjyuDazObWHjdMUWDOeLl6sP3xhhHCjMmn1U_uauQR2Bed-b7zR7AMZ9d_xqIP-SI_V-BEKHVzNBNpvejcZrUHbFOMOq8D-8LA8ndZYGAIl3Q55Hw0MUGWJ9J");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    JSONObject jsonData = new JSONObject();
                    jsonData.put("title", "El evento " + nombre + " fue modificado");
                    jsonData.put("body", fechahora + " " + lat.toString() + " " + lng.toString());

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("content_available", true);
                    jsonParam.put("priority", "high");
//                    jsonParam.put("registration_ids", tokens);
                    jsonParam.put("to", token);
                    jsonParam.put("notification", jsonData);


                    Log.d("LUCSI", jsonParam.toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                    os.writeBytes(jsonParam.toString());

                    os.flush();
                    os.close();

                    Log.d("LUCSI", String.valueOf(conn.getResponseCode()));
                    Log.d("LUCSI", conn.getResponseMessage());

                    conn.disconnect();
                }
                catch (IOException e) {
                    Log.d("LUCSI", e.toString());
                }
                catch (JSONException e) {
                    Log.d("LUCSI", e.toString());
                }

            }
        });
        thread.start();
//        thread.interrupt();
        }
    catch (Exception e){
        Log.d("LUCSI",e.toString());
        }
    }

    private void getRegistToken() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try{
                    tokens.add(dataSnapshot.getValue(String.class));
                    Log.d("LUCSI", "TOKEN " + dataSnapshot.getValue(String.class));
                }catch (Exception e){
                    Log.d("LUCSI", e.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }

    private void startIntentGoogleMaps(Evento event) {
        // Create a Uri from an intent string. Use the result to create an Intent.
        String uri = "google.navigation:q=" + Double.valueOf(event.getLatitud()).toString() + "," + Double.valueOf(event.getLongitud());
        Uri gmmIntentUri = Uri.parse(uri);

        // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        // Make the Intent explicit by setting the Google Maps package
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);

    }

    private void startPopUp() {
        PopUpAddToCalendar frag = new PopUpAddToCalendar();
        frag.setData(event);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction trans = fragmentManager.beginTransaction();
        trans.add(R.id.container_popup, frag, "PopUpCalendar");
        trans.commit();
    }

    public void quitFragment(String tag) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment frag = fragmentManager.findFragmentByTag(tag);
        FragmentTransaction trans = fragmentManager.beginTransaction();
        trans.remove(frag);
        trans.commit();
    }

    public void setData(Evento event, String user_email, SQLiteDatabase db, EventsDBHelper helper, String user_id) {
        this.event = event;
        this.user_email = user_email;
        this.db_sqlite = db;
        this.helper = helper;
        this.user_id = user_id;
    }

    private void updateDislikes() {
        try{
            Log.d("LUCSI","updatin dislikes");
            database = FirebaseDatabase.getInstance();
            myRef = database.getReference("eventos");

            Integer dislikes = event.getDislikes() + 1;
            event.dislikes = dislikes;

            Map<String,Object> update = new HashMap<>();
            update.put("dislikes",dislikes);

            DatabaseReference eventRef = myRef.child(event.id);
            eventRef.updateChildren(update);

            //update view
            vdislikes.setText(Integer.valueOf(dislikes).toString());
        }catch(Exception e){
            Log.d("LUCSI",e.toString());
        }

    }

    private void deleteEvent(Evento event) {
        try{
            Log.d("LUCSI","deletin event: " + event.getId());
            database = FirebaseDatabase.getInstance();
            myRef = database.getReference("eventos");
            DatabaseReference eventRef = myRef.child(event.getId());
            eventRef.removeValue();
            Toast.makeText(getActivity(),"El evento " + event.getNombre() + " fue eliminado",Toast.LENGTH_LONG).show();
//            events.remove(i);
            quitFragment("DetailEventFrag");
        }
        catch (Exception e){
            Log.d("LUCSI",e.toString());
        }

    }

    private void updateEvent(String nombre, Double lat, Double lng, String fechahora, String tipo, Integer duracion, String descripcion) {
        try{
            database = FirebaseDatabase.getInstance();
            myRef = database.getReference("eventos");
            Map<String,Object> update = new HashMap<>();
            update.put("nombre",nombre);
            update.put("latitud",lat);
            update.put("longitud",lng);
            update.put("fechahora",fechahora);
            update.put("tipo",tipo);
            update.put("duracion",duracion);
            update.put("descripcion",descripcion);

            DatabaseReference eventRef = myRef.child(event.id);
            eventRef.updateChildren(update);
        }
        catch (Exception e){
            Log.d("LUCSI",e.toString());
        }

    }

    private void updateInterested(String user_id){
        try{
            Log.d("LUCSI",user_id);
            database = FirebaseDatabase.getInstance();
            myRef = database.getReference("eventos");
            Map<String,Object> update = new HashMap<>();
            update.put("interesados",event.interesados);
            DatabaseReference eventRef = myRef.child(event.id);
            eventRef.updateChildren(update);
//            update view
//            adapter_interested.add(user_id);
//            vinteresados.setAdapter(adapter_interested);
        }
        catch (Exception e){
            Log.d("LUCSI",e.toString());
        }

    }

    private void updateAssistants(String user_id) {
        try{
            Log.d("LUCSI",user_id);
            database = FirebaseDatabase.getInstance();
            myRef = database.getReference("eventos");
            Map<String,Object> update = new HashMap<>();
            update.put("suscriptos",event.getSuscriptos());
            Log.d("LUCSI",event.getId());
            DatabaseReference eventRef = myRef.child(event.getId());
            eventRef.updateChildren(update);

//            update view
//            adapter.add(user_id);
//            vsuscriptos.setAdapter(adapter);
        }
        catch (Exception e){
            Log.d("LUCSI",e.toString());
        }
    }

}
