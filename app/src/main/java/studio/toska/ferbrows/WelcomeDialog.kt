package studio.toska.ferbrows

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun WelcomeDialog(
    onStart: () -> Unit,
    onPrivacyPolicy: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onStart,
        containerColor = Panel,
        title = { Text("Добро пожаловать в FerBrows", color = White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Браузер работает на Android System WebView и сохраняет обычные вкладки, закладки и историю на устройстве.", color = White)
                Text("Блокировка трекеров использует небольшой список доменов и не является полной защитой. Приватный режим требует поддержки отдельного профиля WebView.", color = Muted)
                Text("HTTPS защищает соединение, но не делает сайт автоматически безопасным и не обеспечивает анонимность.", color = Muted)
            }
        },
        confirmButton = {
            TextButton(onClick = onStart) { Text("Начать работу", color = Lime) }
        },
        dismissButton = {
            TextButton(onClick = onPrivacyPolicy) { Text("Политика конфиденциальности", color = Muted) }
        }
    )
}
