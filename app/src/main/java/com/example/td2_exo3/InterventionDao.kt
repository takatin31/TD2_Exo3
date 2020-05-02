package com.example.td2_exo3


import androidx.room.*

@Dao
interface InterventionDao {


    @Query("Select * from Intervention")
    fun loadAllIntervention(): List<Intervention>

    @Query("Select * from Intervention where numero = :interventionId")
    fun findIntervention(interventionId : Int): List<Intervention>

    @Query("Select max(numero) from Intervention")
    fun getNextNumero(): List<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addIntervention(seance: Intervention)

    @Update
    fun modifyIntervention(seance: Intervention)

    @Delete
    fun deleteIntervention(seance: Intervention)

}
