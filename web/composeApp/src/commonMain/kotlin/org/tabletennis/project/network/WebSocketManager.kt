package org.tabletennis.project.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.contentOrNull

@Serializable
data class CoordinatesEvent(
    val x: Float,
    val y: Float,
    val z: Float,
    val v: Float
)

@Serializable
data class ScoreEvent(
    val score: List<Int>,
    val message: String
)

class WebSocketManager {

    private val serverUrl = "192.168.178.85" // 10.0.2.2 for emulator
    private val serverPort = 3000
    private val useSecure = true // Use wss:// for HTTPS compatibility

    private val _bothPlayersConnected = MutableStateFlow(false)
    val bothPlayersConnected: StateFlow<Boolean> = _bothPlayersConnected

    private val _coordinatesEvent = MutableStateFlow<CoordinatesEvent?>(null)
    val coordinatesEvent: StateFlow<CoordinatesEvent?> = _coordinatesEvent

    private val _scoreEvent = MutableStateFlow<ScoreEvent?>(null)
    val scoreEvent: StateFlow<ScoreEvent?> = _scoreEvent

    // Expose the Lobby ID so the UI can show it
    private val _currentLobbyId = MutableStateFlow<String?>(null)
    val currentLobbyId: StateFlow<String?> = _currentLobbyId

    // Track which roles are already taken in the lobby
    private val _occupiedRoles = MutableStateFlow<List<String>>(emptyList())
    val occupiedRoles: StateFlow<List<String>> = _occupiedRoles

    // Track the assigned role from server (for auto-assignment)
    private val _assignedRole = MutableStateFlow<String?>(null)
    val assignedRole: StateFlow<String?> = _assignedRole

    // Track if in singleplayer mode
    private val _isSingleplayer = MutableStateFlow(false)
    val isSingleplayer: StateFlow<Boolean> = _isSingleplayer

    private val client = HttpClient {
        install(WebSockets)
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var connectionJob: Job? = null

    /**
     * Connects to the server.
     * @param hostToken: "host" for auto-assign, or "host1"/"host2" for specific slot
     * @param lobbyId: The 4-letter code if joining.
     * @param isCreating: True if we want to generate a new lobby.
     * @param isSingleplayer: True if creating a singleplayer game with bot.
     */
    fun connect(hostToken: String, lobbyId: String?, isCreating: Boolean, isSingleplayer: Boolean = false) {
        disconnect()
        _isSingleplayer.value = isSingleplayer

        connectionJob = scope.launch {
            try {
                println("🔌 Connecting to WebSocket: wss://$serverUrl:$serverPort (Singleplayer: $isSingleplayer)")
                client.webSocket(
                    method = HttpMethod.Get,
                    host = serverUrl,
                    port = serverPort,
                    path = "/",
                    request = {
                        // Use wss:// for secure WebSocket connection
                        url.protocol = if (useSecure) io.ktor.http.URLProtocol.WSS else io.ktor.http.URLProtocol.WS
                        url.parameters.append("token", hostToken)

                        if (isCreating) {
                            if (isSingleplayer) {
                                url.parameters.append("action", "create_singleplayer")
                                url.parameters.append("difficulty", "medium")
                            } else {
                                url.parameters.append("action", "create")
                            }
                        } else if (!lobbyId.isNullOrEmpty()) {
                            url.parameters.append("lobby", lobbyId)
                            _currentLobbyId.value = lobbyId
                        }
                    }
                ) {
                    println("✅ WebSocket connected!")
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val message = frame.readText()
                            handleMessage(message)
                        }
                    }
                }
            } catch (e: Exception) {
                println("❌ WebSocket connection failed: ${e.message}")
                e.printStackTrace()
                _bothPlayersConnected.value = false
            } finally {
                _bothPlayersConnected.value = false
            }
        }
    }

    private fun handleMessage(message: String) {
        try {
            if (message == "start") {
                _bothPlayersConnected.value = true
                return
            }

            val rootElement = json.parseToJsonElement(message)
            val rootObj = rootElement.jsonObject
            val type = rootObj["type"]?.jsonPrimitive?.contentOrNull

            when (type) {
                // 1. Handle Lobby Creation response
                "lobby_created" -> {
                    val id = rootObj["lobbyId"]?.jsonPrimitive?.contentOrNull
                    if (id != null) {
                        _currentLobbyId.value = id
                        println("Lobby Created: $id")
                    }
                }
                // 2. Handle Lobby Info (sent on join)
                "lobby_info" -> {
                    val id = rootObj["lobbyId"]?.jsonPrimitive?.contentOrNull
                    if (id != null) _currentLobbyId.value = id
                }
                // 3. Handle Coordinates
                "coordinates" -> {
                    val dataObject = rootObj["data"]
                    if (dataObject != null) {
                        val event = json.decodeFromJsonElement<CoordinatesEvent>(dataObject)
                        _coordinatesEvent.value = event
                    }
                }
                // 4. Handle Score
                "score" -> {
                    val event = json.decodeFromString<ScoreEvent>(message)
                    _scoreEvent.value = event
                }
                // 5. Handle Lobby State (Occupied Roles)
                "lobby_state" -> {
                    val occupiedArray = rootObj["occupied"]?.jsonArray
                    if (occupiedArray != null) {
                        val roles = occupiedArray.mapNotNull { it.jsonPrimitive.contentOrNull }
                        _occupiedRoles.value = roles
                        println("Occupied roles: $roles")
                    }
                }
                // 6. Handle Role Assignment (for auto-assign)
                "role_assigned" -> {
                    val role = rootObj["role"]?.jsonPrimitive?.contentOrNull
                    if (role != null) {
                        _assignedRole.value = role
                        println("Role assigned by server: $role")
                    }
                }
                // 7. Handle Game In Progress (rejoin running game)
                "game_in_progress" -> {
                    println("Rejoining game in progress")
                    _bothPlayersConnected.value = true
                }
            }
        } catch (e: Exception) {
            println("Error parsing message: $message | ${e.message}")
        }
    }

    fun disconnect() {
        connectionJob?.cancel()
        _bothPlayersConnected.value = false
        _currentLobbyId.value = null
        _occupiedRoles.value = emptyList()
        _assignedRole.value = null
        _isSingleplayer.value = false
    }
}