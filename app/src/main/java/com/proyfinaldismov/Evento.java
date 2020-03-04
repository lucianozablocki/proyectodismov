package com.proyfinaldismov;

import java.util.Map;

public class Evento {
    String nombre,tipo,descripcion,creador;
//    LocalDateTime fechahora;
    Integer duracion;
    Double latitud,longitud;
    Map<String,Boolean> suscriptos,interesados;
//    List<String,Boolean> suscriptos;
    Integer dislikes;
    String fechahora;
    String id;

    public Evento(String id, String nombre, Double latitud, Double longitud, String fechahora, String tipo, Integer duracion, String descripcion,
                    Map<String,Boolean> suscriptos, Map<String,Boolean> interesados, String creador, Integer dislikes){
        this.id = id;
        this.nombre = nombre;
        this.latitud = latitud;
        this.longitud = longitud;
        this.fechahora = fechahora;
        this.tipo = tipo;
        this.duracion = duracion;
        this.descripcion = descripcion;
        this.creador = creador;
        this.suscriptos = suscriptos;
        this.interesados = interesados;
        this.dislikes = dislikes;
    }

    public Evento(String nombre, Double latitud, Double longitud, String fechahora, String tipo, Integer duracion, String descripcion,
                  Map<String,Boolean> suscriptos, Map<String,Boolean> interesados, String creador, Integer dislikes){
        this.nombre = nombre;
        this.latitud = latitud;
        this.longitud = longitud;
        this.fechahora = fechahora;
        this.tipo = tipo;
        this.duracion = duracion;
        this.descripcion = descripcion;
        this.creador = creador;
        this.suscriptos = suscriptos;
        this.interesados = interesados;
        this.dislikes = dislikes;
    }

    public Evento(){}

    public String getId(){return id;}
    public String getNombre(){return nombre;}
    public Double getLatitud(){return latitud;}
    public Double getLongitud(){return longitud;}
    public String getFechahora(){return fechahora;}
    public String getTipo(){return tipo;}
    public Integer getDuracion(){return duracion;}
    public String getDescripcion(){return descripcion;}
    public Integer getDislikes(){return dislikes;}
    public Map<String,Boolean> getSuscriptos(){return suscriptos;}
    public Map<String,Boolean> getInteresados(){return  interesados;}
    public String getCreador(){return creador;}

//    public Map<String,Object> to_json(){
//        Map<String,Object> map = new HashMap<>();
//        map.put("nombre",nombre);
//        map.put("latitud",latitud);
//        map.put("longitud",longitud);
//        map.put("fechahora",fechahora);
//        map.put("tipo",tipo);
//        map.put("duracion",duracion);
//        map.put("descripcion",descripcion);
//        return map;
//    }



}
