package com.example.usrdel.androidrestapi

import java.util.*
import kotlin.collections.ArrayList

class Entrenador(var nombre: String,
                 var apellido: String,
                 var medallas: Int,
                 var edad: String,
                 var createdAt: Long,
                 var updatedAt: Long,
                 var id: Int,
                 var pokemons: ArrayList<Pokemon> = ArrayList()) {

    var createdAtDate = Date(createdAt)
    var updatedAtDate = Date(updatedAt)

}