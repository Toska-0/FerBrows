package studio.toska.ferbrows

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeDialog(
    onStart: () -> Unit,
    onPrivacyPolicy: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onStart,
        containerColor = Panel,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(15.dp)).background(Lime),
                    contentAlignment = Alignment.Center
                ) {
                    Text("F", color = Background, fontSize = 30.sp, fontWeight = FontWeight.Black)
                }
                Text("Добро пожаловать в FerBrows", color = White, fontWeight = FontWeight.Bold)
                Text("Интернет — без лишнего шума.", color = Lime, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Обычные вкладки, закладки и история сохраняются на устройстве.", color = White, fontSize = 13.sp)
                Text("Защита от трекеров использует список известных доменов и не блокирует абсолютно все способы отслеживания.", color = Muted, fontSize = 11.sp)
                Text("Приватный режим зависит от поддержки отдельного профиля WebView. HTTPS шифрует соединение, но не гарантирует безопасность сайта.", color = Muted, fontSize = 11.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = onStart) { Text("Начать работу", color = Lime, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onPrivacyPolicy) { Text("Политика конфиденциальности", color = Muted) }
        }
    )
}
