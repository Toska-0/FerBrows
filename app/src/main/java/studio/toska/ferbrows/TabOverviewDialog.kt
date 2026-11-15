package studio.toska.ferbrows

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TabOverviewDialog(
    tabs: List<BrowserTab>,
    activeId: Int,
    previews: Map<Int, Bitmap>,
    onSelect: (BrowserTab) -> Unit,
    onClose: (BrowserTab) -> Unit,
    onNewTab: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(),
        containerColor = Background,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Вкладки", color = White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                    Text("${tabs.size} открыто", color = Muted, fontSize = 12.sp)
                }
                IconButton(onClick = onNewTab) {
                    Icon(Icons.Filled.Add, "Новая вкладка", tint = Lime)
                }
            }
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().heightIn(max = 560.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tabs, key = { it.id }) { tab ->
                    val active = tab.id == activeId
                    Column(
                        Modifier.clip(RoundedCornerShape(17.dp))
                            .background(Panel)
                            .border(if (active) 2.dp else 1.dp, if (active) Lime else PanelSoft, RoundedCornerShape(17.dp))
                            .clickable { onSelect(tab) }
                    ) {
                        Box(
                            Modifier.fillMaxWidth().height(142.dp)
                                .background(Color(0xFF080B08)),
                            contentAlignment = Alignment.Center
                        ) {
                            val bitmap = previews[tab.id]
                            if (bitmap != null && !bitmap.isRecycled) {
                                Image(bitmap.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            } else {
                                Icon(Icons.Filled.Language, null, tint = Muted, modifier = Modifier.size(38.dp))
                            }
                            if (tab.isPrivate) {
                                Surface(Modifier.align(Alignment.TopStart).padding(7.dp), color = PanelSoft, shape = RoundedCornerShape(8.dp)) {
                                    Row(Modifier.padding(horizontal = 7.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Lock, null, tint = Lime, modifier = Modifier.size(11.dp))
                                        Text("Приватная", color = Lime, fontSize = 9.sp, modifier = Modifier.padding(start = 4.dp))
                                    }
                                }
                            }
                        }
                        Row(
                            Modifier.fillMaxWidth().padding(start = 9.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(tab.title.ifBlank { "Новая вкладка" }, color = White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(tab.url.ifBlank { "Пустая вкладка" }, color = Muted, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            IconButton(onClick = { onClose(tab) }, modifier = Modifier.size(29.dp)) {
                                Icon(Icons.Filled.Close, "Закрыть вкладку", tint = Muted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Готово", color = Lime) } }
    )
}
