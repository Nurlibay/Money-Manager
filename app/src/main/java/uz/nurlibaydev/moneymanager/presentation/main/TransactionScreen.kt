package uz.nurlibaydev.moneymanager.presentation.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import by.kirich1409.viewbindingdelegate.viewBinding
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import uz.nurlibaydev.moneymanager.R
import uz.nurlibaydev.moneymanager.data.models.TransactionModel
import uz.nurlibaydev.moneymanager.databinding.ScreenTransactionBinding
import uz.nurlibaydev.moneymanager.utils.extenions.onClick
import java.util.*

class TransactionScreen : Fragment(R.layout.screen_transaction) {

    private lateinit var transactionRecyclerView: RecyclerView
    private lateinit var tvNoData: TextView
    private lateinit var noDataImage: ImageView
    private lateinit var tvNoDataTitle: TextView
    private lateinit var tvVisibilityNoData: TextView
    private lateinit var shimmerLoading: ShimmerFrameLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var transactionList: ArrayList<TransactionModel>
    private lateinit var exportButton: ImageButton
    private lateinit var dbRef: DatabaseReference
    private val user = FirebaseAuth.getInstance().currentUser
    private lateinit var typeOption: Spinner
    private lateinit var timeSpanOption: Spinner
    private var selectedType: String = "All Type"//default is all type
    private var selectedTimeSpan: String = "All Time" //default is all time
    var dateStart: Long = 0
    var dateEnd: Long = 0

    private val binding: ScreenTransactionBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeItems()
        showUserName()
        exportButtonClicked()
        visibilityOptions()

        binding.fab.onClick {
            findNavController().navigate(TransactionScreenDirections.actionTransactionScreenToInsertionScreen())
        }

        transactionRecyclerView = view.findViewById(R.id.rvTransaction)
        transactionRecyclerView.layoutManager = LinearLayoutManager(this.activity)
        transactionRecyclerView.setHasFixedSize(true)

        transactionList = arrayListOf()

