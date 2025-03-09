package com.tstool.trackexpenses.utils.ktx

import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.widget.AppCompatEditText
import com.tstool.trackexpenses.data.model.ExpenseTag
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.*

fun AppCompatEditText.formatCurrencyInput() {
    addTextChangedListener(object : TextWatcher {
        private var current = ""

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            if (s.toString() != current) {
                removeTextChangedListener(this)
                val cleanString = s.toString().replace(".", "")
                if (cleanString.isNotEmpty()) {
                    try {
                        val parsed = cleanString.toDouble()
                        val formatter = DecimalFormat("#,###").apply {
                            decimalFormatSymbols = decimalFormatSymbols.apply {
                                groupingSeparator = '.'
                            }
                        }
                        current = formatter.format(parsed)
                        setText(current)
                        setSelection(current.length)
                    } catch (e: NumberFormatException) {
                        // Bỏ qua nếu không parse được
                    }
                }
                addTextChangedListener(this)
            }
        }
    })
}

// Extension mới để lấy giá trị Double từ EditText đã định dạng
fun AppCompatEditText.getCurrencyValue(): Double? {
    val cleanString = text.toString().replace(".", "").trim()
    return cleanString.toDoubleOrNull()
}

// Chuyển Double thành chuỗi tiền tệ để gán lại EditText
fun Double?.toCurrencyString(): String {
    if (this == null) return ""
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    formatter.minimumFractionDigits = 0
    formatter.maximumFractionDigits = 2
    return formatter.format(this)
}

fun Long.toFormattedDateTime(): String {
    val sdf = SimpleDateFormat("HH:mm - dd 'thg' M yyyy", Locale("vi", "VN"))
    return sdf.format(Date(this))
}


fun getResourceByTag(nameTag: String): Int? {
    return ExpenseTag.entries.firstOrNull { it.nameTag == nameTag }?.resource
}