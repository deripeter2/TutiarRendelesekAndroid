package hu.tutiar.tutirrendelsek.model

import com.google.gson.annotations.SerializedName

data class ApiResponse(
    val success: Boolean = false,
    val data: SyncData? = null,
    val error: ApiError? = null
)

data class ApiError(
    val message: String? = null
)

data class SyncData(
    @SerializedName("server_time") val serverTime: String? = null,
    @SerializedName("sync_from") val syncFrom: String? = null,
    @SerializedName("sync_to") val syncTo: String? = null,
    @SerializedName("full_sync") val fullSync: Boolean = false,
    val orders: List<OrderDto> = emptyList(),
    @SerializedName("deleted_order_ids") val deletedOrderIds: List<Long> = emptyList(),
    @SerializedName("api_version") val apiVersion: Int? = null
)

data class OrderDto(
    val id: Long,
    val status: String? = null,
    @SerializedName("status_name") val statusName: String? = null,
    val currency: String? = null,
    val total: Double? = null,
    val subtotal: Double? = null,
    @SerializedName("shipping_total") val shippingTotal: Double? = null,
    @SerializedName("discount_total") val discountTotal: Double? = null,
    @SerializedName("total_tax") val totalTax: Double? = null,
    @SerializedName("payment_method") val paymentMethod: String? = null,
    @SerializedName("customer_note") val customerNote: String? = null,
    @SerializedName("date_created") val dateCreated: String? = null,
    @SerializedName("date_modified") val dateModified: String? = null,
    @SerializedName("date_paid") val datePaid: String? = null,
    @SerializedName("date_completed") val dateCompleted: String? = null,
    val billing: PersonDto? = null,
    val shipping: PersonDto? = null,
    @SerializedName("shipping_methods") val shippingMethods: List<String> = emptyList(),
    val items: List<OrderItemDto> = emptyList(),
    @SerializedName("csv_order_info") val csvOrderInfo: CsvOrderInfoDto? = null
)

data class PersonDto(
    val name: String? = null,
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    val company: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null
)

data class OrderItemDto(
    @SerializedName("item_id") val itemId: Long? = null,
    val name: String? = null,
    val quantity: Double? = null,
    val sku: String? = null,
    val subtotal: Double? = null,
    val total: Double? = null
)

data class CsvOrderInfoDto(
    val status: String? = null,
    val date: String? = null,
    val filename: String? = null
)

data class OrderSummary(
    val id: Long,
    val status: String,
    val statusName: String,
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val total: Double,
    val currency: String,
    val dateCreated: String,
    val json: String
)
