package io.horizontalsystems.marketkit.storage

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.horizontalsystems.marketkit.models.CoinType
import java.math.BigDecimal

class DatabaseTypeConverters {
    private val gson by lazy { Gson() }

    @TypeConverter
    fun fromCoinType(coinType: CoinType?): String {
        return coinType?.id ?: ""
    }

    @TypeConverter
    fun toCoinType(value: String): CoinType {
        return CoinType.fromId(value)
    }

    @TypeConverter
    fun fromMap(map: Map<String, String>): String {
        return gson.toJson(map)
    }

    @TypeConverter
    fun toMap(value: String): Map<String, String> {
        return gson.fromJson(value, object : TypeToken<Map<String, String>>() {}.type)
    }

    @TypeConverter
    fun fromBigDecimal(bigDecimal: BigDecimal): String? {
        return bigDecimal.toPlainString()
    }

    @TypeConverter
    fun toBigDecimal(value: String): BigDecimal{
        return BigDecimal(value)
    }
}