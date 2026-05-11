package com.example.stylica.data.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "stylica.db"
        private const val DATABASE_VERSION = 17

        const val TABLE_USERS = "users"
        const val COL_ID = "id"
        const val COL_FIRST_NAME = "first_name"
        const val COL_LAST_NAME = "last_name"
        const val COL_EMAIL = "email"
        const val COL_PASSWORD = "password"
        const val COL_PROFILE_IMAGE = "profile_image"
        const val COL_ROLE = "role"
        const val COL_GENDER = "gender"
        const val COL_PHONE = "phone"
        const val COL_ADDRESS = "address"
        const val COL_DOMAIN = "domain"
        const val COL_REGISTERED_AT = "registered_at"

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
        const val COL_PRODUCT_CREATED_AT = "created_at"

        const val STATUS_PENDING_QA = "pending_qa"
        const val STATUS_PENDING_ADMIN = "pending_admin"
        const val STATUS_APPROVED = "approved"

        const val TABLE_ORDERS = "orders"
        const val COL_ORDER_ID = "order_id"
        const val COL_ORDER_PRODUCT_ID = "product_id"
        const val COL_ORDER_USER_EMAIL = "user_email"
        const val COL_ORDER_STATUS = "order_status"
        const val COL_ORDER_NAME = "order_name"
        const val COL_ORDER_PHONE = "order_phone"
        const val COL_ORDER_ADDRESS = "order_address"
        const val COL_ORDER_PAYMENT = "order_payment"
        const val COL_ORDER_COURIER = "courier"
        const val COL_ORDER_PLACED_AT = "ordered_at"

        const val TABLE_ANNOUNCEMENTS = "announcements"
        const val COL_ANN_ID = "ann_id"
        const val COL_ANN_TITLE = "title"
        const val COL_ANN_BODY = "body"
        const val COL_ANN_TYPE = "ann_type"
        const val COL_ANN_CREATED_AT = "ann_created_at"

        const val TABLE_COURIERS = "couriers"
        const val COL_COURIER_ID = "courier_id"
        const val COL_COURIER_NAME = "courier_name"
        const val COL_COURIER_PHONE = "courier_phone"

        const val TABLE_PAYMENT_COMPANIES = "payment_companies"
        const val COL_PC_ID = "pc_id"
        const val COL_PC_NAME = "pc_name"
        const val COL_PC_KIND = "pc_kind"

        const val TABLE_EMPLOYEES = "employees"
        const val COL_EMP_ID = "emp_row_id"
        const val COL_EMP_CODE = "employee_code"
        const val COL_EMP_FIRST = "emp_first_name"
        const val COL_EMP_LAST = "emp_last_name"
    }

    private fun nowIso(): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_USERS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_FIRST_NAME TEXT NOT NULL,
                $COL_LAST_NAME TEXT NOT NULL,
                $COL_EMAIL TEXT UNIQUE NOT NULL,
                $COL_PASSWORD TEXT NOT NULL,
                $COL_ROLE TEXT NOT NULL,
                $COL_PROFILE_IMAGE TEXT,
                $COL_GENDER TEXT,
                $COL_PHONE TEXT,
                $COL_ADDRESS TEXT,
                $COL_DOMAIN TEXT,
                $COL_REGISTERED_AT TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
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
                $COL_PRODUCT_IMAGE TEXT,
                $COL_PRODUCT_CREATED_AT TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
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
                selected_color TEXT,
                $COL_ORDER_COURIER TEXT,
                $COL_ORDER_PLACED_AT TEXT
            )
            """.trimIndent()
        )

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

        createAuxiliaryTables(db)
        seedDefaultCouriers(db)
    }

    private fun createAuxiliaryTables(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_ANNOUNCEMENTS (
                $COL_ANN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_ANN_TITLE TEXT NOT NULL,
                $COL_ANN_BODY TEXT,
                $COL_ANN_TYPE TEXT,
                $COL_ANN_CREATED_AT TEXT
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE $TABLE_COURIERS (
                $COL_COURIER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_COURIER_NAME TEXT NOT NULL,
                $COL_COURIER_PHONE TEXT
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE $TABLE_PAYMENT_COMPANIES (
                $COL_PC_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PC_NAME TEXT NOT NULL,
                $COL_PC_KIND TEXT
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE $TABLE_EMPLOYEES (
                $COL_EMP_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_EMP_CODE TEXT UNIQUE NOT NULL,
                $COL_EMP_FIRST TEXT NOT NULL,
                $COL_EMP_LAST TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    private fun seedDefaultCouriers(db: SQLiteDatabase) {
        val defaults = listOf(
            Triple("TCS", "021-111-123-456"),
            Triple("Leopard Courier", "021-111-456-789"),
            Triple("Pak Post", "051-920-5010")
        )
        for ((name, phone) in defaults) {
            val cv = ContentValues().apply {
                put(COL_COURIER_NAME, name)
                put(COL_COURIER_PHONE, phone)
            }
            db.insert(TABLE_COURIERS, null, cv)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 9) {
            db.execSQL("ALTER TABLE $TABLE_PRODUCTS ADD COLUMN $COL_PRODUCT_IMAGE TEXT")
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
            db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COL_PROFILE_IMAGE TEXT")
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
            db.execSQL("ALTER TABLE $TABLE_PRODUCTS ADD COLUMN $COL_TARGET_AUDIENCE TEXT DEFAULT 'All'")
        }

        if (oldVersion < 15) {
            db.execSQL("ALTER TABLE $TABLE_PRODUCTS ADD COLUMN $COL_INVENTORY INTEGER DEFAULT 10")
            db.execSQL("ALTER TABLE $TABLE_PRODUCTS ADD COLUMN $COL_SIZE TEXT DEFAULT 'N/A'")
        }

        if (oldVersion < 16) {
            db.execSQL("ALTER TABLE cart ADD COLUMN selected_size TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE cart ADD COLUMN selected_color TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE $TABLE_ORDERS ADD COLUMN selected_size TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE $TABLE_ORDERS ADD COLUMN selected_color TEXT DEFAULT ''")
        }

        if (oldVersion < 17) {
            db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COL_GENDER TEXT")
            db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COL_PHONE TEXT")
            db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COL_ADDRESS TEXT")
            db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COL_DOMAIN TEXT")
            db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COL_REGISTERED_AT TEXT")
            val iso = nowIso()
            db.execSQL("UPDATE $TABLE_USERS SET $COL_REGISTERED_AT = ? WHERE $COL_REGISTERED_AT IS NULL OR $COL_REGISTERED_AT = ''", arrayOf(iso))

            db.execSQL("ALTER TABLE $TABLE_PRODUCTS ADD COLUMN $COL_PRODUCT_CREATED_AT TEXT")
            db.execSQL("UPDATE $TABLE_PRODUCTS SET $COL_PRODUCT_CREATED_AT = ? WHERE $COL_PRODUCT_CREATED_AT IS NULL OR $COL_PRODUCT_CREATED_AT = ''", arrayOf(iso))
            db.execSQL("UPDATE $TABLE_PRODUCTS SET $COL_STATUS = '$STATUS_PENDING_ADMIN' WHERE $COL_STATUS = 'pending'")

            db.execSQL("ALTER TABLE $TABLE_ORDERS ADD COLUMN $COL_ORDER_COURIER TEXT")
            db.execSQL("ALTER TABLE $TABLE_ORDERS ADD COLUMN $COL_ORDER_PLACED_AT TEXT")

            createAuxiliaryTables(db)
            val c = db.rawQuery("SELECT COUNT(*) FROM $TABLE_COURIERS", null)
            var count = 0
            if (c.moveToFirst()) count = c.getInt(0)
            c.close()
            if (count == 0) seedDefaultCouriers(db)
        }
    }

    fun registerUser(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        role: String,
        gender: String? = null,
        phone: String? = null,
        address: String? = null,
        domain: String? = null
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_FIRST_NAME, firstName)
            put(COL_LAST_NAME, lastName)
            put(COL_EMAIL, email)
            put(COL_PASSWORD, password)
            put(COL_ROLE, role)
            put(COL_GENDER, gender)
            put(COL_PHONE, phone)
            put(COL_ADDRESS, address)
            put(COL_DOMAIN, domain)
            put(COL_REGISTERED_AT, nowIso())
        }
        return try {
            db.insertOrThrow(TABLE_USERS, null, values)
            true
        } catch (e: SQLiteConstraintException) {
            false
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
        profileImage: String? = null,
        gender: String? = null,
        phone: String? = null,
        address: String? = null,
        domain: String? = null
    ): Int {
        val db = writableDatabase
        val values = ContentValues()
        if (!firstName.isNullOrEmpty()) values.put(COL_FIRST_NAME, firstName)
        if (!lastName.isNullOrEmpty()) values.put(COL_LAST_NAME, lastName)
        if (!password.isNullOrEmpty()) values.put(COL_PASSWORD, password)
        if (!profileImage.isNullOrEmpty()) values.put(COL_PROFILE_IMAGE, profileImage)
        if (gender != null) values.put(COL_GENDER, gender)
        if (phone != null) values.put(COL_PHONE, phone)
        if (address != null) values.put(COL_ADDRESS, address)
        if (domain != null) values.put(COL_DOMAIN, domain)
        if (values.size() == 0) return 0
        return db.update(TABLE_USERS, values, "$COL_EMAIL=?", arrayOf(email))
    }

    fun deleteUser(email: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_USERS, "$COL_EMAIL=?", arrayOf(email))
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
        if (!cursor.moveToFirst()) {
            cursor.close()
            return null
        }
        val firstName = cursor.getString(cursor.getColumnIndexOrThrow(COL_FIRST_NAME))
        val lastName = cursor.getString(cursor.getColumnIndexOrThrow(COL_LAST_NAME))
        val em = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL))
        val password = cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSWORD))
        val role = cursor.getString(cursor.getColumnIndexOrThrow(COL_ROLE))
        val profileImageIndex = cursor.getColumnIndex(COL_PROFILE_IMAGE)
        val profileImage = if (profileImageIndex != -1 && !cursor.isNull(profileImageIndex)) {
            cursor.getString(profileImageIndex)
        } else null
        fun colStr(name: String): String? {
            val i = cursor.getColumnIndex(name)
            return if (i != -1 && !cursor.isNull(i)) cursor.getString(i) else null
        }
        val gender = colStr(COL_GENDER)
        val phone = colStr(COL_PHONE)
        val address = colStr(COL_ADDRESS)
        val domain = colStr(COL_DOMAIN)
        val registeredAt = colStr(COL_REGISTERED_AT)
        cursor.close()
        return User(
            firstName, lastName, em, password, role, profileImage,
            gender, phone, address, domain, registeredAt
        )
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
            put(COL_STATUS, STATUS_PENDING_QA)
            put(COL_SUBCATEGORY, color)
            put(COL_TARGET_AUDIENCE, targetAudience)
            put(COL_INVENTORY, inventory)
            put(COL_SIZE, size)
            put(COL_MODERATOR_EMAIL, moderatorEmail)
            put(COL_PRODUCT_IMAGE, imageUri)
            put(COL_PRODUCT_CREATED_AT, nowIso())
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
            arrayOf(STATUS_PENDING_ADMIN),
            null,
            null,
            null
        )
    }

    fun getPendingQaForModerator(moderatorEmail: String): Cursor {
        val db = readableDatabase
        return db.query(
            TABLE_PRODUCTS,
            null,
            "$COL_MODERATOR_EMAIL=? AND $COL_STATUS=?",
            arrayOf(moderatorEmail, STATUS_PENDING_QA),
            null,
            null,
            null
        )
    }

    fun moderatorApproveProductQuality(productId: Int, moderatorEmail: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_STATUS, STATUS_PENDING_ADMIN)
        }
        return db.update(
            TABLE_PRODUCTS,
            values,
            "$COL_PRODUCT_ID=? AND $COL_MODERATOR_EMAIL=? AND $COL_STATUS=?",
            arrayOf(productId.toString(), moderatorEmail, STATUS_PENDING_QA)
        )
    }

    fun approveProduct(productId: Int): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_STATUS, STATUS_APPROVED)
        }
        return db.update(
            TABLE_PRODUCTS,
            values,
            "$COL_PRODUCT_ID=? AND ($COL_STATUS=? OR $COL_STATUS=? OR $COL_STATUS=?)",
            arrayOf(
                productId.toString(),
                STATUS_PENDING_ADMIN,
                STATUS_PENDING_QA,
                "pending"
            )
        )
    }

    fun getApprovedProducts(): Cursor {
        val db = readableDatabase
        return db.query(
            TABLE_PRODUCTS,
            null,
            "$COL_STATUS=?",
            arrayOf(STATUS_APPROVED),
            null,
            null,
            null
        )
    }

    fun searchModeratorProductsForAdmin(
        moderatorFirst: String,
        moderatorLast: String,
        moderatorRegDatePart: String,
        category: String,
        subcategory: String
    ): Cursor {
        val db = readableDatabase
        val args = ArrayList<String>()
        val where = StringBuilder("p.$COL_STATUS != ?")
        args.add("deleted")
        if (moderatorFirst.isNotBlank()) {
            where.append(" AND u.$COL_FIRST_NAME LIKE ?")
            args.add("%${moderatorFirst.trim()}%")
        }
        if (moderatorLast.isNotBlank()) {
            where.append(" AND u.$COL_LAST_NAME LIKE ?")
            args.add("%${moderatorLast.trim()}%")
        }
        if (moderatorRegDatePart.isNotBlank()) {
            where.append(" AND u.$COL_REGISTERED_AT LIKE ?")
            args.add("%${moderatorRegDatePart.trim()}%")
        }
        if (category.isNotBlank()) {
            where.append(" AND p.$COL_CATEGORY LIKE ?")
            args.add("%${category.trim()}%")
        }
        if (subcategory.isNotBlank()) {
            where.append(" AND p.$COL_SUBCATEGORY LIKE ?")
            args.add("%${subcategory.trim()}%")
        }
        val sql =
            "SELECT p.* FROM $TABLE_PRODUCTS p LEFT JOIN $TABLE_USERS u ON p.$COL_MODERATOR_EMAIL = u.$COL_EMAIL WHERE ${where}"
        return db.rawQuery(sql, args.toTypedArray())
    }

    fun getModeratorProductsByCategory(categoryFilter: String): Cursor {
        val db = readableDatabase
        return if (categoryFilter.isBlank()) {
            db.query(
                TABLE_PRODUCTS,
                null,
                "$COL_STATUS != ? AND $COL_MODERATOR_EMAIL IS NOT NULL AND $COL_MODERATOR_EMAIL != ''",
                arrayOf("deleted"),
                null,
                null,
                null
            )
        } else {
            db.query(
                TABLE_PRODUCTS,
                null,
                "$COL_STATUS != ? AND $COL_CATEGORY LIKE ? AND $COL_MODERATOR_EMAIL IS NOT NULL AND $COL_MODERATOR_EMAIL != ''",
                arrayOf("deleted", "%${categoryFilter.trim()}%"),
                null,
                null,
                null
            )
        }
    }

    fun searchModerators(
        domainPart: String,
        registeredAtPart: String,
        productCategory: String
    ): Cursor {
        val db = readableDatabase
        val args = ArrayList<String>()
        args.add("moderator")
        val where = StringBuilder("u.$COL_ROLE = ?")
        if (domainPart.isNotBlank()) {
            where.append(" AND IFNULL(u.$COL_DOMAIN,'') LIKE ?")
            args.add("%${domainPart.trim()}%")
        }
        if (registeredAtPart.isNotBlank()) {
            where.append(" AND IFNULL(u.$COL_REGISTERED_AT,'') LIKE ?")
            args.add("%${registeredAtPart.trim()}%")
        }
        if (productCategory.isNotBlank()) {
            where.append(
                " AND EXISTS (SELECT 1 FROM $TABLE_PRODUCTS p WHERE p.$COL_MODERATOR_EMAIL = u.$COL_EMAIL AND p.$COL_CATEGORY LIKE ? AND p.$COL_STATUS != 'deleted')"
            )
            args.add("%${productCategory.trim()}%")
        }
        val sql = "SELECT u.* FROM $TABLE_USERS u WHERE $where ORDER BY u.$COL_REGISTERED_AT DESC"
        return db.rawQuery(sql, args.toTypedArray())
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
        return db.query(TABLE_ORDERS, null, null, null, null, null, null)
    }

    fun getOrdersForModerator(moderatorEmail: String): Cursor {
        val db = readableDatabase
        val sql =
            "SELECT o.* FROM $TABLE_ORDERS o INNER JOIN $TABLE_PRODUCTS p ON o.$COL_ORDER_PRODUCT_ID = p.$COL_PRODUCT_ID WHERE p.$COL_MODERATOR_EMAIL = ? ORDER BY o.$COL_ORDER_ID DESC"
        return db.rawQuery(sql, arrayOf(moderatorEmail))
    }

    fun searchOrdersForAdmin(userEmailPart: String, datePart: String): Cursor {
        val db = readableDatabase
        val args = ArrayList<String>()
        val where = StringBuilder("1=1")
        if (userEmailPart.isNotBlank()) {
            where.append(" AND o.$COL_ORDER_USER_EMAIL LIKE ?")
            args.add("%${userEmailPart.trim()}%")
        }
        if (datePart.isNotBlank()) {
            where.append(" AND IFNULL(o.$COL_ORDER_PLACED_AT,'') LIKE ?")
            args.add("%${datePart.trim()}%")
        }
        val sql = "SELECT o.* FROM $TABLE_ORDERS o WHERE $where ORDER BY o.$COL_ORDER_ID DESC"
        return db.rawQuery(sql, args.toTypedArray())
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

    fun updateOrderPayment(orderId: Int, paymentMethod: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_ORDER_PAYMENT, paymentMethod)
        }
        return db.update(
            TABLE_ORDERS,
            values,
            "$COL_ORDER_ID=?",
            arrayOf(orderId.toString())
        )
    }

    fun deleteOrder(orderId: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_ORDERS, "$COL_ORDER_ID=?", arrayOf(orderId.toString()))
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
        color: String = "",
        courier: String = ""
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
            put(COL_ORDER_COURIER, courier)
            put(COL_ORDER_PLACED_AT, nowIso())
        }
        db.insert(TABLE_ORDERS, null, values)
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
        return db.rawQuery("SELECT * FROM cart WHERE user_email = ?", arrayOf(userEmail))
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
        db.delete("cart", "product_id=? AND user_email=?", arrayOf(productId.toString(), userEmail))
    }

    fun clearCart(userEmail: String) {
        val db = writableDatabase
        db.delete("cart", "user_email=?", arrayOf(userEmail))
    }

    fun getApprovedProductsByCategory(category: String): Cursor {
        val db = readableDatabase
        return db.query(
            TABLE_PRODUCTS,
            null,
            "$COL_STATUS=? AND $COL_CATEGORY LIKE ?",
            arrayOf(STATUS_APPROVED, "%$category%"),
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
        return db.rawQuery(
            "SELECT p.* FROM $TABLE_PRODUCTS p INNER JOIN favorites f ON p.$COL_PRODUCT_ID = f.product_id WHERE f.user_email=?",
            arrayOf(userEmail)
        )
    }

    fun getAllCouriers(): Cursor {
        return readableDatabase.query(TABLE_COURIERS, null, null, null, null, null, COL_COURIER_NAME)
    }

    fun insertCourier(name: String, phone: String): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_COURIER_NAME, name)
            put(COL_COURIER_PHONE, phone)
        }
        return db.insert(TABLE_COURIERS, null, cv)
    }

    fun updateCourier(id: Int, name: String, phone: String): Int {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_COURIER_NAME, name)
            put(COL_COURIER_PHONE, phone)
        }
        return db.update(TABLE_COURIERS, cv, "$COL_COURIER_ID=?", arrayOf(id.toString()))
    }

    fun deleteCourier(id: Int): Int {
        return writableDatabase.delete(TABLE_COURIERS, "$COL_COURIER_ID=?", arrayOf(id.toString()))
    }

    fun getAllPaymentCompanies(): Cursor {
        return readableDatabase.query(TABLE_PAYMENT_COMPANIES, null, null, null, null, null, COL_PC_NAME)
    }

    fun insertPaymentCompany(name: String, kind: String): Long {
        val cv = ContentValues().apply {
            put(COL_PC_NAME, name)
            put(COL_PC_KIND, kind)
        }
        return writableDatabase.insert(TABLE_PAYMENT_COMPANIES, null, cv)
    }

    fun updatePaymentCompany(id: Int, name: String, kind: String): Int {
        val cv = ContentValues().apply {
            put(COL_PC_NAME, name)
            put(COL_PC_KIND, kind)
        }
        return writableDatabase.update(TABLE_PAYMENT_COMPANIES, cv, "$COL_PC_ID=?", arrayOf(id.toString()))
    }

    fun deletePaymentCompany(id: Int): Int {
        return writableDatabase.delete(TABLE_PAYMENT_COMPANIES, "$COL_PC_ID=?", arrayOf(id.toString()))
    }

    fun getAllAnnouncements(): Cursor {
        return readableDatabase.query(TABLE_ANNOUNCEMENTS, null, null, null, null, null, "$COL_ANN_CREATED_AT DESC")
    }

    fun searchAnnouncements(typePart: String, datePart: String): Cursor {
        val args = ArrayList<String>()
        val where = StringBuilder("1=1")
        if (typePart.isNotBlank()) {
            where.append(" AND IFNULL($COL_ANN_TYPE,'') LIKE ?")
            args.add("%${typePart.trim()}%")
        }
        if (datePart.isNotBlank()) {
            where.append(" AND IFNULL($COL_ANN_CREATED_AT,'') LIKE ?")
            args.add("%${datePart.trim()}%")
        }
        return readableDatabase.rawQuery(
            "SELECT * FROM $TABLE_ANNOUNCEMENTS WHERE $where ORDER BY $COL_ANN_CREATED_AT DESC",
            args.toTypedArray()
        )
    }

    fun insertAnnouncement(title: String, body: String, type: String): Long {
        val cv = ContentValues().apply {
            put(COL_ANN_TITLE, title)
            put(COL_ANN_BODY, body)
            put(COL_ANN_TYPE, type)
            put(COL_ANN_CREATED_AT, nowIso())
        }
        return writableDatabase.insert(TABLE_ANNOUNCEMENTS, null, cv)
    }

    fun updateAnnouncement(id: Int, title: String, body: String, type: String): Int {
        val cv = ContentValues().apply {
            put(COL_ANN_TITLE, title)
            put(COL_ANN_BODY, body)
            put(COL_ANN_TYPE, type)
        }
        return writableDatabase.update(TABLE_ANNOUNCEMENTS, cv, "$COL_ANN_ID=?", arrayOf(id.toString()))
    }

    fun deleteAnnouncement(id: Int): Int {
        return writableDatabase.delete(TABLE_ANNOUNCEMENTS, "$COL_ANN_ID=?", arrayOf(id.toString()))
    }

    fun searchEmployees(codePart: String, firstPart: String, lastPart: String): Cursor {
        val args = ArrayList<String>()
        val where = StringBuilder("1=1")
        if (codePart.isNotBlank()) {
            where.append(" AND $COL_EMP_CODE LIKE ?")
            args.add("%${codePart.trim()}%")
        }
        if (firstPart.isNotBlank()) {
            where.append(" AND $COL_EMP_FIRST LIKE ?")
            args.add("%${firstPart.trim()}%")
        }
        if (lastPart.isNotBlank()) {
            where.append(" AND $COL_EMP_LAST LIKE ?")
            args.add("%${lastPart.trim()}%")
        }
        return readableDatabase.rawQuery(
            "SELECT * FROM $TABLE_EMPLOYEES WHERE $where ORDER BY $COL_EMP_CODE",
            args.toTypedArray()
        )
    }

    fun insertEmployee(code: String, first: String, last: String): Long {
        val cv = ContentValues().apply {
            put(COL_EMP_CODE, code)
            put(COL_EMP_FIRST, first)
            put(COL_EMP_LAST, last)
        }
        return try {
            writableDatabase.insertOrThrow(TABLE_EMPLOYEES, null, cv)
        } catch (_: SQLiteConstraintException) {
            -1L
        }
    }

    fun updateEmployee(rowId: Int, code: String, first: String, last: String): Int {
        val cv = ContentValues().apply {
            put(COL_EMP_CODE, code)
            put(COL_EMP_FIRST, first)
            put(COL_EMP_LAST, last)
        }
        return writableDatabase.update(TABLE_EMPLOYEES, cv, "$COL_EMP_ID=?", arrayOf(rowId.toString()))
    }

    fun deleteEmployee(rowId: Int): Int {
        return writableDatabase.delete(TABLE_EMPLOYEES, "$COL_EMP_ID=?", arrayOf(rowId.toString()))
    }
}
