package com.proyfinaldismov;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class EventsDBHelper extends SQLiteOpenHelper {
    String create = "CREATE TABLE Eventos (id TEXT, nombre TEXT, latitud REAL, longitud REAL, " +
            "fechahora TEXT, tipo TEXT, duracion INTEGER,descripcion TEXT, creador TEXT, suscriptos TEXT, interesados TEXT, dislikes INTEGER)";

    public EventsDBHelper(Context context, String nombre, SQLiteDatabase.CursorFactory cursor, int version){
        super(context,nombre,cursor,version);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("LUCSI","into onCreate method of dbhelper");
        db.execSQL(create);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("LUCSI","into onUpgrade method of dbhelper");
        db.execSQL("DROP TABLE IF EXISTS Eventos");
        db.execSQL(create);
    }
}
