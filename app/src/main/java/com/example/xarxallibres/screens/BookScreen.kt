package com.example.xarxallibres

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jan.supabase.postgrest.from

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksScreen(userId: Long, onBackClick: () -> Unit) {
    var allBooks by remember { mutableStateOf<List<Libro>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var query by remember { mutableStateOf("") }
    var selectedEditorial by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        try {
            val client = SupabaseClient.client

            val alumnos = client.from("Alumnos").select {
                filter { eq("user_id", userId) }
            }.decodeList<Alumno>()

            if (alumnos.isNotEmpty()) {
                val cursoId = alumnos.first().cursoId
                val asignaturas = client.from("Asignaturas").select {
                    filter { eq("curso_id", cursoId) }
                }.decodeList<Asignatura>()

                val idsLibros = asignaturas.map { it.libroId }

                if (idsLibros.isNotEmpty()) {
                    val librosResult = client.from("Libro").select {
                        filter { isIn("libro_id", idsLibros) }
                    }.decodeList<Libro>()
                    allBooks = librosResult
                } else {
                    allBooks = emptyList()
                }
            } else {
                allBooks = emptyList()
            }
        } catch (e: Exception) {
            println(e.message)
        } finally {
            isLoading = false
        }
    }

    val editorials = remember(allBooks) {
        allBooks.mapNotNull { it.editorial }.distinct().sorted()
    }

    val filteredBooks = remember(allBooks, query, selectedEditorial) {
        allBooks.filter { book ->
            val matchesSearch = query.isBlank() ||
                    book.titulo.contains(query, ignoreCase = true) ||
                    (book.autor?.contains(query, ignoreCase = true) == true) ||
                    book.isbn.contains(query, ignoreCase = true)

            val matchesEditorial = selectedEditorial == null || book.editorial == selectedEditorial

            matchesSearch && matchesEditorial
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Libros", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E)),
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                }
            )
        },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar título, autor o ISBN") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Borrar", tint = Color.Gray)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF2C2C2C),
                    unfocusedContainerColor = Color(0xFF2C2C2C),
                    cursorColor = Color(0xFF0D47A1),
                    focusedBorderColor = Color(0xFF0D47A1),
                    unfocusedBorderColor = Color.Gray
                )
            )

            if (editorials.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedEditorial == null,
                            onClick = { selectedEditorial = null },
                            label = { Text("Todos") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0D47A1),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF2C2C2C),
                                labelColor = Color.White
                            )
                        )
                    }
                    items(editorials) { editorial ->
                        FilterChip(
                            selected = selectedEditorial == editorial,
                            onClick = {
                                selectedEditorial = if (selectedEditorial == editorial) null else editorial
                            },
                            label = { Text(editorial) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0D47A1),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF2C2C2C),
                                labelColor = Color.White
                            )
                        )
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF0D47A1))
                }
            } else if (filteredBooks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontraron libros", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredBooks) { libro ->
                        ItemLibro(libro)
                    }
                }
            }
        }
    }
}

@Composable
fun ItemLibro(libro: Libro) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Book,
                contentDescription = null,
                tint = Color(0xFF0D47A1),
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = libro.titulo, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = "Autor: ${libro.autor ?: "Desconocido"}", color = Color.Gray)
                if (!libro.editorial.isNullOrEmpty()) {
                    Text(text = "Editorial: ${libro.editorial}", fontSize = 12.sp, color = Color(0xFF90CAF9))
                }
                Text(text = "ISBN: ${libro.isbn}", fontSize = 12.sp, color = Color.DarkGray)
            }
        }
    }
}