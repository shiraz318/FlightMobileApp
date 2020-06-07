package com.example.flightmobileapp

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "url_table")

class URL(
    @PrimaryKey @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "time") val time: String
) {

}