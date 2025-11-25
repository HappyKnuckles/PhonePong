package com.example.insanecrossmobilepingpongapp.model

/**
 * Represents different screens in the app navigation flow.
 */
sealed class Screen {
    /**
     * Start menu where user selects player role.
     */
    data object Menu : Screen()
    
    /**
     * Waiting screen until all players join.
     */
    data object Waiting : Screen()

    /**
     * Game screen with racket view and motion control.
     */
    data object Game : Screen()
}

/**
 * Player role selection.
 */
enum class PlayerRole(val displayName: String, val token: String) {
    PLAYER1("Player 1", "player1"),
    PLAYER2("Player 2", "player2")
}
