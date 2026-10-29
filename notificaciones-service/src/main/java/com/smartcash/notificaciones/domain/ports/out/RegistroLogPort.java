package com.smartcash.notificaciones.domain.ports.out;

/**
 * Puerto de salida: permite registrar errores y eventos en el microservicio central de logs
 * sin que la capa de aplicacion conozca como se envian (principio de inversion de dependencias).
 */
public interface RegistroLogPort {

    void registrar(String nivel, String mensaje, String detalle,
                   String ruta, String metodo, Integer estadoHttp, String usuario);
}
