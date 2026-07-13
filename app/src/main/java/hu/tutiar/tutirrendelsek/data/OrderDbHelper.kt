package hu.tutiar.tutirrendelsek.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.gson.Gson
import hu.tutiar.tutirrendelsek.model.OrderDto
import hu.tutiar.tutirrendelsek.model.OrderSummary

class OrderDbHelper(context: Context) :
    SQLiteOpenHelper(context, "tutiar_orders.db", null, 1) {

    private val gson = Gson()

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE orders (
                id INTEGER PRIMARY KEY,
                modified TEXT NOT NULL DEFAULT '',
                status TEXT NOT NULL DEFAULT '',
                status_name TEXT NOT NULL DEFAULT '',
                customer_name TEXT NOT NULL DEFAULT '',
                phone TEXT NOT NULL DEFAULT '',
                email TEXT NOT NULL DEFAULT '',
                address TEXT NOT NULL DEFAULT '',
                total REAL NOT NULL DEFAULT 0,
                currency TEXT NOT NULL DEFAULT 'HUF',
                date_created TEXT NOT NULL DEFAULT '',
                json TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_orders_name ON orders(customer_name)")
        db.execSQL("CREATE INDEX idx_orders_phone ON orders(phone)")
        db.execSQL("CREATE INDEX idx_orders_email ON orders(email)")
        db.execSQL("CREATE INDEX idx_orders_modified ON orders(modified)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    fun upsertOrders(orders: List<OrderDto>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (order in orders) {
                val billing = order.billing
                val values = ContentValues().apply {
                    put("id", order.id)
                    put("modified", order.dateModified.orEmpty())
                    put("status", order.status.orEmpty())
                    put("status_name", order.statusName.orEmpty())
                    put("customer_name", billing?.name.orEmpty())
                    put("phone", billing?.phone.orEmpty())
                    put("email", billing?.email.orEmpty())
                    put("address", order.shipping?.address ?: billing?.address.orEmpty())
                    put("total", order.total ?: 0.0)
                    put("currency", order.currency ?: "HUF")
                    put("date_created", order.dateCreated.orEmpty())
                    put("json", gson.toJson(order))
                }
                db.insertWithOnConflict(
                    "orders",
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun deleteOrders(ids: List<Long>) {
        if (ids.isEmpty()) return
        val db = writableDatabase
        db.beginTransaction()
        try {
            ids.forEach { id ->
                db.delete("orders", "id = ?", arrayOf(id.toString()))
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun countOrders(): Int {
        readableDatabase.rawQuery("SELECT COUNT(*) FROM orders", null).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
    }

    fun search(query: String, limit: Int = 50): List<OrderSummary> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()

        val like = "%$trimmed%"
        val result = mutableListOf<OrderSummary>()
        val sql =
            """
            SELECT id, status, status_name, customer_name, phone, email, address,
                   total, currency, date_created, json
            FROM orders
            WHERE CAST(id AS TEXT) LIKE ?
               OR customer_name LIKE ?
               OR phone LIKE ?
               OR email LIKE ?
            ORDER BY id DESC
            LIMIT ?
            """.trimIndent()

        readableDatabase.rawQuery(
            sql,
            arrayOf(like, like, like, like, limit.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result += OrderSummary(
                    id = cursor.getLong(0),
                    status = cursor.getString(1),
                    statusName = cursor.getString(2),
                    name = cursor.getString(3),
                    phone = cursor.getString(4),
                    email = cursor.getString(5),
                    address = cursor.getString(6),
                    total = cursor.getDouble(7),
                    currency = cursor.getString(8),
                    dateCreated = cursor.getString(9),
                    json = cursor.getString(10)
                )
            }
        }
        return result
    }

    fun parseOrder(json: String): OrderDto = gson.fromJson(json, OrderDto::class.java)
}
