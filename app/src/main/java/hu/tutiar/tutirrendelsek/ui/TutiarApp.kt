package hu.tutiar.tutirrendelsek.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import hu.tutiar.tutirrendelsek.data.OrderRepository
import hu.tutiar.tutirrendelsek.model.OrderDto
import hu.tutiar.tutirrendelsek.model.OrderSummary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TutiarApp() {
    MaterialTheme {
        val context = LocalContext.current
        val repository = remember { OrderRepository(context) }
        val scope = rememberCoroutineScope()

        var query by remember { mutableStateOf("") }
        var loading by remember { mutableStateOf(false) }
        var message by remember { mutableStateOf("Készen áll.") }
        var orderCount by remember { mutableIntStateOf(0) }
        var selected by remember { mutableStateOf<OrderDto?>(null) }
        val results = remember { mutableStateListOf<OrderSummary>() }

        suspend fun refreshCount() {
            orderCount = repository.count()
        }

        LaunchedEffect(Unit) {
            refreshCount()
            val last = repository.lastSync()
            if (last.isNotBlank()) message = "Utolsó szinkron: $last"
        }

        LaunchedEffect(query) {
            delay(180)
            results.clear()
            if (query.trim().isNotEmpty()) {
                results.addAll(repository.search(query))
            }
        }

        if (selected != null) {
            OrderDetailsScreen(
                order = selected!!,
                onBack = { selected = null }
            )
        } else {
            Scaffold { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(18.dp)
                ) {
                    Text(
                        text = "TutiÁr Rendelések",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))
                    Text("$orderCount rendelés helyben")
                    Text(message, style = MaterialTheme.typography.bodySmall)

                    Spacer(Modifier.height(16.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !loading,
                        onClick = {
                            scope.launch {
                                loading = true
                                message = "Szinkronizálás..."
                                try {
                                    val result = repository.sync()
                                    refreshCount()
                                    message =
                                        "Kész: ${result.imported} frissítve, ${result.deleted} törölve."
                                } catch (e: Exception) {
                                    message = "Hiba: ${e.message ?: "ismeretlen hiba"}"
                                } finally {
                                    loading = false
                                }
                            }
                        }
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Adatok frissítése")
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Rendelésszám, név, telefon vagy e-mail") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (query.all { it.isDigit() })
                                KeyboardType.Number else KeyboardType.Text
                        )
                    )

                    Spacer(Modifier.height(12.dp))
                    if (query.isNotBlank() && results.isEmpty()) {
                        Text("Nincs találat.")
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(results, key = { it.id }) { order ->
                            OrderCard(order) {
                                scope.launch {
                                    selected = repository.parse(order)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(order: OrderSummary, onClick: () -> Unit) {
    val money = remember(order.total) {
        NumberFormat.getNumberInstance(Locale("hu", "HU")).format(order.total)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("#${order.id}", fontWeight = FontWeight.Bold)
                Text(order.statusName.ifBlank { order.status })
            }
            Spacer(Modifier.height(4.dp))
            Text(order.name.ifBlank { "Név nélkül" })
            if (order.phone.isNotBlank()) Text(order.phone)
            Text("$money ${order.currency}")
        }
    }
}

@Composable
private fun OrderDetailsScreen(order: OrderDto, onBack: () -> Unit) {
    val context = LocalContext.current
    val billing = order.billing
    val shipping = order.shipping

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                TextButton(onClick = onBack) {
                    Text("← Vissza")
                }
                Text(
                    "#${order.id}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(order.statusName ?: order.status.orEmpty())
                HorizontalDivider(Modifier.padding(vertical = 10.dp))
            }

            item {
                DetailLine("Név", billing?.name)
                DetailLine("Telefon", billing?.phone, clickable = true) {
                    billing?.phone?.takeIf { it.isNotBlank() }?.let {
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$it")))
                    }
                }
                DetailLine("E-mail", billing?.email, clickable = true) {
                    billing?.email?.takeIf { it.isNotBlank() }?.let {
                        context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$it")))
                    }
                }
                DetailLine("Számlázási cím", billing?.address)
                DetailLine("Szállítási cím", shipping?.address)
                DetailLine("Fizetés", order.paymentMethod)
                DetailLine("Szállítás", order.shippingMethods.joinToString())
                DetailLine(
                    "Végösszeg",
                    "${order.total ?: 0.0} ${order.currency ?: "HUF"}"
                )
                DetailLine("Rendelés ideje", order.dateCreated)
                if (!order.customerNote.isNullOrBlank()) {
                    DetailLine("Vevői megjegyzés", order.customerNote)
                }
                order.csvOrderInfo?.let {
                    DetailLine("Beszerzés", it.status)
                }
                HorizontalDivider(Modifier.padding(vertical = 10.dp))
                Text("Termékek", fontWeight = FontWeight.Bold)
            }

            items(order.items) { item ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(item.name.orEmpty(), fontWeight = FontWeight.Medium)
                        Text("${formatQty(item.quantity)} db · ${item.sku.orEmpty()}")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailLine(
    label: String,
    value: String?,
    clickable: Boolean = false,
    onClick: () -> Unit = {}
) {
    if (value.isNullOrBlank()) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (clickable) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 3.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(value)
    }
}

private fun formatQty(value: Double?): String {
    val number = value ?: 0.0
    return if (number % 1.0 == 0.0) number.toInt().toString() else number.toString()
}
