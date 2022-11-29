package uz.nurlibaydev.moneymanager.presentation.main

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources.getColorStateList
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import uz.nurlibaydev.moneymanager.R
import uz.nurlibaydev.moneymanager.data.models.TransactionModel
import uz.nurlibaydev.moneymanager.databinding.ScreenInsertionBinding
import uz.nurlibaydev.moneymanager.utils.CategoryOptions
import uz.nurlibaydev.moneymanager.utils.extenions.onClick
import uz.nurlibaydev.moneymanager.utils.extenions.showMessage
import java.text.SimpleDateFormat
import java.util.*

/**
 *  Created by Nurlibay Koshkinbaev on 29/11/2022 14:20
 */

class InsertionScreen : Fragment(R.layout.screen_insertion) {

    private lateinit var etTitle: EditText
    private lateinit var etCategory: AutoCompleteTextView
    private lateinit var etAmount: EditText
    private lateinit var etDate: EditText
    private lateinit var btnSaveData: Button
    private lateinit var radioGroup: RadioGroup
    private lateinit var rbExpense: RadioButton
    private lateinit var rbIncome: RadioButton
    private lateinit var etNote: EditText
    private lateinit var toolbarLinear: LinearLayout

    private var type: Int = 1
    private var amount: Double = 0.0
    private var date: Long = 0
    private var invertedDate: Long = 0

    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var isSubmitted: Boolean = false

    private val binding: ScreenInsertionBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backBtn.onClick {
            findNavController().popBackStack()
        }
        initItem()

        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid
        if (uid != null) {
            dbRef = FirebaseDatabase.getInstance().getReference(uid)
        }

        auth = FirebaseAuth.getInstance()
        etCategory = binding.category

        val listExpense = CategoryOptions.expenseCategory()
        val expenseAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, listExpense)
        etCategory.setAdapter(expenseAdapter)

        radioGroup.setOnCheckedChangeListener { _, checkedID ->
            etCategory.text.clear() //clear the category autocompletetextview when the type changes
            if (checkedID == R.id.rbExpense) {
                type = 1 //expense
                setBackgroundColor()
                etCategory.setAdapter(expenseAdapter) //if expense type selected, the set list expense array in category menu
            }
            if (checkedID == R.id.rbIncome) {
                type = 2 //income
                setBackgroundColor()
                //if expense type selected, the set list income array in category menu :
                val listIncome = CategoryOptions.incomeCategory()
                val incomeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, listIncome)
                etCategory.setAdapter(incomeAdapter)
            }
        }

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        val currentDate = sdf.parse(sdf.format(System.currentTimeMillis())) //take current date
        date = currentDate!!.time //initialized date value to current date as the default value
        etDate.onClick {
            clickDatePicker()
        }

        btnSaveData.setOnClickListener {
            if (!isSubmitted) {
                saveTransactionData()
            } else {
                Snackbar.make(requireActivity().findViewById(android.R.id.content), "You have saved the transaction data", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun setBackgroundColor() {
        if (type == 1) {
            rbExpense.setBackgroundResource(R.drawable.radio_selected_expense)
            rbIncome.setBackgroundResource(R.drawable.radio_not_selected)
            toolbarLinear.setBackgroundResource(R.drawable.bg_insert_expense)
            btnSaveData.backgroundTintList = getColorStateList(requireContext(), R.color.orangePrimary)
            requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.orangePrimary)
        } else {
            rbIncome.setBackgroundResource(R.drawable.radio_selected_income)
            rbExpense.setBackgroundResource(R.drawable.radio_not_selected)
            toolbarLinear.setBackgroundResource(R.drawable.bg_insert_income)
            btnSaveData.backgroundTintList = getColorStateList(requireContext(), R.color.toscaSecondary)
            requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.toscaSecondary)
        }
    }

    private fun initItem() {
        etTitle = binding.title
        etCategory = binding.category
        etAmount = binding.amount
        etDate = binding.date
        btnSaveData = binding.saveButton
        radioGroup = binding.typeRadioGroup
        rbExpense = binding.rbExpense
        rbIncome = binding.rbIncome
        etNote = binding.note
        toolbarLinear = binding.toolbarLinear
    }

    private fun clickDatePicker() {
        val myCalendar = Calendar.getInstance()
        val year = myCalendar.get(Calendar.YEAR)
        val month = myCalendar.get(Calendar.MONTH)
        val day = myCalendar.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(
            requireContext(), { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                val selectedDate = "$selectedDayOfMonth/${selectedMonth + 1}/$selectedYear"
                etDate.text = null
                etDate.hint = selectedDate

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                val theDate = sdf.parse(selectedDate)
                date = theDate!!.time //convert date to millisecond

            },
            year,
            month,
            day
        )
        dpd.show()
    }

    private fun saveTransactionData() {
        //getting values from form input user:
        val title = etTitle.text.toString()
        val category = etCategory.text.toString()
        val amountEt = etAmount.text.toString()
        val note = etNote.text.toString()

        if (amountEt.isEmpty()) {
            etAmount.error = "Please enter Amount"
        } else if (title.isEmpty()) {
            etTitle.error = "Please enter Title"
        } else if (category.isEmpty()) {
            etCategory.error = "Please enter Category"
        } else {
            amount = etAmount.text.toString().toDouble() //convert to double type

            val transactionID = dbRef.push().key!!
            invertedDate =
                date * -1 //convert millis value to negative, so it can be sort as descending order
            val transaction = TransactionModel(
                transactionID,
                type,
                title,
                category,
                amount,
                date,
                note,
                invertedDate
            ) //object of data class

            dbRef.child(transactionID).setValue(transaction)
                .addOnCompleteListener {
                    showMessage("Data Inserted Successfully")
                    findNavController().popBackStack()
                }.addOnFailureListener { err ->
                    showMessage("Error ${err.message}")
                }
            isSubmitted = true
        }
    }
}