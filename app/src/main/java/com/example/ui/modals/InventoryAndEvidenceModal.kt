package com.example.ui.modals

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.ClueId
import com.example.model.CollectedClueRecord
import com.example.mystery.MysteryCatalog
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.BorderSteel
import com.example.ui.theme.CharcoalSlate
import com.example.ui.theme.EmeraldPatina
import com.example.ui.theme.MidnightVoid
import com.example.ui.theme.MutedPewter
import com.example.ui.theme.PaleGold
import com.example.ui.theme.ParchmentIvory
import com.example.ui.theme.VelvetIndigo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InventoryAndEvidenceModal(
    collectedClues: List<CollectedClueRecord>,
    discoveredLinkIds: Set<String>,
    selectedClueId: ClueId?,
    boardSelectedClue: ClueId?,
    boardFeedback: String?,
    onSelectClue: (ClueId) -> Unit,
    onTapBoardClue: (ClueId) -> Unit,
    onClose: () -> Unit,
) {
    var activeTab by remember { mutableIntStateOf(0) }
    val collectedSet = collectedClues.mapNotNull { runCatching { ClueId.valueOf(it.clueId) }.getOrNull() }.toSet()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightVoid.copy(alpha = 0.85f))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(containerColor = CharcoalSlate),
            border = BorderStroke(1.5.dp, AntiqueGold),
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "DETECTIVE CASEBOOK (${collectedSet.size} / 5 CLUES)",
                        style = MaterialTheme.typography.headlineMedium,
                        color = PaleGold,
                    )
                    IconButton(onClick = onClose, modifier = Modifier.testTag("btn_inventory_close")) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(id = R.string.btn_close), tint = ParchmentIvory)
                    }
                }

                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = CharcoalSlate,
                    contentColor = AntiqueGold,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                            color = AntiqueGold,
                        )
                    },
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("Clue Inventory") },
                        modifier = Modifier.testTag("tab_clues"),
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("Evidence Board (${discoveredLinkIds.size}/3 Links)") },
                        modifier = Modifier.testTag("tab_evidence_board"),
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (activeTab == 0) {
                    Row(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        LazyColumn(
                            modifier = Modifier.weight(0.95f).fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(MysteryCatalog.clues) { clue ->
                                val discovered = collectedSet.contains(clue.id)
                                val isSelected = selectedClueId == clue.id
                                Surface(
                                    color = if (isSelected) VelvetIndigo else MidnightVoid.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(
                                        1.2.dp,
                                        when {
                                            isSelected -> AntiqueGold
                                            discovered -> BorderSteel
                                            else -> BorderSteel.copy(alpha = 0.4f)
                                        },
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = discovered) { onSelectClue(clue.id) }
                                        .testTag("clue_item_${clue.id.name.lowercase()}"),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        if (discovered) {
                                            Image(
                                                painter = painterResource(id = clue.imageRes),
                                                contentDescription = clue.title,
                                                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(6.dp)),
                                                contentScale = ContentScale.Crop,
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .background(CharcoalSlate, RoundedCornerShape(6.dp)),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Icon(Icons.Filled.Lock, contentDescription = "Undiscovered", tint = MutedPewter)
                                            }
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (discovered) clue.title else "Undiscovered Evidence",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = if (discovered) PaleGold else MutedPewter,
                                            )
                                            Text(
                                                text = "${clue.roomId.displayName} • ${clue.category.label}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MutedPewter,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Clue Inspection Detail Pane
                        val activeClue = selectedClueId?.let { id ->
                            if (collectedSet.contains(id)) MysteryCatalog.clueById(id) else null
                        }
                        val record = collectedClues.firstOrNull { it.clueId == activeClue?.id?.name }
                        Box(
                            modifier = Modifier
                                .weight(1.05f)
                                .fillMaxHeight()
                                .background(MidnightVoid.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                                .border(1.dp, BorderSteel, RoundedCornerShape(10.dp))
                                .padding(14.dp),
                        ) {
                            if (activeClue != null) {
                                val fmt = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
                                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Image(
                                            painter = painterResource(id = activeClue.imageRes),
                                            contentDescription = activeClue.title,
                                            modifier = Modifier
                                                .size(72.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .border(1.dp, AntiqueGold, RoundedCornerShape(10.dp)),
                                            contentScale = ContentScale.Crop,
                                        )
                                        Column {
                                            Text(activeClue.category.label.uppercase(), style = MaterialTheme.typography.labelSmall, color = AntiqueGold)
                                            Text(activeClue.title, style = MaterialTheme.typography.headlineMedium, color = PaleGold)
                                            if (record != null) {
                                                Text(
                                                    text = "Logged at ${fmt.format(Date(record.discoveredAtEpochMs))}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MutedPewter,
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(activeClue.fullDescription, style = MaterialTheme.typography.bodyLarge, color = ParchmentIvory)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        color = VelvetIndigo,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, AntiqueGold.copy(alpha = 0.45f)),
                                    ) {
                                        Text(
                                            text = "Case Note: ${activeClue.boardSnippet}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = PaleGold,
                                            modifier = Modifier.padding(10.dp),
                                        )
                                    }
                                }
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        text = if (collectedSet.isEmpty()) {
                                            "No clues logged yet. Unlock a mansion wing and inspect glowing items inside."
                                        } else {
                                            "Select a discovered clue to examine forensic details."
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MutedPewter,
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Tab 2: Interactive Evidence Board
                    Row(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Column(
                            modifier = Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "Tap two collected clues to weave an investigative thread:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedPewter,
                            )
                            MysteryCatalog.clues.forEach { clue ->
                                val unlocked = collectedSet.contains(clue.id)
                                val selected = boardSelectedClue == clue.id
                                Surface(
                                    color = if (selected) AntiqueGold else VelvetIndigo.copy(alpha = if (unlocked) 0.9f else 0.35f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, if (selected) PaleGold else BorderSteel),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = unlocked) { onTapBoardClue(clue.id) }
                                        .testTag("board_clue_${clue.id.name.lowercase()}"),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (unlocked) clue.title else "Locked Evidence (${clue.roomId.displayName})",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = if (selected) MidnightVoid else if (unlocked) PaleGold else MutedPewter,
                                                fontWeight = FontWeight.Bold,
                                            )
                                            if (unlocked) {
                                                Text(
                                                    text = clue.boardSnippet,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (selected) MidnightVoid else ParchmentIvory,
                                                )
                                            }
                                        }
                                        if (unlocked) {
                                            Icon(
                                                imageVector = Icons.Filled.Link,
                                                contentDescription = null,
                                                tint = if (selected) MidnightVoid else AntiqueGold,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(MidnightVoid.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                                .border(1.dp, BorderSteel, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text("CONNECTED INSIGHTS", style = MaterialTheme.typography.titleMedium, color = AntiqueGold)
                            if (!boardFeedback.isNullOrBlank()) {
                                Surface(
                                    color = VelvetIndigo,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, AntiqueGold),
                                ) {
                                    Text(
                                        text = boardFeedback,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = PaleGold,
                                        modifier = Modifier.padding(10.dp),
                                    )
                                }
                            }
                            MysteryCatalog.evidenceLinks.forEach { link ->
                                val found = discoveredLinkIds.contains(link.id)
                                Surface(
                                    color = CharcoalSlate,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, if (found) EmeraldPatina else BorderSteel),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.Top,
                                    ) {
                                        Icon(
                                            imageVector = if (found) Icons.Filled.CheckCircle else Icons.Filled.Lock,
                                            contentDescription = null,
                                            tint = if (found) EmeraldPatina else MutedPewter,
                                            modifier = Modifier.size(18.dp),
                                        )
                                        Column {
                                            Text(
                                                text = if (found) link.insightTitle else "Unlinked Deduction Thread",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = if (found) EmeraldPatina else MutedPewter,
                                            )
                                            Text(
                                                text = if (found) link.insightBody else "Connect two related clues on the left to expose this link.",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (found) ParchmentIvory else MutedPewter,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
