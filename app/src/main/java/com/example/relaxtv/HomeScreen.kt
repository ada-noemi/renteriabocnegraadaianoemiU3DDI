@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.example.relaxtv

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text

internal val Night = Color(0xFF081522)
internal val Accent = Color(0xFF80DECF)

@Composable
fun HomeScreen(lastSelected: String, onSelect: (Ambience) -> Unit) {
    val requester = remember { FocusRequester() }
    LaunchedEffect(Unit) { requester.requestFocus() }
    Column(Modifier.fillMaxSize().background(Night).padding(horizontal = 48.dp, vertical = 32.dp)) {
        Text("RelaxTV", color = Color.White, fontSize = 34.sp)
        Spacer(Modifier.height(8.dp))
        Text("Elige tu ambiente y disfruta de un momento de calma", color = Color(0xFFB7C9D8), fontSize = 18.sp)
        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            ambiences.forEach { ambience ->
                var focused by remember { mutableStateOf(false) }
                val shape = RoundedCornerShape(18.dp)
                Column(Modifier.weight(1f)
                    .then(if (ambience.id == lastSelected) Modifier.focusRequester(requester) else Modifier)
                    .onFocusChanged { focused = it.isFocused }.clip(shape)
                    .background(if (focused) Color(0xFF203F50) else Color(0xFF122737))
                    .border(if (focused) 3.dp else 1.dp, if (focused) Accent else Color(0xFF294252), shape)
                    .clickable { onSelect(ambience) }) {
                    Image(painterResource(ambience.image), ambience.title, Modifier.fillMaxWidth().aspectRatio(1.8f), contentScale = ContentScale.Crop)
                    Column(Modifier.padding(16.dp)) {
                        Text(ambience.title, color = Color.White, fontSize = 24.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(ambience.description, color = Color(0xFFB7C9D8), fontSize = 14.sp)
                    }
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Text("← →  Elegir ambiente     •     OK  Reproducir", color = Accent, fontSize = 16.sp)
    }
}
