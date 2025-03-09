package com.tstool.trackexpenses.ui.view.create.helper

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AlertDialog
import com.tstool.trackexpenses.databinding.DialogMonthPickerBinding
import java.util.Calendar

object MonthPickerHelper {

    fun showMonthPicker(
        context: Context,
        onMonthSelected: (startOfMonth: Long, endOfMonth: Long) -> Unit
    ) {
        val binding = DialogMonthPickerBinding.inflate(android.view.LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Khởi tạo giá trị ban đầu
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1 // Tháng từ 1-12

        // Cấu hình Month Picker
        binding.pickerMonth.apply {
            minValue = 1
            maxValue = 12
            value = currentMonth
            displayedValues = arrayOf(
                "Thg 1", "Thg 2", "Thg 3", "Thg 4", "Thg 5", "Thg 6",
                "Thg 7", "Thg 8", "Thg 9", "Thg 10", "Thg 11", "Thg 12"
            )
            wrapSelectorWheel = true // Cho phép vòng lặp
        }

        // Cấu hình Year Picker
        binding.pickerYear.apply {
            minValue = currentYear - 10 // Từ 10 năm trước
            maxValue = currentYear + 10 // Đến 10 năm sau
            value = currentYear
            wrapSelectorWheel = false // Không vòng lặp cho năm
        }

        // Xử lý nút Cancel
        binding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Xử lý nút OK
        binding.btnOk.setOnClickListener {
            val selectedMonth = binding.pickerMonth.value - 1 // Chuyển về 0-11
            val selectedYear = binding.pickerYear.value

            // Tính ngày đầu tiên của tháng
            val startCalendar = Calendar.getInstance().apply {
                set(selectedYear, selectedMonth, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfMonth = startCalendar.timeInMillis

            // Tính ngày cuối cùng của tháng
            val endCalendar = startCalendar.apply {
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            val endOfMonth = endCalendar.timeInMillis

            // Gọi callback với kết quả
            onMonthSelected(startOfMonth, endOfMonth)
            dialog.dismiss()
        }

        // Hiển thị dialog
        dialog.show()
    }
}