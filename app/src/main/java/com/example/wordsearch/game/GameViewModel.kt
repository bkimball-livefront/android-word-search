package com.example.wordsearch.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlin.math.abs

class GameViewModel : ViewModel() {
    val puzzle: WordSearchPuzzle = generatePuzzle()

    var selection by mutableStateOf<List<Position>>(emptyList())
        private set

    var foundWords by mutableStateOf<Set<String>>(emptySet())
        private set

    val isComplete: Boolean
        get() = foundWords.size == PUZZLE_WORDS.size

    private var dragStart: Position? = null

    fun onDragStart(position: Position) {
        dragStart = position
        selection = listOf(position)
    }

    fun onDragTo(position: Position) {
        val start = dragStart ?: return
        selection = straightLineBetween(start, position)
    }

    fun onDragEnd() {
        val match = puzzle.placedWords.firstOrNull { placed ->
            placed.word !in foundWords &&
                (selection == placed.positions || selection == placed.positions.reversed())
        }
        if (match != null) {
            foundWords = foundWords + match.word
        }
        selection = emptyList()
        dragStart = null
    }

    private fun straightLineBetween(start: Position, end: Position): List<Position> {
        val rowDelta = end.row - start.row
        val colDelta = end.col - start.col
        val rowStep = rowDelta.coerceIn(-1, 1)
        val colStep = colDelta.coerceIn(-1, 1)
        val length = maxOf(abs(rowDelta), abs(colDelta))
        return (0..length)
            .map { i -> Position(start.row + rowStep * i, start.col + colStep * i) }
            .filter { it.row in 0 until GRID_SIZE && it.col in 0 until GRID_SIZE }
    }
}
