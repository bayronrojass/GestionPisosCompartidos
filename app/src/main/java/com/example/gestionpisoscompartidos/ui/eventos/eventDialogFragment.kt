package com.example.gestionpisoscompartidos.ui.eventos

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.Selection.setSelection
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.gestionpisoscompartidos.R
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Date

class eventDialogFragment : DialogFragment() {
    interface DialogListener {
        fun onDataSaved(eventTitle: String)
    }

    private val viewModel: EventDialogViewModel by viewModels()
    private var listener: DialogListener? = null

    private var positiveButton: Button? = null

    private var dateButton: Button? = null
    private var titleText: EditText? = null

    private var pickedDate: Date? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        activity?.let { context ->
            val builder = MaterialAlertDialogBuilder(requireContext())
            val inflater = requireActivity().layoutInflater
            val dialogView = inflater.inflate(R.layout.fragment_evento_dialog, null)

            titleText = dialogView.findViewById(R.id.edit_title)

            dateButton = dialogView.findViewById(R.id.buttonEventDate)

            dateButton?.setOnClickListener {
                showMaterialDatePicker()
            }

            builder
                .setView(dialogView)
                .setTitle("¡Crea un evento!")
                .setPositiveButton("Guardar") { _, _ ->
                    val eventTitle = titleText?.text?.toString()?.trim() ?: ""
                    listener?.onDataSaved(eventTitle)
                }.setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.cancel()
                }

            val dialog = builder.create()

            dialog.setOnShowListener {
                positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                positiveButton?.isEnabled = false

                titleText?.addTextChangedListener(
                    object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {
                            val eventTitle = s?.toString() ?: ""
                            updateButtonState(eventTitle, pickedDate)
                            showError(eventTitle)
                        }

                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int,
                        ) {}

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int,
                        ) {}
                    },
                )

                val initialText = titleText?.text?.toString() ?: ""
                updateButtonState(initialText, pickedDate)
            }

            dialog
        } ?: throw IllegalStateException("Activity cannot be null")

    private fun updateButtonState(
        eventTitle: String,
        pickedDate: Date? = null,
    ) {
        val state = viewModel.buttonState(eventTitle, pickedDate)
        positiveButton?.isEnabled = state
    }

    private fun showError(text: String) {
        if (text.isBlank()) {
            titleText?.error = "Introduzca un título para el evento"
        } else {
            titleText?.error = null
        }
    }

    fun setDialogListener(listener: DialogListener) {
        this.listener = listener
    }

    private fun showMaterialDatePicker() {
        val builder =
            MaterialDatePicker.Builder
                .datePicker()
                .setTitleText("Selecciona la fecha del evento")

        if (pickedDate != null) {
            builder.setSelection(pickedDate!!.time)
        } else {
            builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds())
        }

        val datePicker = builder.build()

        datePicker.addOnPositiveButtonClickListener { selectedDate ->
            pickedDate = Date(selectedDate)
            updateButtonState(getEventTitle(), pickedDate)
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun getEventTitle(): String {
        if (titleText?.text?.isEmpty() == true) {
            return ""
        }
        return titleText?.text.toString()
    }

    private fun getDate(): Date? = pickedDate
}
