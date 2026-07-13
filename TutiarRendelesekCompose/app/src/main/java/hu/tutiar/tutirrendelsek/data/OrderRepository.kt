package hu.tutiar.tutirrendelsek.data

import android.content.Context
import hu.tutiar.tutirrendelsek.AppConfig
import hu.tutiar.tutirrendelsek.model.OrderDto
import hu.tutiar.tutirrendelsek.model.OrderSummary
import hu.tutiar.tutirrendelsek.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class SyncResult(
    val imported: Int,
    val deleted: Int,
    val syncTime: String
)

class OrderRepository(context: Context) {
    private val appContext = context.applicationContext
    private val db = OrderDbHelper(appContext)
    private val prefs = appContext.getSharedPreferences("sync", Context.MODE_PRIVATE)

    suspend fun sync(): SyncResult = withContext(Dispatchers.IO) {
        if (AppConfig.API_TOKEN.startsWith("IDE_")) {
            error("Az API-token még nincs beállítva az AppConfig.kt fájlban.")
        }

        val since = prefs.getString("last_sync", null)
        val response = ApiClient.service.sync(
            authorization = "Bearer ${AppConfig.API_TOKEN}",
            since = since
        )

        if (!response.success) {
            error(response.error?.message ?: "Ismeretlen API-hiba.")
        }

        val data = response.data ?: error("Az API nem adott vissza adatot.")
        db.upsertOrders(data.orders)
        db.deleteOrders(data.deletedOrderIds)

        val syncTime = data.syncTo ?: data.serverTime.orEmpty()
        if (syncTime.isNotBlank()) {
            prefs.edit().putString("last_sync", syncTime).apply()
        }

        SyncResult(
            imported = data.orders.size,
            deleted = data.deletedOrderIds.size,
            syncTime = syncTime
        )
    }

    suspend fun count(): Int = withContext(Dispatchers.IO) {
        db.countOrders()
    }

    suspend fun search(query: String): List<OrderSummary> = withContext(Dispatchers.IO) {
        db.search(query)
    }

    suspend fun parse(summary: OrderSummary): OrderDto = withContext(Dispatchers.Default) {
        db.parseOrder(summary.json)
    }

    fun lastSync(): String = prefs.getString("last_sync", null).orEmpty()
}
