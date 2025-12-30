import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LobbySelectionScreen(
    onLobbySelected: (lobbyId: String?, isCreating: Boolean) -> Unit
) {
    var lobbyCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Table Tennis Host",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // --- Create Section ---
        Button(
            onClick = { onLobbySelected(null, true) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Create New Game", color = Color.White)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "OR",
            color = Color.White
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- Join Section ---
        OutlinedTextField(
            value = lobbyCode,
            onValueChange = {
                if (it.length <= 4) lobbyCode = it.uppercase()
            },
            label = { Text("Enter Lobby Code", color = Color.White) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.Gray,
                cursorColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onLobbySelected(lobbyCode, false) },
            enabled = lobbyCode.length == 4,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White
            )
        ) {
            Text("Join Game", color = Color.White)
        }
    }
}
