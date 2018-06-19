package com.example.usrdel.androidrestapi

import java.util.*

class Pokemon (var nombre: String,
               var numeroPokemon: Int,
               var tipo: String,
               var createdAt: Long,
               var updatedAt: Long,
               var id: Int,
               var entrenadorId: Int? = null) {

    var createdAtDate = Date(createdAt)
    var updatedAtDate = Date(updatedAt)

}