package studio.toska.ferbrows

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun BookmarkEditDialog(
    initialUrl: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var value by remember(initialUrl) { mutableStateOf(initialUrl) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Panel,
        title = { Text("Изменить закладку", color = White) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Адрес сайта") }
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(value) }) { Text("Сохранить", color = Lime) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена", color = Muted) }
        }
    )
}
