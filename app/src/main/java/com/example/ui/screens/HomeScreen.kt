package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.BackupCard
import com.example.ui.components.HeaderBanner
import com.example.ui.components.ProgressCard
import com.example.ui.components.ReadingCard
import com.example.ui.components.ReminderCard
import com.example.ui.viewmodel.BiblePlanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: BiblePlanViewModel,
    onCreatePlanClick: () -> Unit,
    onViewPlanDetailsClick: (Long) -> Unit,
    onMyPlansClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activePlan by viewModel.activePlan.collectAsStateWithLifecycle()
    val readings by viewModel.activePlanReadings.collectAsStateWithLifecycle()
    val completedCount by viewModel.activePlanCompletedCount.collectAsStateWithLifecycle()

    val streak = viewModel.calculateStreak(readings)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Book,
                                    contentDescription = "Logo",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Plano Bíblico",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            )
                        )
                    }
                },
                actions = {
                    OutlinedButton(
                        onClick = onMyPlansClick,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Meus Planos",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Meus Planos")
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            HeaderBanner(
                title = activePlan?.title ?: "Sua Jornada Bíblica",
                subtitle = if (activePlan != null) "Mantenha o hábito da leitura diária da Palavra" else "Crie um plano para acompanhar sua leitura",
                streak = streak
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (activePlan == null) {
                // Empty state card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Sem plano ativo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Nenhum Plano Ativo",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Escolha um plano pré-definido ou crie o seu personalizado para começar a ler a Bíblia diariamente.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onCreatePlanClick,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_create_first_plan")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Criar Plano")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Criar Plano de Leitura")
                        }
                    }
                }
            } else {
                val plan = activePlan!!
                val estimatedDate = viewModel.calculateEstimatedCompletionDate(plan.startDate, plan.totalDays)
                val todayNumber = viewModel.calculateTodayNumber(plan.startDate, plan.totalDays)
                val todayReading = readings.firstOrNull { it.dayNumber == todayNumber }
                    ?: readings.firstOrNull { !it.isCompleted }
                    ?: readings.lastOrNull()

                // Progress Card
                ProgressCard(
                    plan = plan,
                    completedDays = completedCount,
                    estimatedCompletionDate = estimatedDate,
                    onClick = { onViewPlanDetailsClick(plan.id) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Leitura de Hoje
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Leitura de Hoje",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                    )

                    OutlinedButton(
                        onClick = { onViewPlanDetailsClick(plan.id) },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Ver Todas")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (todayReading != null) {
                    ReadingCard(
                        reading = todayReading,
                        isToday = true,
                        onToggleCompletion = { isCompleted ->
                            viewModel.toggleReadingCompletion(todayReading.id, isCompleted)
                        },
                        onSaveNotes = { readingId, notes ->
                            viewModel.saveReadingNotes(readingId, notes)
                        },
                        onCardClick = { onViewPlanDetailsClick(plan.id) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Estatísticas Rápidas
                Text(
                    text = "Resumo do Progresso",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatBox(
                        icon = Icons.Default.LocalFireDepartment,
                        label = "Ofensiva",
                        value = "$streak dias",
                        color = Color(0xFFE28322),
                        modifier = Modifier.weight(1f)
                    )

                    StatBox(
                        icon = Icons.Default.CheckCircle,
                        label = "Concluídos",
                        value = "$completedCount / ${plan.totalDays}",
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )

                    StatBox(
                        icon = Icons.Default.Schedule,
                        label = "Restantes",
                        value = "${plan.totalDays - completedCount} dias",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Próximas Leituras Preview
                val upcomingReadings = readings.filter { !it.isCompleted }.take(3)
                if (upcomingReadings.isNotEmpty()) {
                    Text(
                        text = "Próximas Leituras",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        upcomingReadings.forEach { item ->
                            ReadingCard(
                                reading = item,
                                isToday = item.dayNumber == todayNumber,
                                onToggleCompletion = { isCompleted ->
                                    viewModel.toggleReadingCompletion(item.id, isCompleted)
                                },
                                onSaveNotes = { readingId, notes ->
                                    viewModel.saveReadingNotes(readingId, notes)
                                },
                                onCardClick = { onViewPlanDetailsClick(plan.id) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Daily Reminder Notification Card
            ReminderCard()

            Spacer(modifier = Modifier.height(20.dp))

            // Export / Import Backup Card
            BackupCard()

            Spacer(modifier = Modifier.height(24.dp))

            // Fast Create Button
            Button(
                onClick = onCreatePlanClick,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_navigate_create_plan")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Criar Novo Plano")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Criar Outro Plano de Leitura", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun StatBox(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.15f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
