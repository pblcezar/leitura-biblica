package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.InitialBibleData
import com.example.data.model.Book
import com.example.engine.CustomReadingSelection
import com.example.ui.components.getScopeText
import com.example.ui.viewmodel.BiblePlanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlanScreen(
    viewModel: BiblePlanViewModel,
    onBackClick: () -> Unit,
    onPlanCreated: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Padrão/Rápido, 1 = Customizado

    // State for Standard Mode
    var title by remember { mutableStateOf("Bíblia Completa em 1 Ano") }
    var selectedScope by remember { mutableStateOf("ALL") }
    var totalDays by remember { mutableFloatStateOf(365f) }

    // State for Custom Mode
    var customTitle by remember { mutableStateOf("Meu Plano Customizado") }
    var customTotalDays by remember { mutableFloatStateOf(30f) }
    val customSelections = remember { mutableStateListOf<CustomReadingSelection>() }

    val scopeChapters = when (selectedScope) {
        "OLD" -> 929
        "NEW" -> 260
        "PSALMS_PROVERBS" -> 181
        else -> 1189 // ALL
    }

    val chaptersPerDay = String.format("%.1f", scopeChapters / totalDays.coerceAtLeast(1f))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Criar Novo Plano",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Mode Selector Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Planos Rápidos", fontWeight = FontWeight.Bold)
                        }
                    }
                )

                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Customizado", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                if (selectedTab == 0) {
                    // Standard / Quick Preset Mode
                    Text(
                        text = "Opções Pré-definidas",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Escolha um dos planos mais populares para começar instantaneamente:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Preset 1: Bíblia Completa 365
                    PresetCard(
                        title = "Bíblia Completa em 365 dias",
                        description = "Leia toda a Bíblia de Gênesis a Apocalipse em um ano.",
                        scope = "ALL",
                        days = 365,
                        icon = Icons.Default.Book,
                        isSelected = selectedScope == "ALL" && totalDays.toInt() == 365,
                        onSelect = {
                            title = "Bíblia Completa em 365 dias"
                            selectedScope = "ALL"
                            totalDays = 365f
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset 2: Novo Testamento 90
                    PresetCard(
                        title = "Novo Testamento em 90 dias",
                        description = "Cumpra os 27 livros do Novo Testamento em três meses.",
                        scope = "NEW",
                        days = 90,
                        icon = Icons.Default.AutoAwesome,
                        isSelected = selectedScope == "NEW" && totalDays.toInt() == 90,
                        onSelect = {
                            title = "Novo Testamento em 90 dias"
                            selectedScope = "NEW"
                            totalDays = 90f
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset 3: Salmos e Provérbios 30
                    PresetCard(
                        title = "Salmos e Provérbios em 30 dias",
                        description = "30 dias de sabedoria e oração através da poesia bíblica.",
                        scope = "PSALMS_PROVERBS",
                        days = 30,
                        icon = Icons.Default.Favorite,
                        isSelected = selectedScope == "PSALMS_PROVERBS" && totalDays.toInt() == 30,
                        onSelect = {
                            title = "Salmos e Provérbios em 30 dias"
                            selectedScope = "PSALMS_PROVERBS"
                            totalDays = 30f
                        }
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Personalizar Escopo
                    Text(
                        text = "Ou Escolha por Escopo",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Scope Options
                    Text(
                        text = "Escopo de Leitura:",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScopeChip(
                            label = "Toda a Bíblia",
                            selected = selectedScope == "ALL",
                            onClick = {
                                selectedScope = "ALL"
                                if (title.isEmpty() || title.contains("Novo") || title.contains("Salmos")) {
                                    title = "Plano da Bíblia Completa"
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )

                        ScopeChip(
                            label = "Novo Test.",
                            selected = selectedScope == "NEW",
                            onClick = {
                                selectedScope = "NEW"
                                title = "Plano do Novo Testamento"
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScopeChip(
                            label = "Antigo Test.",
                            selected = selectedScope == "OLD",
                            onClick = {
                                selectedScope = "OLD"
                                title = "Plano do Antigo Testamento"
                            },
                            modifier = Modifier.weight(1f)
                        )

                        ScopeChip(
                            label = "Salmos/Prov.",
                            selected = selectedScope == "PSALMS_PROVERBS",
                            onClick = {
                                selectedScope = "PSALMS_PROVERBS"
                                title = "Plano Salmos e Provérbios"
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Title Field
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Nome do Plano") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_plan_title"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Days Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Duração em Dias:",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${totalDays.toInt()} dias",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Slider(
                        value = totalDays,
                        onValueChange = { totalDays = it },
                        valueRange = 10f..365f,
                        steps = 70,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quick days chip shortcuts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(30, 60, 90, 180, 365).forEach { d ->
                            Surface(
                                onClick = { totalDays = d.toFloat() },
                                shape = RoundedCornerShape(20.dp),
                                color = if (totalDays.toInt() == d) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                                    Text(
                                        text = "${d}d",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (totalDays.toInt() == d) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Pace preview card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "Ritmo",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Ritmo Diário Estimado:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "~$chaptersPerDay capítulos por dia ($scopeChapters caps totais)",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Serif
                                    ),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Create Plan Submit Button
                    Button(
                        onClick = {
                            if (title.isBlank()) title = "Meu Plano de Leitura"
                            viewModel.createNewPlan(
                                title = title.trim(),
                                scope = selectedScope,
                                totalDays = totalDays.toInt(),
                                startDate = System.currentTimeMillis(),
                                onCreated = { newPlanId ->
                                    onPlanCreated(newPlanId)
                                }
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("create_plan_submit")
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Gerar Plano")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gerar e Iniciar Plano",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                } else {
                    // Custom Mode: Select Books, Chapter Ranges, and Custom Sequence
                    CustomPlanBuilderSection(
                        customTitle = customTitle,
                        onCustomTitleChange = { customTitle = it },
                        customTotalDays = customTotalDays,
                        onCustomTotalDaysChange = { customTotalDays = it },
                        customSelections = customSelections,
                        onAddSelection = { selection ->
                            customSelections.add(selection)
                            if (customTitle == "Meu Plano Customizado" && customSelections.isNotEmpty()) {
                                customTitle = "Plano: " + customSelections.take(2).joinToString(" + ") { it.bookName }
                            }
                        },
                        onRemoveSelection = { index ->
                            if (index in customSelections.indices) {
                                customSelections.removeAt(index)
                            }
                        },
                        onMoveUp = { index ->
                            if (index > 0 && index < customSelections.size) {
                                val item = customSelections.removeAt(index)
                                customSelections.add(index - 1, item)
                            }
                        },
                        onMoveDown = { index ->
                            if (index >= 0 && index < customSelections.size - 1) {
                                val item = customSelections.removeAt(index)
                                customSelections.add(index + 1, item)
                            }
                        },
                        onSubmitCustomPlan = {
                            if (customSelections.isNotEmpty()) {
                                val finalTitle = if (customTitle.isBlank()) "Plano Customizado" else customTitle.trim()
                                viewModel.createCustomPlan(
                                    title = finalTitle,
                                    selections = customSelections.toList(),
                                    totalDays = customTotalDays.toInt().coerceAtLeast(1),
                                    startDate = System.currentTimeMillis(),
                                    onCreated = { newPlanId ->
                                        onPlanCreated(newPlanId)
                                    }
                                )
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomPlanBuilderSection(
    customTitle: String,
    onCustomTitleChange: (String) -> Unit,
    customTotalDays: Float,
    onCustomTotalDaysChange: (Float) -> Unit,
    customSelections: List<CustomReadingSelection>,
    onAddSelection: (CustomReadingSelection) -> Unit,
    onRemoveSelection: (Int) -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
    onSubmitCustomPlan: () -> Unit
) {
    val allBooks = InitialBibleData.books
    var bookSearchQuery by remember { mutableStateOf("") }
    var selectedBook by remember { mutableStateOf(allBooks.first()) }
    var isWholeBook by remember { mutableStateOf(true) }
    var startChapterText by remember { mutableStateOf("1") }
    var endChapterText by remember { mutableStateOf("${allBooks.first().totalChapters}") }
    var isBookDropdownExpanded by remember { mutableStateOf(false) }

    val filteredBooks = remember(bookSearchQuery) {
        if (bookSearchQuery.isBlank()) allBooks
        else allBooks.filter { it.name.contains(bookSearchQuery, ignoreCase = true) }
    }

    val totalChaptersSelected = remember(customSelections.size, customSelections.toList()) {
        customSelections.sumOf { (it.endChapter - it.startChapter + 1).coerceAtLeast(0) }
    }

    val customPace = if (customTotalDays > 0) {
        String.format("%.1f", totalChaptersSelected / customTotalDays)
    } else "0.0"

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Montar Sequência Customizada",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Escolha livros ou intervalos de capítulos e organize a ordem em que deseja ler:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Book and Chapter Picker Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "1. Selecionar Livro",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Book Dropdown Selection
                ExposedDropdownMenuBox(
                    expanded = isBookDropdownExpanded,
                    onExpandedChange = { isBookDropdownExpanded = !isBookDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = "${selectedBook.name} (${selectedBook.totalChapters} caps)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Livro Bíblico") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isBookDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = isBookDropdownExpanded,
                        onDismissRequest = { isBookDropdownExpanded = false }
                    ) {
                        // Search bar inside dropdown
                        OutlinedTextField(
                            value = bookSearchQuery,
                            onValueChange = { bookSearchQuery = it },
                            placeholder = { Text("Buscar livro...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        )

                        filteredBooks.forEach { book ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(book.name, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = if (book.testament == "OLD") "AT (${book.totalChapters} caps)" else "NT (${book.totalChapters} caps)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                },
                                onClick = {
                                    selectedBook = book
                                    startChapterText = "1"
                                    endChapterText = "${book.totalChapters}"
                                    isBookDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Chapter Scope Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Incluir livro inteiro (${selectedBook.totalChapters} caps)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Switch(
                        checked = isWholeBook,
                        onCheckedChange = {
                            isWholeBook = it
                            if (it) {
                                startChapterText = "1"
                                endChapterText = "${selectedBook.totalChapters}"
                            }
                        }
                    )
                }

                // If specific chapter range
                AnimatedVisibility(visible = !isWholeBook) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        Text(
                            text = "Intervalo de Capítulos:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = startChapterText,
                                onValueChange = { startChapterText = it.filter { char -> char.isDigit() } },
                                label = { Text("Do cap.") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = endChapterText,
                                onValueChange = { endChapterText = it.filter { char -> char.isDigit() } },
                                label = { Text("Até o cap.") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Add to Sequence Button
                Button(
                    onClick = {
                        val startCh = startChapterText.toIntOrNull()?.coerceIn(1, selectedBook.totalChapters) ?: 1
                        val endCh = endChapterText.toIntOrNull()?.coerceIn(startCh, selectedBook.totalChapters) ?: selectedBook.totalChapters

                        onAddSelection(
                            CustomReadingSelection(
                                bookName = selectedBook.name,
                                startChapter = if (isWholeBook) 1 else startCh,
                                endChapter = if (isWholeBook) selectedBook.totalChapters else endCh
                            )
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Adicionar à Sequência")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sequence Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FormatListNumbered,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "2. Sequência de Leitura (${customSelections.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                )
            }

            if (customSelections.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "$totalChaptersSelected caps",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (customSelections.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum livro na sequência ainda.\nEscolha o livro acima e toque em 'Adicionar à Sequência'.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                customSelections.forEachIndexed { index, selection ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = selection.bookName,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Serif
                                        )
                                    )
                                    val capText = if (selection.startChapter == selection.endChapter) {
                                        "Cap. ${selection.startChapter}"
                                    } else {
                                        "Caps. ${selection.startChapter} - ${selection.endChapter} (${selection.endChapter - selection.startChapter + 1} caps)"
                                    }
                                    Text(
                                        text = capText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onMoveUp(index) },
                                    enabled = index > 0,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowUp,
                                        contentDescription = "Mover para cima",
                                        tint = if (index > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                                    )
                                }

                                IconButton(
                                    onClick = { onMoveDown(index) },
                                    enabled = index < customSelections.size - 1,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Mover para baixo",
                                        tint = if (index < customSelections.size - 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                                    )
                                }

                                IconButton(
                                    onClick = { onRemoveSelection(index) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remover",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Custom Plan Details
        Text(
            text = "3. Configurações do Plano",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = customTitle,
            onValueChange = onCustomTitleChange,
            label = { Text("Nome do Plano Customizado") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_custom_plan_title"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Duração em Dias:",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "${customTotalDays.toInt()} dias",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Slider(
            value = customTotalDays,
            onValueChange = onCustomTotalDaysChange,
            valueRange = 1f..180f,
            steps = 179,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Shortcut days chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(7, 14, 30, 60, 90, 180).forEach { d ->
                Surface(
                    onClick = { onCustomTotalDaysChange(d.toFloat()) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (customTotalDays.toInt() == d) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                        Text(
                            text = "${d}d",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (customTotalDays.toInt() == d) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Pace Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Ritmo Customizado Estimado:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "~$customPace caps/dia ($totalChaptersSelected caps em ${customTotalDays.toInt()} dias)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        ),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Submit Custom Plan
        Button(
            onClick = onSubmitCustomPlan,
            enabled = customSelections.isNotEmpty(),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("create_custom_plan_submit")
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = "Gerar Plano Customizado")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Gerar Plano Customizado (${customSelections.size} itens)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun PresetCard(
    title: String,
    description: String,
    scope: String,
    days: Int,
    icon: ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selecionado",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScopeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = Color.White
        ),
        modifier = modifier
    )
}
