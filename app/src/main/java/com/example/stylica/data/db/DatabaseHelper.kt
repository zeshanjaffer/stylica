package com.example.stylica.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import com.example.stylica.data.db.DatabaseHelper.Companion.COL_PRODUCT_ID
import com.example.stylica.data.db.DatabaseHelper.Companion.COL_STATUS
import com.example.stylica.data.db.DatabaseHelper.Companion.TABLE_PRODUCTS


class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "stylica.db"
        private const val DATABASE_VERSION = 16

        const val TABLE_USERS = "users"
        const val COL_ID = "id"
        const val COL_FIRST_NAME = "first_name"
        const val COL_LAST_NAME = "last_name"
        const val COL_EMAIL = "email"
        const val COL_PASSWORD = "password"
        const val COL_PROFILE_IMAGE = "profile_image"

        const val COL_ROLE = "role"

        // ================= PRODUCTS TABLE =================
        const val TABLE_PRODUCTS = "products"
        const val COL_PRODUCT_ID = "product_id"
        const val COL_PRODUCT_NAME = "product_name"
        const val COL_PRODUCT_DESC = "product_description"

        const val COL_PRODUCT_IMAGE = "product_image"
        const val COL_PRICE = "price"
        const val COL_CATEGORY = "category"
        const val COL_SUBCATEGORY = "subcategory"
        const val COL_TARGET_AUDIENCE = "target_audience"
        const val COL_INVENTORY = "inventory_count"
        const val COL_SIZE = "size"
        const val COL_STATUS = "status"
        const val COL_MODERATOR_EMAIL = "moderator_email"


        // ================= ORDERS TABLE =================
        const val TABLE_ORDERS = "orders"
        const val COL_ORDER_ID = "order_id"
        const val COL_ORDER_PRODUCT_ID = "product_id"
        const val COL_ORDER_USER_EMAIL = "user_email"
        const val COL_ORDER_STATUS = "order_status"

        const val COL_ORDER_NAME = "order_name"
        const val COL_ORDER_PHONE = "order_phone"
        const val COL_ORDER_ADDRESS = "order_address"
        const val COL_ORDER_PAYMENT = "order_payment"


    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_USERS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_FIRST_NAME TEXT NOT NULL,
                $COL_LAST_NAME TEXT NOT NULL,
                $COL_EMAIL TEXT UNIQUE NOT NULL,
                $COL_PASSWORD TEXT NOT NULL,
                $COL_ROLE TEXT NOT NULL,
                $COL_PROFILE_IMAGE TEXT
)
        """.trimIndent()

        db.execSQL(createTable)

        // Create Products Table
        val createProductsTable = """
    CREATE TABLE $TABLE_PRODUCTS (
        $COL_PRODUCT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
        $COL_PRODUCT_NAME TEXT NOT NULL,
        $COL_PRODUCT_DESC TEXT,
        $COL_PRICE REAL NOT NULL,
        $COL_CATEGORY TEXT,
        $COL_SUBCATEGORY TEXT,
        $COL_TARGET_AUDIENCE TEXT,
        $COL_INVENTORY INTEGER,
        $COL_SIZE TEXT,
        $COL_STATUS TEXT NOT NULL,
        $COL_MODERATOR_EMAIL TEXT,
        $COL_PRODUCT_IMAGE TEXT
    )
""".trimIndent()

        db.execSQL(createProductsTable)

        // Create Orders Table
        val createOrdersTable = """
    CREATE TABLE $TABLE_ORDERS (
        $COL_ORDER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
        $COL_ORDER_PRODUCT_ID INTEGER,
        $COL_ORDER_USER_EMAIL TEXT,
        $COL_ORDER_NAME TEXT,
        $COL_ORDER_PHONE TEXT,
        $COL_ORDER_ADDRESS TEXT,
        $COL_ORDER_PAYMENT TEXT,
        $COL_ORDER_STATUS TEXT,
        selected_size TEXT,
        selected_color TEXT
    )
