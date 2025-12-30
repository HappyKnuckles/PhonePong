package org.tabletennis.project.screens

import LobbySelectionScreen
import androidx.compose.runtime.*
import org.tabletennis.project.network.WebSocketManager
import org.tabletennis.project.screens.game.ui.screens.GameScreen
import org.tabletennis.project.screens.onboarding.PlayerSelectionScreen
import org.tabletennis.project.screens.onboarding.WaitingScreen

@Composable
fun GameFlow() {
    val webSocketManager = remember { WebSocketManager() }

    // Game State Management
    var gameState by remember { mutableStateOf(GameState.LOBBY_SELECTION) }

    // User Choices
    var isCreatingGame by remember { mutableStateOf(false) }
    var selectedLobbyId by remember { mutableStateOf<String?>(null) }
    var playerNumber by remember { mutableStateOf(0) }

    // WebSocket Observables
    val bothPlayersConnected by webSocketManager.bothPlayersConnected.collectAsState()
    val activeLobbyId by webSocketManager.currentLobbyId.collectAsState()
    val assignedRole by webSocketManager.assignedRole.collectAsState()

    // --- State Transitions ---

    // Update player number when role is assigned by server
    LaunchedEffect(assignedRole) {
        assignedRole?.let { role ->
            playerNumber = when (role) {
                "host1" -> 1
                "host2" -> 2
                else -> playerNumber
            }
            println("Player number updated to: $playerNumber based on role: $role")
        }
    }

    // Automatically start game when both connect
    LaunchedEffect(bothPlayersConnected) {
        if (bothPlayersConnected) {
            gameState = GameState.PLAYING
        }
    }

    // Cleanup on exit
    DisposableEffect(Unit) {
        onDispose {
            webSocketManager.disconnect()
        }
    }

    // --- UI Routing ---
    when (gameState) {

        // 1. Choose Create or Join
        GameState.LOBBY_SELECTION -> {
            LobbySelectionScreen(
                onLobbySelected = { lobbyId, isCreating ->
                    selectedLobbyId = lobbyId
                    isCreatingGame = isCreating
                    
                    // Use "host" token for auto-assignment
                    // Server will assign host1 or host2 based on availability
                    webSocketManager.connect(
                        hostToken = "host",
                        lobbyId = lobbyId,
                        isCreating = isCreating
                    )
                    
                    // Skip player selection, go directly to waiting
                    gameState = GameState.WAITING
                }
            )
        }

        // 2. Player Selection - now optional, kept for backward compatibility
        GameState.PLAYER_SELECTION -> {
            PlayerSelectionScreen(
                onPlayerSelected = { number ->
                    playerNumber = number

                    // Manual selection - use specific token
                    val token = "host$number" // Maps 1 -> host1, 2 -> host2

                    webSocketManager.connect(
                        hostToken = token,
                        lobbyId = selectedLobbyId,
                        isCreating = isCreatingGame
                    )

                    gameState = GameState.WAITING
                }
            )
        }

        // 3. Wait for opponent
        GameState.WAITING -> {
            WaitingScreen(
                playerNumber = playerNumber,
                lobbyId = activeLobbyId
            )
        }

        // 4. Play Game
        GameState.PLAYING -> {
            if (playerNumber > 0) {
                GameScreen(
                    webSocketManager = webSocketManager,
                    playerNumber = playerNumber
                )
            }
        }
    }
}