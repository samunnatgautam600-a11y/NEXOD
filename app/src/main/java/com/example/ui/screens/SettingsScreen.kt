package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SearchMode
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.theme.NexoraCyan
import com.example.ui.theme.NexoraViolet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: UiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var geminiKeyInput by remember { mutableStateOf(uiState.geminiApiKey) }
    var showGeminiKey by remember { mutableStateOf(false) }

    var tavilyKeyInput by remember { mutableStateOf(uiState.tavilyApiKey) }
    var showTavilyKey by remember { mutableStateOf(false) }

    var saveConfirmationMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        item {
            Text(
                text = "Settings & Configuration",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Manage live web search providers and AI synthesis credentials",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Web Search Provider
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Public, contentDescription = null, tint = NexoraCyan)
                        Text(
                            text = "Web Search Provider",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Text(
                        text = "NEXORA uses an abstraction layer to query live web data. Choose your provider:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val providers = listOf(
                        Pair("open_web", "Live Open Web Index (Wikipedia & Scientific Archives)"),
                        Pair("tavily", "Tavily AI Search Engine")
                    )

                    providers.forEach { (id, label) ->
                        val isSelected = uiState.searchProvider == id
                        OutlinedCard(
                            onClick = { viewModel.updateSearchProvider(id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("provider_option_$id"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) NexoraCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.updateSearchProvider(id) }
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Default Search Mode Preference
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = NexoraViolet)
                        Text(
                            text = "Default Search Mode",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SearchMode.values().forEach { mode ->
                            val isSelected = uiState.searchMode == mode
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateDefaultSearchMode(mode) },
                                label = { Text(mode.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                }
            }
        }

        // API Credentials
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = NexoraCyan)
                        Text(
                            text = "API Credentials & Secrets",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Text(
                        text = "API keys are automatically injected via the Secrets panel in AI Studio. You can also configure a custom override key below:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Gemini Key
                    OutlinedTextField(
                        value = geminiKeyInput,
                        onValueChange = { geminiKeyInput = it },
                        label = { Text("Gemini API Key (Optional Override)") },
                        placeholder = { Text("AI Studio automatically supplies default key") },
                        singleLine = true,
                        visualTransformation = if (showGeminiKey) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { showGeminiKey = !showGeminiKey }) {
                                Icon(
                                    imageVector = if (showGeminiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle visibility"
                                )
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gemini_key_input")
                    )

                    // Tavily Key
                    OutlinedTextField(
                        value = tavilyKeyInput,
                        onValueChange = { tavilyKeyInput = it },
                        label = { Text("Tavily Search API Key (Optional)") },
                        placeholder = { Text("tvly-...") },
                        singleLine = true,
                        visualTransformation = if (showTavilyKey) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { showTavilyKey = !showTavilyKey }) {
                                Icon(
                                    imageVector = if (showTavilyKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle visibility"
                                )
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tavily_key_input")
                    )

                    Button(
                        onClick = {
                            viewModel.updateGeminiApiKey(geminiKeyInput)
                            viewModel.updateTavilyApiKey(tavilyKeyInput)
                            saveConfirmationMessage = "Settings saved successfully"
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_settings_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Credentials")
                    }

                    if (saveConfirmationMessage != null) {
                        Text(
                            text = saveConfirmationMessage!!,
                            color = Color(0xFF10B981),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // About NEXORA Architecture
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "NEXORA Reliability Protocol",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "• Live Web Grounding: Retrieved from open scientific indices and official documents.\n• Source Ranking: Automatic priority for .gov, .edu, scientific orgs & official technical docs.\n• Zero Fabricated Citations: Never generates phantom URLs.\n• Room Offline Database: All saved topics and quiz histories are stored securely on-device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