""".trimIndent()

        db.execSQL(createOrdersTable)

        db.execSQL(
            "CREATE TABLE cart (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "product_id INTEGER," +
                    "user_email TEXT," +
                    "quantity INTEGER DEFAULT 1," +
                    "selected_size TEXT," +
                    "selected_color TEXT" +
                    ")"
        )

        db.execSQL(
            "CREATE TABLE favorites (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "product_id INTEGER," +
                    "user_email TEXT" +
                    ")"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Do not DROP TABLE / onCreate here: bumping DATABASE_VERSION would wipe every user's
        // local data on each app update. Add ALTER TABLE / new-table migrations only when the
        // schema actually changes.

        if (oldVersion < 9) {
            db.execSQL(
                "ALTER TABLE $TABLE_PRODUCTS ADD COLUMN $COL_PRODUCT_IMAGE TEXT"
            )
        }

        if (oldVersion < 10) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS cart (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "product_id INTEGER," +
                        "user_email TEXT," +
                        "quantity INTEGER DEFAULT 1" +
                        ")"
            )
        }

        if (oldVersion < 11) {
            db.execSQL(
                "ALTER TABLE $TABLE_USERS ADD COLUMN $COL_PROFILE_IMAGE TEXT"
            )
        }

        if (oldVersion < 12) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS favorites (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "product_id INTEGER," +
                        "user_email TEXT" +
                        ")"
            )
        }

        if (oldVersion < 14) {
            db.execSQL(
                "ALTER TABLE $TABLE_PRODUCTS ADD COLUMN $COL_TARGET_AUDIENCE TEXT DEFAULT 'All'"
            )
        }

        if (oldVersion < 15) {
            db.execSQL(
                "ALTER TABLE $TABLE_PRODUCTS ADD COLUMN $COL_INVENTORY INTEGER DEFAULT 10"
            )
            db.execSQL(
                "ALTER TABLE $TABLE_PRODUCTS ADD COLUMN $COL_SIZE TEXT DEFAULT 'N/A'"
            )
        }

        if (oldVersion < 16) {
            db.execSQL("ALTER TABLE cart ADD COLUMN selected_size TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE cart ADD COLUMN selected_color TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE $TABLE_ORDERS ADD COLUMN selected_size TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE $TABLE_ORDERS ADD COLUMN selected_color TEXT DEFAULT ''")
        }
    }

    fun registerUser(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        role: String

    ): Boolean {

        val db = writableDatabase

        val values = ContentValues().apply {
            put(COL_FIRST_NAME, firstName)
            put(COL_LAST_NAME, lastName)
            put(COL_EMAIL, email)
            put(COL_PASSWORD, password)
            put(COL_ROLE, role)

        }

        return try {
            db.insertOrThrow(TABLE_USERS, null, values)
            true
        } catch (e: SQLiteConstraintException) {
            false   // email already exists
        }
    }

    fun loginUser(email: String, password: String): Cursor? {

        val db = readableDatabase

        return db.query(
            TABLE_USERS,
            null,
            "$COL_EMAIL = ? AND $COL_PASSWORD = ?",
            arrayOf(email, password),
            null,
            null,
            null
        )
    }

    fun changePassword(email: String, oldPassword: String, newPassword: String): Boolean {
        val db = writableDatabase
        val cursor = loginUser(email, oldPassword)
        if (cursor != null && cursor.moveToFirst()) {
            cursor.close()
            val values = ContentValues().apply {
                put(COL_PASSWORD, newPassword)
            }
            val rows = db.update(TABLE_USERS, values, "$COL_EMAIL = ?", arrayOf(email))
            return rows > 0
        }
        cursor?.close()
        return false
    }

    fun updateUser(
        email: String,
        firstName: String?,
        lastName: String?,
        password: String?,
        profileImage: String? = null
    ): Int {

        val db = writableDatabase
        val values = ContentValues()

        if (!firstName.isNullOrEmpty()) {
            values.put(COL_FIRST_NAME, firstName)
        }

        if (!lastName.isNullOrEmpty()) {
            values.put(COL_LAST_NAME, lastName)
        }

        if (!password.isNullOrEmpty()) {
            values.put(COL_PASSWORD, password)
        }

        if (!profileImage.isNullOrEmpty()) {
            values.put(COL_PROFILE_IMAGE, profileImage)
        }

        // If nothing to update, return 0
        if (values.size() == 0) {
            return 0
        }

        return db.update(
            TABLE_USERS,
            values,
            "$COL_EMAIL=?",
            arrayOf(email)
        )
    }

    fun deleteUser(email: String): Int {

        val db = writableDatabase

        return db.delete(
            TABLE_USERS,
            "$COL_EMAIL=?",
            arrayOf(email)
        )
    }

    fun getUserByEmail(email: String): User? {

        val db = readableDatabase

        val cursor = db.query(

            TABLE_USERS,
            null,
            "$COL_EMAIL = ?",
            arrayOf(email),
            null,
            null,
            null

        )


        if (cursor.moveToFirst()) {

            val firstName = cursor.getString(
                cursor.getColumnIndexOrThrow(COL_FIRST_NAME)
            )

            val lastName = cursor.getString(
                cursor.getColumnIndexOrThrow(COL_LAST_NAME)
            )

            val email = cursor.getString(
                cursor.getColumnIndexOrThrow(COL_EMAIL)
            )

            val password = cursor.getString(
                cursor.getColumnIndexOrThrow(COL_PASSWORD)
            )

            val role = cursor.getString(
                cursor.getColumnIndexOrThrow(COL_ROLE)
            )

            val profileImageIndex = cursor.getColumnIndex(COL_PROFILE_IMAGE)
            val profileImage = if (profileImageIndex != -1 && !cursor.isNull(profileImageIndex)) {
                cursor.getString(profileImageIndex)
            } else null

            cursor.close()

            return User(firstName, lastName, email, password, role, profileImage)

        }

        cursor.close()

        return null

    }

    fun addProduct(
        name: String,
        category: String,
        price: Double,
        description: String,
        imageUri: String,
        moderatorEmail: String,
        color: String = "",
        targetAudience: String = "All",
        inventory: Int = 10,
        size: String = "N/A"
    ): Boolean {

        val db = writableDatabase

        val values = ContentValues().apply {
            put(COL_PRODUCT_NAME, name)
            put(COL_CATEGORY, category)
            put(COL_PRICE, price)
            put(COL_PRODUCT_DESC, description)
            put(COL_STATUS, "pending")   // waiting for admin approval
            put(COL_SUBCATEGORY, color)
            put(COL_TARGET_AUDIENCE, targetAudience)
            put(COL_INVENTORY, inventory)
            put(COL_SIZE, size)
            put(COL_MODERATOR_EMAIL, moderatorEmail)
            put(COL_PRODUCT_IMAGE, imageUri)
        }

        val result = db.insert(TABLE_PRODUCTS, null, values)

        return result != -1L
    }

    fun updateProduct(
        productId: Int,
        name: String,
        category: String,
        price: Double,
        description: String,
        color: String = "",
        targetAudience: String = "All",
        inventory: Int = 10,
        size: String = "N/A"
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_PRODUCT_NAME, name)
            put(COL_CATEGORY, category)
            put(COL_PRICE, price)
            put(COL_PRODUCT_DESC, description)
            put(COL_SUBCATEGORY, color)
            put(COL_TARGET_AUDIENCE, targetAudience)
            put(COL_INVENTORY, inventory)
            put(COL_SIZE, size)
        }
        val result = db.update(
            TABLE_PRODUCTS,
            values,
            "$COL_PRODUCT_ID=?",
            arrayOf(productId.toString())
        )
        return result > 0
    }

    fun deleteProduct(productId: Int): Boolean {
        val db = writableDatabase
        db.delete("favorites", "product_id=?", arrayOf(productId.toString()))
        db.delete("cart", "product_id=?", arrayOf(productId.toString()))
        
        val values = ContentValues().apply {
            put(COL_STATUS, "deleted")
        }
        
        val result = db.update(
            TABLE_PRODUCTS,
            values,
            "$COL_PRODUCT_ID=?",
            arrayOf(productId.toString())
        )
        return result > 0
    }

    fun getProductsByModerator(email: String): Cursor {
        val db = readableDatabase
        return db.query(
            TABLE_PRODUCTS,
            null,
            "$COL_MODERATOR_EMAIL=? AND $COL_STATUS != ?",
            arrayOf(email, "deleted"),
            null,
            null,
            null
        )
    }


    fun getProductById(productId: Int): Cursor {
        val db = readableDatabase
        return db.query(
            TABLE_PRODUCTS,
            null,
            "$COL_PRODUCT_ID=?",
            arrayOf(productId.toString()),
            null,
            null,
            null
        )
    }

    fun getPendingProducts(): Cursor {
        val db = readableDatabase
        return db.query(
            TABLE_PRODUCTS,
            null,
            "$COL_STATUS=?",
            arrayOf("pending"),
            null,
            null,
            null
        )
    }

    fun approveProduct(productId: Int): Int {

        val db = writableDatabase

        val values = ContentValues().apply {
            put(COL_STATUS, "approved")
        }

        return db.update(
            TABLE_PRODUCTS,
            values,
            "$COL_PRODUCT_ID=?",
            arrayOf(productId.toString())
        )
    }

    fun getApprovedProducts(): Cursor {
        val db = readableDatabase
        return db.query(
            TABLE_PRODUCTS,
            null,
            "$COL_STATUS=?",
            arrayOf("approved"),
            null,
            null,
            null
        )
    }

    fun placeOrder(productId: Int, userEmail: String): Boolean {

        val db = writableDatabase

        val values = ContentValues().apply {
            put(COL_ORDER_PRODUCT_ID, productId)
            put(COL_ORDER_USER_EMAIL, userEmail)
            put(COL_ORDER_STATUS, "placed")
        }

        val result = db.insert(TABLE_ORDERS, null, values)

        return result != -1L
    }

    fun getAllOrders(): Cursor {
        val db = readableDatabase
        return db.query(
            TABLE_ORDERS,
            null,
            null,
            null,
            null,
            null,
            null
        )
    }

    fun updateOrderStatus(orderId: Int, newStatus: String): Int {

        val db = writableDatabase

        val values = ContentValues().apply {
            put(COL_ORDER_STATUS, newStatus)
        }

        return db.update(
            TABLE_ORDERS,
            values,
            "$COL_ORDER_ID=?",
            arrayOf(orderId.toString())
        )
    }

    fun getOrdersByUser(email: String): Cursor {
        val db = readableDatabase
        return db.rawQuery(
            "SELECT * FROM $TABLE_ORDERS WHERE $COL_ORDER_USER_EMAIL = ?",
            arrayOf(email)
        )
    }

    fun getProductNameById(productId: Int): String {

        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT $COL_PRODUCT_NAME FROM $TABLE_PRODUCTS WHERE $COL_PRODUCT_ID = ?",
            arrayOf(productId.toString())
        )

        var name = "Unknown"

        if (cursor.moveToFirst()) {
            name = cursor.getString(0)
        }

        cursor.close()
        return name
    }

    fun insertOrder(
        productId: Int,
        userEmail: String,
        name: String,
        phone: String,
        address: String,
        paymentMethod: String,
        status: String,
        size: String = "",
        color: String = ""
    ) {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COL_ORDER_PRODUCT_ID, productId)
            put(COL_ORDER_USER_EMAIL, userEmail)
            put(COL_ORDER_NAME, name)
            put(COL_ORDER_PHONE, phone)
            put(COL_ORDER_ADDRESS, address)
            put(COL_ORDER_PAYMENT, paymentMethod)
            put(COL_ORDER_STATUS, status)
            put("selected_size", size)
            put("selected_color", color)
        }

        db.insert(TABLE_ORDERS, null, values)
        db.close()
    }

    fun addToCart(productId: Int, userEmail: String, qty: Int = 1, size: String = "", color: String = "") {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("product_id", productId)
            put("user_email", userEmail)
            put("quantity", qty)
            put("selected_size", size)
            put("selected_color", color)
        }
        db.insert("cart", null, values)
    }

    fun getUserCart(userEmail: String): Cursor {
        val db = readableDatabase
        return db.rawQuery(
            "SELECT * FROM cart WHERE user_email = ?",
            arrayOf(userEmail)
        )
    }

    fun getProductPriceById(productId: Int): Double {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_PRICE FROM $TABLE_PRODUCTS WHERE $COL_PRODUCT_ID = ?",
            arrayOf(productId.toString())
        )
        var price = 0.0
        if (cursor.moveToFirst()) {
            price = cursor.getDouble(0)
        }
        cursor.close()
        return price
    }

    fun reduceInventory(productId: Int, amount: Int) {
        val db = writableDatabase
        db.execSQL(
            "UPDATE $TABLE_PRODUCTS SET $COL_INVENTORY = MAX(0, $COL_INVENTORY - ?) WHERE $COL_PRODUCT_ID = ?",
            arrayOf(amount, productId)
        )
    }

    fun isProductInCart(productId: Int, userEmail: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM cart WHERE product_id=? AND user_email=?",
            arrayOf(productId.toString(), userEmail)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun updateCartQuantity(productId: Int, userEmail: String, qty: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("quantity", qty)
        }
        db.update("cart", values, "product_id=? AND user_email=?", arrayOf(productId.toString(), userEmail))
    }

    fun removeFromCart(productId: Int, userEmail: String) {
        val db = writableDatabase
        db.delete(
            "cart",
            "product_id=? AND user_email=?",
            arrayOf(productId.toString(), userEmail)
        )
    }

    fun clearCart(userEmail: String) {
        val db = writableDatabase
        db.delete("cart", "user_email=?", arrayOf(userEmail))
        db.close()
    }

    fun getApprovedProductsByCategory(category: String): Cursor {
        val db = readableDatabase
        return db.query(
            TABLE_PRODUCTS,
            null,
            "$COL_STATUS=? AND $COL_CATEGORY LIKE ?",
            arrayOf("approved", "%$category%"),
            null,
            null,
            null
        )
    }

    fun addToFavorites(productId: Int, userEmail: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("product_id", productId)
            put("user_email", userEmail)
        }
        db.insert("favorites", null, values)
    }

    fun removeFromFavorites(productId: Int, userEmail: String) {
        val db = writableDatabase
        db.delete("favorites", "product_id=? AND user_email=?", arrayOf(productId.toString(), userEmail))
    }

    fun isFavorite(productId: Int, userEmail: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM favorites WHERE product_id=? AND user_email=?",
            arrayOf(productId.toString(), userEmail)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun getUserFavorites(userEmail: String): Cursor {
        val db = readableDatabase
        // Join with products table to get product details
        return db.rawQuery(
            "SELECT p.* FROM $TABLE_PRODUCTS p INNER JOIN favorites f ON p.$COL_PRODUCT_ID = f.product_id WHERE f.user_email=?",
            arrayOf(userEmail)
        )
    }
}

