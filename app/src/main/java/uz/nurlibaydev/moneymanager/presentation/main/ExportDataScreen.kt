//package uz.nurlibaydev.moneymanager.presentation.main
//
//import android.Manifest
//import android.content.DialogInterface
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.net.Uri
//import android.os.Bundle
//import android.os.Environment
//import android.provider.Settings
//import android.text.style.DynamicDrawableSpan.ALIGN_CENTER
//import android.view.View
//import uz.nurlibaydev.moneymanager.R
//import android.widget.EditText
//import android.widget.Toast
//import androidx.appcompat.app.AlertDialog
//import androidx.core.app.ActivityCompat
//import androidx.core.util.Pair
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//import by.kirich1409.viewbindingdelegate.viewBinding
//import com.google.android.material.datepicker.MaterialDatePicker
//import com.google.android.material.snackbar.Snackbar
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.*
//import timber.log.Timber
//import uz.nurlibaydev.moneymanager.data.models.TransactionModel
//import java.io.File
//import java.io.FileOutputStream
//import java.io.IOException
//import java.text.SimpleDateFormat
//import java.util.*
//import org.apache.poi.hssf.usermodel.HSSFCellStyle
//import org.apache.poi.hssf.usermodel.HSSFWorkbook
//import org.apache.poi.hssf.util.HSSFColor
//import org.apache.poi.ss.usermodel.*
//import uz.nurlibaydev.moneymanager.databinding.ScreenExportDataBinding
//import uz.nurlibaydev.moneymanager.utils.extenions.onClick
//
//class ExportDataScreen : Fragment(R.layout.screen_export_data) {
//
//    private var dateStart: Long = 0
//    private var dateEnd: Long = 0
//
//    private var TAG: String = "ExcelUtil"
//    private lateinit var cell: Cell
//    private lateinit var workbook: Workbook
//    private lateinit var sheet: Sheet
//    private lateinit var headerCellStyle: CellStyle
//
//    // Initialize Firebase Auth and database
//    private var user = FirebaseAuth.getInstance().currentUser
//    private val uid = user?.uid //get user id from database
//    private var dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference(uid!!)
//
//    private val tagPermission: String = ExportDataScreen::class.java.simpleName
//    private val PERMISSIONS = arrayOf(
//        Manifest.permission.READ_EXTERNAL_STORAGE,
//        Manifest.permission.WRITE_EXTERNAL_STORAGE
//    )
//
//    private val binding: ScreenExportDataBinding by viewBinding()
//
//    private lateinit var fileName: String
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        backButtonClicked()
//
//        setInitDateRange()
//
//        dateRangePicker()
//
//        //Export transaction data to excel file : https://medium.com/geekculture/creating-an-excel-in-android-cd9c22198619
//        binding.exportBtn.onClick{
//            fileName = "Catat_Uang_" + convertDateFileName(dateStart, dateEnd) + ".xls"
//            val isPermissionGranted = checkPermissionsAtRuntime()
//            if (isPermissionGranted){
//                exportDataIntoWorkbook()
//            }else{
//                inflateAlertDialog()
//                Snackbar.make(it, "Permission is not granted", Snackbar.LENGTH_LONG).show()
//            }
//        }
//    }
//
//    private fun exportDataIntoWorkbook() {
//
//        // Check if available and not read only
//        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
//            Timber.tag(TAG).e("Storage not available or read only")
//
//        }
//
//        //Creating a new HSSF Workbook (.xls format)
//        workbook = HSSFWorkbook()
//
//        setHeaderCellStyle()
//
//        // Creating a New Sheet and Setting width for each column
//        sheet = workbook.createSheet("Transactions")
//        sheet.setColumnWidth(0, (15 * 230))
//        sheet.setColumnWidth(1, (15 * 230))
//        sheet.setColumnWidth(2, (15 * 400))
//        sheet.setColumnWidth(3, (15 * 400))
//        sheet.setColumnWidth(4, (15 * 400))
//        sheet.setColumnWidth(5, (15 * 500))
//
//        setHeaderRow()
//        fillDataIntoExcel()
//    }
//
//    private fun storeExcelInStorage() {
//        var isExcelGenerated: Boolean
//        val file = File(
//            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
//        var fileOutputStream: FileOutputStream? = null
//
//        try {
//            fileOutputStream = FileOutputStream(file)
//            workbook.write(fileOutputStream)
//            Timber.tag(TAG).e("Writing file$file")
//            isExcelGenerated = true
//        } catch (e: IOException) {
//            Timber.tag(TAG).e(e, "Error writing Exception: ")
//            isExcelGenerated = false
//        } catch (e: Exception) {
//            Timber.tag(TAG).e(e, "Failed to save file due to Exception: ")
//            isExcelGenerated = false
//        } finally {
//            try {
//                fileOutputStream?.close()
//            } catch (ex: Exception) {
//                ex.printStackTrace()
//            }
//        }
//
//        if (isExcelGenerated){
//            Snackbar.make(requireActivity().findViewById(android.R.id.content), "Excel file Exported Successfully into Downloads Folder", Snackbar.LENGTH_LONG).show()
//        }else{
//            Toast.makeText(requireContext(), "Export failed!", Toast.LENGTH_LONG).show()
//        }
//    }
//
//    private fun fillDataIntoExcel() {
//        val transactionList: ArrayList<TransactionModel> = arrayListOf()
//
//        dbRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                transactionList.clear()
//                if (snapshot.exists()) {
//                    for (transactionSnap in snapshot.children) {
//                        val transactionData = transactionSnap.getValue(TransactionModel::class.java) //reference data class
//                        if (transactionData!!.date!! > dateStart-86400000 &&
//                            transactionData.date!!<= dateEnd){
//                            transactionList.add(transactionData)
//                        }
//                    }
//
//                    if (transactionList.isEmpty()){ //if there is no data in the selected time range
//                        Snackbar.make(requireActivity().findViewById(android.R.id.content), "There is no transaction data in the " + convertDate(dateStart, dateEnd) + " date range.", Snackbar.LENGTH_LONG).show()
//                    }else{
//                        for ((i) in transactionList.withIndex()){
//                            // Create a New Row for every new entry in list
//                            val rowData: Row = sheet.createRow(i+1)
//
//                            // Create Cells for each row
//                            cell = rowData.createCell(0)
//                            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
//                            val result = Date(transactionList[i].date!!)
//                            cell.setCellValue(simpleDateFormat.format(result))
//
//                            cell = rowData.createCell(1)
//                            if (transactionList[i].type == 1){
//                                cell.setCellValue("Expense")
//                            }else{
//                                cell.setCellValue("Income")
//                            }
//
//                            cell = rowData.createCell(2)
//                            cell.setCellValue(transactionList[i].amount.toString())
//
//                            cell = rowData.createCell(3)
//                            cell.setCellValue(transactionList[i].title)
//
//                            cell = rowData.createCell(4)
//                            cell.setCellValue(transactionList[i].category)
//
//                            cell = rowData.createCell(5)
//                            cell.setCellValue(transactionList[i].note)
//                        }
//                        storeExcelInStorage()
//                    }
//                }else{
//                    Snackbar.make(requireActivity().findViewById(android.R.id.content), "There is no transaction data, you can add transaction first", Snackbar.LENGTH_LONG).show()
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//
//            }
//        })
//    }
//
//    private fun isExternalStorageAvailable(): Boolean {
//        val externalStorageState: String = Environment.getExternalStorageState()
//        return Environment.MEDIA_MOUNTED == externalStorageState
//    }
//
//    private fun isExternalStorageReadOnly(): Boolean {
//        val externalStorageState = Environment.getExternalStorageState()
//        return Environment.MEDIA_MOUNTED_READ_ONLY == externalStorageState
//    }
//
//    private fun setHeaderCellStyle() {
//        headerCellStyle = workbook.createCellStyle()
//        headerCellStyle.fillForegroundColor = HSSFColor.ORANGE.index
//        headerCellStyle.fillPattern = HSSFCellStyle.SOLID_FOREGROUND
//        headerCellStyle.alignment = CellStyle.ALIGN_CENTER
//    }
//
//    private fun setHeaderRow() {
//        val headerRow: Row = sheet.createRow(0)
//
//        cell = headerRow.createCell(0)
//        cell.setCellValue("Date")
//        cell.cellStyle = headerCellStyle
//
//        cell = headerRow.createCell(1)
//        cell.setCellValue("Type")
//        cell.cellStyle = headerCellStyle
//
//        cell = headerRow.createCell(2)
//        cell.setCellValue("Amount")
//        cell.cellStyle = headerCellStyle
//
//        cell = headerRow.createCell(3)
//        cell.setCellValue("Title")
//        cell.cellStyle = headerCellStyle
//
//        cell = headerRow.createCell(4)
//        cell.setCellValue("Category")
//        cell.cellStyle = headerCellStyle
//
//        cell = headerRow.createCell(5)
//        cell.setCellValue("Note")
//        cell.cellStyle = headerCellStyle
//    }
//
//    private fun setInitDateRange() {
//        val dateRangeEt: EditText = binding.dateRangeEt
//
//        val currentDate = Date()
//        val cal: Calendar = Calendar.getInstance(TimeZone.getDefault())
//        cal.time = currentDate
//
//        val startDay = cal.getActualMinimum(Calendar.DAY_OF_MONTH) //get the first date of the month
//        cal.set(Calendar.DAY_OF_MONTH, startDay)
//        val startDate = cal.time
//        dateStart= startDate.time //convert to millis
//
//        val endDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH) //get the last date of the month
//        cal.set(Calendar.DAY_OF_MONTH, endDay)
//        val endDate = cal.time
//        dateEnd= endDate.time //convert to millis
//
//        dateRangeEt.hint = "This Month"
//    }
//
//    private fun dateRangePicker() {
//        val dateRangeEt: EditText = binding.dateRangeEt
//
//        dateRangeEt.setOnClickListener {
//            // Opens the date range picker with the range of the first day of
//            // the month to today selected.
//            val datePicker = MaterialDatePicker.Builder.dateRangePicker()
//                .setTitleText("Select Date")
//                .setSelection(
//                    Pair(
//                        dateStart,
//                        dateEnd
//                    )
//                ).build()
//            datePicker.show(childFragmentManager, "DatePicker")
//
//            // Setting up the event for when ok is clicked
//            datePicker.addOnPositiveButtonClickListener {
//                //convert the result from string to long type :
//                val dateString = datePicker.selection.toString()
//                val date: String = dateString.filter { it.isDigit() } //only takes digit value
//                //divide the start and end date value :
//                dateStart = date.substring(0,13).toLong()
//                dateEnd  = date.substring(13).toLong()
//                dateRangeEt.hint = convertDate(dateStart, dateEnd) //call function to convert millis to string
//            }
//        }
//    }
//
//    private fun convertDate(dateStart: Long, dateEnd: Long): String {
//        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
//        val date1 = Date(dateStart)
//        val date2 = Date(dateEnd)
//        val result1 = simpleDateFormat.format(date1)
//        val result2 = simpleDateFormat.format(date2)
//        return "$result1 - $result2"
//    }
//
//    private fun convertDateFileName(dateStart: Long, dateEnd: Long): String {
//        val simpleDateFormat = SimpleDateFormat("ddMMyyyy", Locale.ENGLISH)
//        val date1 = Date(dateStart)
//        val date2 = Date(dateEnd)
//        val result1 = simpleDateFormat.format(date1)
//        val result2 = simpleDateFormat.format(date2)
//        return "${result1}_$result2"
//    }
//
//    private fun backButtonClicked() {
//        binding.backBtn.onClick {
//            findNavController().popBackStack()
//        }
//    }
//
//    private fun requestPermissions() {
//        Timber.tag(tagPermission).e("requestPermissions: ")
//        ActivityCompat.requestPermissions(requireActivity(), PERMISSIONS, 25)
//    }
//
//    private fun checkPermissionsAtRuntime(): Boolean {
//        Timber.tag(tagPermission).e("checkPermissionsAtRuntime: ")
//        for (permission in PERMISSIONS) {
//            if (ActivityCompat.checkSelfPermission(requireContext(), permission)
//                != PackageManager.PERMISSION_GRANTED
//            ) {
//                return false
//            }
//        }
//        return true
//    }
//
//    /**
//     * Method: Show Alert Dialog when User denies permission permanently
//     */
//    private fun inflateAlertDialog() {
//        // Inflate Alert Dialog
//        AlertDialog.Builder(requireContext())
//            .setTitle("Permissions Mandatory")
//            .setMessage("Kindly enable all permissions through Settings")
//            .setPositiveButton("OKAY") { dialogInterface: DialogInterface, _: Int ->
//                launchAppSettings()
//                dialogInterface.dismiss()
//            }
//            .setCancelable(false)
//            .show()
//    }
//
//    /**
//     * Method: Launch App-Settings Screen
//     */
//    private fun launchAppSettings() {
//        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//        val uri = Uri.fromParts("package", requireActivity().packageName, null)
//        intent.data = uri
//        startActivityForResult(intent, 20)
//    }
//
//    override fun onStart() {
//        super.onStart()
//        requestPermissions()
//    }
//}