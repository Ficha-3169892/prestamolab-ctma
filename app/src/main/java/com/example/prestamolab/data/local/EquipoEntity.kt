package com.example.prestamolab.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.prestamolab.model.CategoriaEquipo
import com.example.prestamolab.model.EstadoEquipo
import com.example.prestamolab.model.Equipo

@Entity(tableName = "equipos")
data class EquipoEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val categoria: String,
    val estado: String
)

fun EquipoEntity.toDomain(): Equipo {
    return Equipo(
        id = id,
        nombre = nombre,
        categoria = try { CategoriaEquipo.valueOf(categoria) } catch (e: Exception) { CategoriaEquipo.OTRO },
        estado = try { EstadoEquipo.valueOf(estado) } catch (e: Exception) { EstadoEquipo.DISPONIBLE }
    )
}

fun Equipo.toEntity(): EquipoEntity {
    return EquipoEntity(
        id = id,
        nombre = nombre,
        categoria = categoria.name,
        estado = estado.name
    )
}
