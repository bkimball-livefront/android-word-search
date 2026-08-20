package com.example.wordsearch.game

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.border
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameScreen(viewModel: GameViewModel = viewModel()) {
    val puzzle = viewModel.puzzle
    val selection = viewModel.selection
    val foundWords = viewModel.foundWords
    val isComplete = viewModel.isComplete

    val foundPositions = remember(foundWords) {
        puzzle.placedWords
            .filter { it.word in foundWords }
            .flatMap { it.positions }
            .toSet()
    }
    val selectedPositions = remember(selection) { selection.toSet() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(
            text = "Find the hidden words",
            style = MaterialTheme.typography.titleLarge,
        )

        Text(
            text = "${foundWords.size} / ${PUZZLE_WORDS.size} found",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PUZZLE_WORDS.forEach { word ->
                val found = word in foundWords
                Text(
                    text = word,
                    textDecoration = if (found) TextDecoration.LineThrough else null,
                    color = if (found) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .border(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            val density = LocalDensity.current
            val cellSizeDp = maxWidth / GRID_SIZE
            val cellSizePx = with(density) { cellSizeDp.toPx() }
            val outlineColor = MaterialTheme.colorScheme.outline

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                viewModel.onDragStart(offset.toGridPosition(cellSizePx))
                            },
                            onDrag = { change, _ ->
                                viewModel.onDragTo(change.position.toGridPosition(cellSizePx))
                            },
                            onDragEnd = { viewModel.onDragEnd() },
                            onDragCancel = { viewModel.onDragEnd() },
                        )
                    },
            ) {
                for (row in 0 until GRID_SIZE) {
                    Row {
                        for (col in 0 until GRID_SIZE) {
                            val position = Position(row, col)
                            val background = when {
                                position in foundPositions -> MaterialTheme.colorScheme.primaryContainer
                                position in selectedPositions -> MaterialTheme.colorScheme.secondaryContainer
                                else -> Color.Transparent
                            }
                            Box(
                                modifier = Modifier
                                    .size(cellSizeDp)
                                    .border(0.5.dp, outlineColor)
                                    .background(background),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = puzzle.grid[row][col].toString(),
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isComplete) {
            Text(
                text = "You found all the words!",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 24.dp),
            )
        }
    }
}

private fun Offset.toGridPosition(cellSizePx: Float): Position {
    val row = (y / cellSizePx).toInt().coerceIn(0, GRID_SIZE - 1)
    val col = (x / cellSizePx).toInt().coerceIn(0, GRID_SIZE - 1)
    return Position(row, col)
}
