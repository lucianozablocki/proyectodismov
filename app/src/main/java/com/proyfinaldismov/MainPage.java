package com.proyfinaldismov;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolLongClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import static com.proyfinaldismov.MainActivity.max_dislikes;


public class MainPage extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener, MapboxMap.OnMapClickListener {
    Button logout, mkevent;
    FirebaseAuth mAuth;
    private MapView mapView;
    private MapboxMap map;
    private PermissionsManager permissionsManager;
    private static final String ICON_ID = "ICON_ID";
    private static final String ICON_ID_GREEN = "ICON_ID_GREEN";
    private SymbolManager symbolManager;
    private List<Evento> events = new ArrayList<>();
    private FirebaseDatabase database;
    private DatabaseReference myRef,userRef;
    private Bitmap bm,bm_green;
    private String user_email,user_id;
    private EventsDBHelper helper;
    private SQLiteDatabase db;
    private List<String> tokens = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(getApplicationContext(), getString(R.string.mapbox_access_token));
        setContentView(R.layout.main);

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("LUCSI", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token and save it to database. key: user uid value: token -> here, look first if an existing user uid key its present on database,
                        // possible problem of this: save a new token for the same user id (update token)
                        String token = task.getResult().getToken();
                        try{
                            Log.d("LUCSI", "REGISTRATION TOKEN ITS: " + token);
                            database = FirebaseDatabase.getInstance();
                            DatabaseReference userRef = database.getReference("usuarios");
                            userRef.child(mAuth.getUid()).child("token").setValue(token);
                        }
                        catch(Exception e){
                            Log.d("LUCSI",e.toString());
                        }

                    }
                });

        helper = new EventsDBHelper(this, "Eventos", null, 1);

        logout = findViewById(R.id.button_logout);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.getInstance().signOut();
                Intent i = new Intent(MainPage.this, LoginActivity.class);
                startActivity(i);
            }
        });

        bm = BitmapFactory.decodeResource(getResources(), R.drawable.red_marker);
        bm_green = BitmapFactory.decodeResource(getResources(), R.drawable.green_marker);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();

        mkevent = findViewById(R.id.button_mkevent);
        mkevent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationComponent locationComponent = map.getLocationComponent();
                Location last_location = locationComponent.getLastKnownLocation();
                Log.d("lucsi",Double.toString(last_location.getLatitude()));
                Log.d("lucsi",Double.toString(last_location.getLongitude()));

                // Add symbol at specified lat/lon
                map.getStyle().addImage(ICON_ID, bm);
                symbolManager.create(new SymbolOptions()
                        .withLatLng(new LatLng(last_location.getLatitude(), last_location.getLongitude()))
                        .withIconImage(ICON_ID)
                        .withIconSize(0.1f));

                FirebaseUser user = mAuth.getCurrentUser();
                user_email = user.getEmail();
                user_id = user.getUid();
                EventFragment frag = new EventFragment();
                frag.setData(last_location.getLatitude(),last_location.getLongitude(),user_email, user_id);

                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction trans = fragmentManager.beginTransaction();
                trans.add(R.id.container_fragment, frag, "EventFrag");
                trans.commit();
            }
        });

    }

    private Evento findEvent(LatLng point) {
        if (events != null){
            for (int i = 0; i<events.size();++i){
                if (point.getLatitude() == events.get(i).latitud && point.getLongitude() == events.get(i).longitud){
                    return events.get(i);
                }
            }
        }
        return null;

    }

    private void readAndDrawEvents(final MapboxMap map) {

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try{
                    Log.d("LUCSI","UPDATING AND REDRAWING EVENTS");
                    symbolManager.deleteAll();
                    events.clear();
                    int i = 0;
                    List<Symbol> list = new ArrayList<>();
                    Symbol s;

                    //READ FIREBASE DATABASE
                    for (DataSnapshot keyNode: dataSnapshot.getChildren()){
                        events.add(keyNode.getValue(Evento.class));
                        events.get(i).id = keyNode.getKey();

                        map.getStyle().addImage(ICON_ID, bm);
                        s = symbolManager.create(new SymbolOptions()
                                .withLatLng(new LatLng(events.get(i).getLatitud(), events.get(i).getLongitud()))
                                .withIconImage(ICON_ID)
                                .withIconSize(0.1f));
                        list.add(s);
                        ++i;
                    }

                    //READ LOCAL DATABASE -> pending work, draw one time every markers, without solaping. dont draw and then delete
                    db = helper.getReadableDatabase();
                    Double lat_local,lng_local;
                    Cursor c = db.rawQuery("SELECT latitud,longitud,id FROM Eventos",null);

                    if (c!= null && c.moveToFirst()) {
                        do {
                            lat_local = c.getDouble(0);
                            lng_local = c.getDouble(1);
                            String id_local = c.getString(2);
                            LatLng latlng_local = new LatLng(lat_local,lng_local);
                            Log.d("LUCSI","READING LOCAL EVENT ON " + Double.valueOf(lat_local).toString() +  " " + Double.valueOf(lng_local).toString());
//                            Log.d("LUCSI","ID OF EVENT IS " + id_local );
                            map.getStyle().addImage(ICON_ID_GREEN, bm_green);
                            symbolManager.create(new SymbolOptions()
                                    .withLatLng(new LatLng(lat_local, lng_local))
                                    .withIconImage(ICON_ID_GREEN)
                                    .withIconSize(0.1f));
                        } while (c.moveToNext());
                    }
                 }catch (Exception e){
                    Log.d("LUCSI",e.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getRegistToken() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try{
                    tokens.add(dataSnapshot.getValue(String.class));
                  Log.d("LUCSI", "POP DATASNAPSHOT " + dataSnapshot.getValue(String.class));
              }catch (Exception e){
                    Log.d("LUCSI", e.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }

    public void quitFragment(String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment frag = fragmentManager.findFragmentByTag(tag);
        FragmentTransaction trans = fragmentManager.beginTransaction();
        trans.remove(frag);
        trans.commit();
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        MainPage.this.map = mapboxMap;
        map.addOnMapClickListener(this);
        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/streets-v10"),
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        enableLocation(style);
                        symbolManager = new SymbolManager(mapView, map, style);
                        symbolManager.setIconAllowOverlap(true);
                        symbolManager.setIconIgnorePlacement(true);
//                        symbolManager.addClickListener(new OnSymbolClickListener() {
//                            @Override
//                            public void onAnnotationClick(Symbol symbol) {
//                                Log.d("LUCSI","onAnotationclick");
//
//                            }
//                        });
                        // Add long click listener
                        symbolManager.addLongClickListener((new OnSymbolLongClickListener() {
                            @Override
                            public void onAnnotationLongClick(Symbol symbol) {
                                LatLng point = symbol.getLatLng();
                                Evento event = findEvent(point);
                                if (event != null) {
                                    Log.d("LUCSI", "you pressed on event named: " + event.getNombre());
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    user_email = user.getEmail(); //HERE -> PASS USER EMAIL TO FRAGMENT INSTEAD OF USER UID
                                    user_id = user.getUid();
                                    db = helper.getWritableDatabase();
                                    DetailEventFragment frag = new DetailEventFragment();
                                    frag.setData(event, user_email, db, helper,user_id);
                                    FragmentManager fragmentManager = getSupportFragmentManager();
                                    FragmentTransaction trans = fragmentManager.beginTransaction();
                                    trans.add(R.id.container_fragment, frag, "DetailEventFrag");
                                    trans.commit();
                                }
                                else{
                                    Log.d("LUCSI","event not found!");
                                }

                            }
                        }));
                    }
                });

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("eventos");
        readAndDrawEvents(map);

        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(2, 2))
                .zoom(15)
                .tilt(20)
                .build();
        mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));

    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {

        // Add symbol at specified lat/lon
        map.getStyle().addImage(ICON_ID, bm);
        symbolManager.create(new SymbolOptions()
                .withLatLng(new LatLng(point.getLatitude(), point.getLongitude()))
                .withIconImage(ICON_ID)
                .withIconSize(0.1f));

        //ask data from user, save event on sqlite and also on firebase
        FirebaseUser user = mAuth.getCurrentUser();
        user_email = user.getEmail();
        user_id = user.getUid();
        EventFragment frag = new EventFragment();
        frag.setData(point.getLatitude(),point.getLongitude(),user_email,user_id);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction trans = fragmentManager.beginTransaction();
        trans.add(R.id.container_fragment, frag, "EventFrag");
        trans.commit();
        return true;
    }

    @SuppressWarnings({"MissingPermission"})
    private void enableLocation(@NonNull Style style) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Get an instance of the component
            LocationComponent locationComponent = map.getLocationComponent();
            // Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, style).build());
            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);
            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);
            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        //this method's called when the user denies access.
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            map.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocation(style);
                }
            });
        } else {
            Toast.makeText(this, "Porfavor dame permisos", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
