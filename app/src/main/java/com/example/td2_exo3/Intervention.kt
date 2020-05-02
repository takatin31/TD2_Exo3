package com.example.td2_exo3

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "Intervention")
data class Intervention (@PrimaryKey val numero : Int, val date : String, val plombier : Int, val type : Int) : Serializable{
}