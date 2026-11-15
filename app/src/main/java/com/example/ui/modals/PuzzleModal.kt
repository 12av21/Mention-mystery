package com.example.ui.modals

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.PuzzleType
import com.example.model.RoomPuzzleDefinition
import com.example.ui.theme.AmberLantern
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.BorderSteel
import com.example.ui.theme.CharcoalSlate
import com.example.ui.theme.CrimsonGlow
import com.example.ui.theme.EmeraldPatina
import com.example.ui.theme.MidnightVoid
import com.example.ui.theme.MutedPewter
import com.example.ui.theme.PaleGold
import com.example.ui.theme.ParchmentIvory
import com.example.ui.theme.VelvetIndigo

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PuzzleModal(
    puzzle: RoomPuzzleDefinition,
    selectedSequence: List<String>,
    hintsRevealed: Int,
    feedback: String?,
    feedbackIsError: Boolean,
    onToggleOption: (String) -> Unit,
    onReset: () -> Unit,
    onRevealHint: () -> Unit,
    onSubmit: () -> Unit,
    onClose: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightVoid.copy(alpha = 0.84f))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(containerColor = CharcoalSlate),
            border = BorderStroke(1.5.dp, AntiqueGold),
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = puzzle.subtitle.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = AntiqueGold,
                        )
                        Text(
                            text = puzzle.title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = PaleGold,
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            color = VelvetIndigo,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, BorderSteel),
                        ) {
                            Text(
                                text = "${selectedSequence.size} / ${puzzle.expectedSequence.size} Selected",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selectedSequence.size == puzzle.expectedSequence.size) EmeraldPatina else PaleGold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            )
                        }
                        IconButton(onClick = onClose, modifier = Modifier.testTag("btn_puzzle_close")) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(id = R.string.btn_close), tint = ParchmentIvory)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Split Landscape Layout: Prompt/Notes/Hints on left, Interactive Matrix/Sequence on right
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .weight(0.9f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        Text(
                            text = puzzle.prompt,
                            style = MaterialTheme.typography.bodyLarge,
                            color = ParchmentIvory,
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        puzzle.extraNotes.forEach { note ->
                            Surface(
                                color = VelvetIndigo.copy(alpha = 0.75f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, AntiqueGold.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            ) {
                                Text(
                                    text = note,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PaleGold,
                                    modifier = Modifier.padding(10.dp),
                                )
                            }
                        }

                        if (hintsRevealed > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            puzzle.hints.take(hintsRevealed).forEachIndexed { idx, hint ->
                                Surface(
                                    color = MidnightVoid,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, AmberLantern.copy(alpha = 0.55f)),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                ) {
                                    Text(
                                        text = "Hint ${idx + 1}: $hint",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AmberLantern,
                                        modifier = Modifier.padding(9.dp),
                                    )
                                }
                            }
                        }

                        if (!feedback.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = feedback,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (feedbackIsError) CrimsonGlow else EmeraldPatina,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }

                    // Interactive Right Panel
                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight()
                            .background(MidnightVoid.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                            .border(1.dp, BorderSteel, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                            Text(
                                text = when (puzzle.type) {
                                    PuzzleType.CIPHER_BOOKS,
                                    PuzzleType.RECIPE_SEQUENCE,
                                    PuzzleType.GALLERY_SYMBOLS -> "Tap options in the exact sequence:"
                                    PuzzleType.BOTANICAL_PATTERN -> "Toggle the 3 matching Conservatory tiles:"
                                    PuzzleType.LOGIC_DEDUCTION -> "Select BOTH the Culprit and Time:"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = MutedPewter,
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (puzzle.type == PuzzleType.BOTANICAL_PATTERN) {
                                // 3x3 Conservatory Tile Matrix
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    puzzle.options.chunked(3).forEach { rowItems ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            rowItems.forEach { option ->
                                                val selected = selectedSequence.contains(option)
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(54.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (selected) AntiqueGold else VelvetIndigo)
                                                        .border(
                                                            1.5.dp,
                                                            if (selected) PaleGold else BorderSteel,
                                                            RoundedCornerShape(8.dp),
                                                        )
                                                        .clickable { onToggleOption(option) }
                                                        .padding(6.dp),
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Text(
                                                        text = option,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (selected) MidnightVoid else ParchmentIvory,
                                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    puzzle.options.forEach { option ->
                                        val orderIdx = selectedSequence.indexOf(option)
                                        val isSelected = orderIdx >= 0
                                        val badge = if (isSelected && puzzle.type != PuzzleType.LOGIC_DEDUCTION) {
                                            "${orderIdx + 1}. $option"
                                        } else option
                                        Surface(
                                            color = if (isSelected) AntiqueGold else VelvetIndigo,
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, if (isSelected) PaleGold else BorderSteel),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { onToggleOption(option) }
                                                .testTag("puzzle_opt_$option"),
                                        ) {
                                            Text(
                                                text = badge,
                                                style = MaterialTheme.typography.labelLarge,
                                                color = if (isSelected) MidnightVoid else ParchmentIvory,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            )
                                        }
                                    }
                                }
                            }

                            if (selectedSequence.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Current Entry: ${selectedSequence.joinToString("  →  ")}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PaleGold,
                                )
                            }
                        }

                        // Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedButton(
                                onClick = onRevealHint,
                                enabled = hintsRevealed < puzzle.hints.size,
                                modifier = Modifier.height(48.dp).testTag("btn_puzzle_hint"),
                                border = BorderStroke(1.dp, AmberLantern.copy(alpha = 0.7f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberLantern),
                            ) {
                                Icon(Icons.Filled.Lightbulb, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(id = R.string.btn_hint))
                            }
                            OutlinedButton(
                                onClick = onReset,
                                modifier = Modifier.height(48.dp).testTag("btn_puzzle_reset"),
                                border = BorderStroke(1.dp, BorderSteel),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ParchmentIvory),
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = null)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Button(
                                onClick = onSubmit,
                                enabled = selectedSequence.size == puzzle.expectedSequence.size,
                                modifier = Modifier.height(48.dp).testTag("btn_puzzle_submit"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AntiqueGold,
                                    contentColor = MidnightVoid,
                                ),
                            ) {
                                Icon(Icons.Filled.LockOpen, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(id = R.string.btn_submit_answer), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
