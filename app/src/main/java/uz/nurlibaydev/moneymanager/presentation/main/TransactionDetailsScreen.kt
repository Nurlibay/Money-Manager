package uz.nurlibaydev.moneymanager.presentation.main

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import uz.nurlibaydev.moneymanager.R
import uz.nurlibaydev.moneymanager.data.models.TransactionModel
import uz.nurlibaydev.moneymanager.databinding.ScreenTransactionDetailsBinding
import uz.nurlibaydev.moneymanager.utils.CategoryOptions
import uz.nurlibaydev.moneymanager.utils.extenions.onClick
import uz.nurlibaydev.moneymanager.utils.extenions.showMessage
import java.text.SimpleDateFormat
import java.util.*

class TransactionDetailsScreen : Fragment(R.layout.screen_transaction_details){

    private lateinit var tvAmountDetails: TextView
    private lateinit var tvTypeDetails: TextView
    private lateinit var tvTitleDetails: TextView
    private lateinit var tvDateDetails: TextView
    private lateinit var tvNoteDetails: TextView
    private lateinit var tvCategoryDetails: TextView
    private lateinit var detailsTitle: RelativeLayout

    private val binding: ScreenTransactionDetailsBinding by viewBinding()
    private val args: TransactionDetailsScreenArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }


        binding.updateData.onClick {
            openUpdateDialog(args.transaction.title.toString())
        }

        deleteData()

        initView() //call method for initialized each ui item
        setValuesToViews() //call method for output the value on db
    }

    private fun deleteData() {
        val deleteData: ImageButton = binding.deleteData
        val alertBox = AlertDialog.Builder(requireContext())
        deleteData.setOnClickListener {
            alertBox.setTitle("Are you sure?")
            alertBox.setMessage("Do you want to delete this transaction?")
            alertBox.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                deleteRecord(args.transaction.transactionID.toString())
            }
            alertBox.setNegativeButton("No") { _: DialogInterface, _: Int -> }
            alertBox.show()
        }
    }

    private fun deleteRecord(transactionID: String) {
        // --Initialize Firebase Auth and firebase database--
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid
        if (uid != null) {
            val dbRef = FirebaseDatabase.getInstance().getReference(uid).child(transactionID) //initialize database with uid as the parent
            val mTask = dbRef.removeValue()

            mTask.addOnSuccessListener {
                showMessage("Transaction Data Deleted")
                findNavController().popBackStack()
            }.addOnFailureListener { error ->
                showMessage("Deleting Err ${error.message}")
            }
        }
    }

    private fun initView() {
        tvAmountDetails = binding.tvAmountDetails
        tvTypeDetails = binding.tvTypeDetails
        tvTitleDetails = binding.tvTitleDetails
        tvDateDetails = binding.tvDateDetails
        tvNoteDetails = binding.tvNoteDetails
        tvCategoryDetails = binding.tvCategoryDetails
        detailsTitle = binding.transactionDetailsTitle
    }

    private fun setValuesToViews(){
        tvTitleDetails.text =  args.transaction.title
        val type: Int = args.transaction.type?: 0
        val amount: Double = args.transaction.amount?: 0.0
        tvAmountDetails.text = amount.toString()

        if (type == 1) {
            tvTypeDetails.text = getString(R.string.expense_transaction)
            tvAmountDetails.setTextColor(Color.parseColor("#ff9f1c"))
            detailsTitle.setBackgroundResource(R.drawable.bg_details_expense)
        }else{
            tvTypeDetails.text = getString(R.string.income_transaction)
            tvAmountDetails.setTextColor(Color.parseColor("#2ec4b6"))
            detailsTitle.setBackgroundResource(R.drawable.bg_details_income)
            requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.toscaSecondary)
        }

        val date: Long = args.transaction.date?: 0L
        val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
        val result = Date(date)
        tvDateDetails.text = simpleDateFormat.format(result)

        tvCategoryDetails.text =  args.transaction.category
        tvNoteDetails.text =  args.transaction.note

    }

    private fun openUpdateDialog(title: String){
        val mDialog = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val mDialogView = inflater.inflate(R.layout.update_dialog, null)
        mDialog.setView(mDialogView)

        //---Initialize item---
        val etTitle = mDialogView.findViewById<EditText>(R.id.titleUpdate)
        val etCategory = mDialogView.findViewById<AutoCompleteTextView>(R.id.categoryUpdate)
        val etAmount = mDialogView.findViewById<EditText>(R.id.amountUpdate)
        val etDate = mDialogView.findViewById<EditText>(R.id.dateUpdate)
        val btnUpdateData = mDialogView.findViewById<Button>(R.id.updateButton)
        val etNote = mDialogView.findViewById<EditText>(R.id.noteUpdate)

        etTitle.setText(args.transaction.title)
        etAmount.setText((args.transaction.amount?: 0).toString())
        etNote.setText(args.transaction.note)

        val type: Int = args.transaction.type?: 0
        val transactionID = args.transaction.transactionID //store transaction id

        //set text to auto complete text view category:
        val categoryOld = args.transaction.category
        etCategory.setText(categoryOld)

        val listExpense = CategoryOptions.expenseCategory() //getting the arrayList data from CategoryOptions file
        val expenseAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, listExpense)
        etCategory.setAdapter(expenseAdapter)

        if (type == 1) { //expense menu
            etCategory.setAdapter(expenseAdapter) //if expense type selected, the set list expense array in category menu
        }
        if (type == 2){ //Income Menu
            //if expense type selected, the set list income array in category menu :
            val listIncome = CategoryOptions.incomeCategory()
            val incomeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, listIncome)
            etCategory.setAdapter(incomeAdapter)
        }

        //---set text to date edit text and date picker:---
        val date: Long = args.transaction.date?: 0L
        val cal = Calendar.getInstance()
        val getDate = Date(date) //convert millis to date format
        cal.time = getDate

        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        val resultParse = simpleDateFormat.format(getDate)
        etDate.setText(resultParse)

        var dateUpdate: Long = args.transaction.date?: 0L //initialized current date value on db
        var invertedDate: Long = dateUpdate * -1
        etDate.setOnClickListener {
            val year = cal.get(Calendar.YEAR) //set default year in datePickerDialog similar with database data
            val month = cal.get(Calendar.MONTH)
            val day = cal.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(requireContext(),
                { _, selectedYear, selectedMonth, selectedDayOfMonth ->

                    val selectedDate = "$selectedDayOfMonth/${selectedMonth + 1}/$selectedYear"
                    etDate.text = null
                    etDate.hint = selectedDate

                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                    val theDate = sdf.parse(selectedDate)
                    dateUpdate = theDate!!.time //convert date to millisecond
                    invertedDate = dateUpdate * -1
                },
                year,
                month,
                day
            )
            dpd.show()
        }

        mDialog.setTitle("Updating $title's Transaction")

        val alertDialog = mDialog.create()
        alertDialog.show()

        btnUpdateData.onClick {
            updateTransactionData(
                transactionID.toString(),
                type,
                etTitle.text.toString(),
                etCategory.text.toString(),
                etAmount.text.toString().toDouble(),
                dateUpdate,
                etNote.text.toString(),
                invertedDate
            )
            showMessage("Transaction Data Updated")

            //setting updated data to textviews :
            tvTitleDetails.text = etTitle.text.toString()
            tvAmountDetails.text = etAmount.text.toString()
            tvNoteDetails.text = etNote.text.toString()
            tvCategoryDetails.text = etCategory.text.toString()
            //tvDateDetails.text = etDate.hint.toString()

            val date: Long = dateUpdate
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val result = Date(date)
            tvDateDetails.text = simpleDateFormat.format(result)

            alertDialog.dismiss()
        }
    }

    private fun updateTransactionData(transactionID:String, type: Int, title: String, category: String, amount: Double, date: Long, note: String, invertedDate: Long
    ){
        // --Initialize Firebase Auth and firebase database--
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid
        if (uid != null) {
            val dbRef = FirebaseDatabase.getInstance().getReference(uid) //initialize database with uid as the parent
            val transactionInfo = TransactionModel(transactionID, type, title, category, amount, date, note, invertedDate)
            dbRef.child(transactionID).setValue(transactionInfo)
        }
    }
}