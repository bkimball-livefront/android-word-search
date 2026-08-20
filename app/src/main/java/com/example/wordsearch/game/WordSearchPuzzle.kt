package com.example.wordsearch.game

const val GRID_SIZE = 6
val PUZZLE_WORDS = listOf("CAT", "MOUSE", "DOG", "MOOSE")

data class Position(val row: Int, val col: Int)

data class PlacedWord(val word: String, val positions: List<Position>)

data class WordSearchPuzzle(
    val grid: List<List<Char>>,
    val placedWords: List<PlacedWord>,
)

private val DIRECTIONS = listOf(
    0 to 1, 0 to -1,
    1 to 0, -1 to 0,
    1 to 1, -1 to -1,
    1 to -1, -1 to 1,
)

fun generatePuzzle(): WordSearchPuzzle {
    while (true) {
        val grid = Array(GRID_SIZE) { arrayOfNulls<Char>(GRID_SIZE) }
        val placedWords = mutableListOf<PlacedWord>()
        var failed = false

        for (word in PUZZLE_WORDS.sortedByDescending { it.length }) {
            val placement = findPlacement(grid, word)
            if (placement == null) {
                failed = true
                break
            }
            for ((index, position) in placement.withIndex()) {
                grid[position.row][position.col] = word[index]
            }
            placedWords += PlacedWord(word, placement)
        }

        if (failed) continue

        val filledGrid = grid.map { row ->
            row.map { it ?: ('A'..'Z').random() }
        }
        return WordSearchPuzzle(filledGrid, placedWords)
    }
}

private fun findPlacement(grid: Array<Array<Char?>>, word: String): List<Position>? {
    val candidates = mutableListOf<List<Position>>()
    for ((rowDelta, colDelta) in DIRECTIONS) {
        for (row in 0 until GRID_SIZE) {
            for (col in 0 until GRID_SIZE) {
                val positions = (0 until word.length).map { i ->
                    Position(row + rowDelta * i, col + colDelta * i)
                }
                if (positions.all { it.row in 0 until GRID_SIZE && it.col in 0 until GRID_SIZE } &&
                    positions.withIndex().all { (i, pos) ->
                        val existing = grid[pos.row][pos.col]
                        existing == null || existing == word[i]
                    }
                ) {
                    candidates += positions
                }
            }
        }
    }
    return candidates.randomOrNull()
}
