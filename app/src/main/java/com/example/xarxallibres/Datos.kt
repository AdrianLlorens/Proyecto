package com.example.xarxallibres

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://xyztewvozclvxflbzdlk.supabase.co",
        supabaseKey = "sb_publishable_9PjrBUOhmEjxXlLKhOB1Pg_gccjzd_W"
    ) {
        install(Postgrest)
    }
}


@Serializable
data class User(
    @SerialName("user_id") val userId: Long,
    val username: String,
    val password: String
)


@Serializable
data class Libro(
    @SerialName("libro_id") val libroId: Long,
    val titulo: String,
    @SerialName("ISBN") val isbn: String,
    @SerialName("Autor") val autor: String?,
    @SerialName("Editorial") val editorial: String?
)