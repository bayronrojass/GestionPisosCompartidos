package com.example.gestionpisoscompartidos.ui.eventos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.google.android.material.datepicker.MaterialDatePicker

@Composable
fun MaterialDatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (Long) -> Unit,
    initialDate: Long? = null,
) {
    val context = LocalContext.current
    val fragmentManager = (context as? FragmentActivity)?.supportFragmentManager

    DisposableEffect(Unit) {
        val builder =
            MaterialDatePicker.Builder
                .datePicker()
                .setTitleText("Selecciona la fecha del evento")

        initialDate?.let { builder.setSelection(it) }
            ?: builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds())

        val datePicker = builder.build()

        datePicker.addOnPositiveButtonClickListener { selectedDate ->
            onDateSelected(selectedDate)
        }

        datePicker.addOnDismissListener {
            onDismissRequest()
        }

        datePicker.show(fragmentManager ?: return@DisposableEffect onDispose {}, "DATE_PICKER")

        onDispose {
            datePicker.dismiss()
        }
    }
}
