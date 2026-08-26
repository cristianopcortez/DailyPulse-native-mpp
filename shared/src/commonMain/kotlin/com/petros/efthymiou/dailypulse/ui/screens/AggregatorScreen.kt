package com.petros.efthymiou.dailypulse.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.petros.efthymiou.dailypulse.aggregators.application.Aggregator
import com.petros.efthymiou.dailypulse.aggregators.presentation.AggregatorViewModel
import com.petros.efthymiou.dailypulse.ui.screens.elements.ErrorMessage
import org.koin.compose.koinInject

class AggregatorScreen : Screen {

    @Composable
    override fun Content() {
        AggregatorScreenContent()
    }
}

@Composable
fun AggregatorScreenContent(
    viewModel: AggregatorViewModel = koinInject()
) {
    val aggregatorState by viewModel.aggregatorState.collectAsState()

    Column {
        AppBar()

        aggregatorState.error?.let { ErrorMessage(it) }

        if (aggregatorState.loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            AggregatorSelectorView(
                aggregators = aggregatorState.aggregators,
                selectedAggregatorId = aggregatorState.selectedAggregatorId,
                onAggregatorSelected = { viewModel.selectAggregator(it) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppBar() {
    val navigator = LocalNavigator.currentOrThrow

    TopAppBar(
        title = { Text(text = "News Provider") },
        navigationIcon = {
            IconButton(onClick = {
                navigator.pop()
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back Button",
                )
            }
        }
    )
}

@Composable
fun AggregatorSelectorView(
    aggregators: List<Aggregator>,
    selectedAggregatorId: String,
    onAggregatorSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedAggregator = aggregators.find { it.id == selectedAggregatorId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Select News Provider",
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Choose where to fetch news from. Your selection will be saved for future sessions.",
            style = TextStyle(color = Color.Gray, fontSize = 14.sp),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedAggregator?.name ?: "NewsAPI (default)",
                        style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Current provider",
                        style = TextStyle(color = Color.Gray, fontSize = 12.sp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Dropdown Icon",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                aggregators.forEach { aggregator ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = aggregator.name,
                                    modifier = Modifier.weight(1f)
                                )
                                if (aggregator.id == selectedAggregatorId) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        onClick = {
                            onAggregatorSelected(aggregator.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
