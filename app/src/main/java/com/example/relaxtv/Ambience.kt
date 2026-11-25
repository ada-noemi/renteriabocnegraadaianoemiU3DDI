package com.example.relaxtv

import androidx.annotation.DrawableRes

data class Ambience(val id: String, val title: String, val description: String, @DrawableRes val image: Int) {
    val assetPath get() = "videos/$id.mp4"
}
val ambiences = listOf(
    Ambience("lluvia", "Lluvia", "Una pausa al ritmo de la lluvia", R.drawable.ambience_rain),
    Ambience("chimenea", "Chimenea", "La calidez de un momento tranquilo", R.drawable.ambience_fire),
    Ambience("oceano", "Océano", "Respira al compás de las olas", R.drawable.ambience_ocean)
)
