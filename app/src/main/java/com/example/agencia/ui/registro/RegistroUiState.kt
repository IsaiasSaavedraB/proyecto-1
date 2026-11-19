package com.example.agencia.ui.registro

data class RegistroUiState(
    // Variables
    val nombre: String = "",
    val apellido: String = "",
    val correo: String = "",
    val password: String = "",
    val repetirPassword: String = "",
    val aceptaTerminos: Boolean = false,

    // Estados de error
    val errorNombre: Boolean = false,
    val errorApellido: Boolean = false,
    val errorCorreo: Boolean = false,
    val errorPassword: Boolean = false,
    val errorRepetirPassword: Boolean = false,
    val errorTerminos: Boolean = false,

    // Estado de exito y carga
    val registroExitoso: Boolean = false,
    val cargando: Boolean = false,
    val mensajeErrorGeneral: String? = null
)