        getTransactionData()

        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh)
        swipeRefreshLayout.setOnRefreshListener { //call getTransaction() back to refresh the recyclerview
            getTransactionData()
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun exportButtonClicked() {
        exportButton.onClick {
            //findNavController().navigate(TransactionScreenDirections.actionTransactionScreenToExportDataScreen())
        }
    }

    private fun initializeItems() {
        tvNoData = requireView().findViewById(R.id.tvNoData)
        noDataImage = requireView().findViewById(R.id.noDataImage)
        tvNoDataTitle = requireView().findViewById(R.id.tvNoDataTitle)
        tvVisibilityNoData = requireView().findViewById(R.id.visibilityNoData)
        shimmerLoading = requireView().findViewById(R.id.shimmerFrameLayout)
        exportButton = requireView().findViewById(R.id.exportButton)
    }

    @SuppressLint("SetTextI18n")
    private fun showUserName() {
        user?.reload()
        val tvUserName: TextView = requireView().findViewById(R.id.userNameTV)
        val email = user!!.email
        val userName = user.displayName
        val name = if (userName == null || userName == "") {
            val splitValue = email?.split("@")
            splitValue?.get(0).toString()
        } else {
            userName
        }
        tvUserName.text = "Hi, ${name}!"
    }

    private fun visibilityOptions() {
        typeOption = requireView().findViewById(R.id.typeSpinner) as Spinner
        val typeList = arrayOf("All Type", "Expense", "Income")
        val typeSpinnerAdapter = ArrayAdapter(this.requireActivity(), R.layout.selected_spinner, typeList)
        typeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        typeOption.adapter = typeSpinnerAdapter

        timeSpanOption = requireView().findViewById(R.id.timeSpanSpinner) as Spinner
        val timeSpanList = arrayOf("All Time", "This Month", "This Week", "Today")
        val timeSpanAdapter =
            ArrayAdapter(this.requireActivity(), R.layout.selected_spinner, timeSpanList)
        timeSpanAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        timeSpanOption.adapter = timeSpanAdapter

        typeOption.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                when (typeList[p2]) {
                    "All Type" -> selectedType = "All Type"
                    "Expense" -> selectedType = "Expense"
                    "Income" -> selectedType = "Income"
                }
                getTransactionData()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        timeSpanOption.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                when (timeSpanList[p2]) {
                    "All Time" -> selectedTimeSpan = "All Time"
                    "This Month" -> {
                        selectedTimeSpan = "This Month"
                        getRangeDate(Calendar.DAY_OF_MONTH)
                    }
                    "This Week" -> {
                        selectedTimeSpan = "This Week"
                        getRangeDate(Calendar.DAY_OF_WEEK)
                    }
                    "Today" -> {
                        selectedTimeSpan = "Today"
                        dateStart = System.currentTimeMillis()
                        dateEnd = System.currentTimeMillis()
                    }
                }
                getTransactionData()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    private fun getRangeDate(rangeType: Int) {
        val currentDate = Date()
        val cal: Calendar = Calendar.getInstance(TimeZone.getDefault())
        cal.time = currentDate

        val startDay = cal.getActualMinimum(rangeType) //get the first date of the month
        cal.set(rangeType, startDay)
        val startDate = cal.time
        dateStart = startDate.time //convert to millis

        val endDay = cal.getActualMaximum(rangeType) //get the last date of the month
        cal.set(rangeType, endDay)
        val endDate = cal.time
        dateEnd = endDate.time //convert to millis
    }

    private fun getTransactionData() {
        shimmerLoading.startShimmer()
        shimmerLoading.visibility = View.VISIBLE
        tvVisibilityNoData.visibility = View.GONE
        transactionRecyclerView.visibility = View.GONE //hide the recycler view
        tvNoData.visibility = View.GONE
        noDataImage.visibility = View.GONE
        tvNoDataTitle.visibility = View.GONE

        val uid = user?.uid //get user id from database
        if (uid != null) {
            dbRef = FirebaseDatabase.getInstance().getReference(uid)
        }
        val query: Query = dbRef.orderByChild("invertedDate") //sorting date descending
        query.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                transactionList.clear()
                if (snapshot.exists()) {
                    when (selectedType) {
                        "All Type" -> { //all option selected
                            for (transactionSnap in snapshot.children) {
                                val transactionData =
                                    transactionSnap.getValue(TransactionModel::class.java) //reference data class
                                if (selectedTimeSpan == "All Time") {
                                    transactionList.add(transactionData!!)
                                } else {
                                    if (transactionData!!.date!! > dateStart - 86400000 &&
                                        transactionData.date!! <= dateEnd
                                    ) {
                                        transactionList.add(transactionData)
                                    }
                                }
                            }
                        }
                        "Expense" -> { //expense option selected
                            for (transactionSnap in snapshot.children) {
                                val transactionData =
                                    transactionSnap.getValue(TransactionModel::class.java) //reference data class
                                if (transactionData!!.type == 1) { //expense type
                                    if (selectedTimeSpan == "All Time") {
                                        transactionList.add(transactionData)
                                    } else {
                                        if (transactionData.date!! > dateStart - 86400000 &&
                                            transactionData.date!! <= dateEnd
                                        ) {
                                            transactionList.add(transactionData)
                                        }
                                    }
                                }
                            }
                        }
                        "Income" -> {
                            for (transactionSnap in snapshot.children) {
                                val transactionData =
                                    transactionSnap.getValue(TransactionModel::class.java) //reference data class
                                if (transactionData!!.type == 2) { //income type
                                    if (selectedTimeSpan == "All Time") {
                                        transactionList.add(transactionData)
                                    } else {
                                        if (transactionData.date!! > dateStart - 86400000 &&
                                            transactionData.date!! <= dateEnd
                                        ) {
                                            transactionList.add(transactionData)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (transactionList.isEmpty()) { //if there is no data being displayed
                        noDataImage.visibility = View.VISIBLE
                        tvNoDataTitle.visibility = View.VISIBLE
                        tvVisibilityNoData.visibility = View.VISIBLE
                        tvVisibilityNoData.text = "There is no $selectedType data $selectedTimeSpan"
                    } else {
                        val mAdapter = TransactionAdapter(transactionList)
                        transactionRecyclerView.adapter = mAdapter

                        mAdapter.setOnItemClickListener(object :
                            TransactionAdapter.onItemClickListener { //item click listener and pass extra data
                            override fun onItemClick(position: Int) {
                                val transaction = TransactionModel(
                                    transactionList[position].transactionID,
                                    transactionList[position].type,
                                    transactionList[position].title,
                                    transactionList[position].category,
                                    transactionList[position].amount,
                                    transactionList[position].date,
                                    transactionList[position].note,
                                    transactionList[position].invertedDate,
                                )
                                findNavController().navigate(TransactionScreenDirections.actionTransactionScreenToTransactionDetailsScreen(transaction))
                            }
                        })
                        transactionRecyclerView.visibility = View.VISIBLE
                    }
                    shimmerLoading.stopShimmer()
                    shimmerLoading.visibility = View.GONE
                } else { //if there is no data in database
                    shimmerLoading.stopShimmer()
                    shimmerLoading.visibility = View.GONE

                    noDataImage.visibility = View.VISIBLE
                    tvNoDataTitle.visibility = View.VISIBLE
                    tvNoData.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                print("Listener was cancelled")
            }
        })
    }

    override fun onResume() {
        super.onResume()
        getTransactionData()
    }
}