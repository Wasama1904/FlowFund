package com.flowfund.app.ui.transactions

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.flowfund.app.R
import com.flowfund.app.data.entities.Category
import com.flowfund.app.data.entities.Transaction
import com.flowfund.app.utils.DateUtils
import com.flowfund.app.utils.ViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import java.io.File
import java.util.*

class AddEditTransactionFragment : DialogFragment() {

    private lateinit var viewModel: TransactionViewModel
    private var existingId: Long? = null
    private var existingTx: Transaction? = null
    private var selectedDate = System.currentTimeMillis()
    private var startTime: Long? = null
    private var endTime: Long? = null
    private var photoPath: String? = null
    private var categories: List<Category> = emptyList()
    private var cameraUri: Uri? = null

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { photoPath = it.toString(); updatePhotoPreview() }
    }
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) { photoPath = cameraUri?.toString(); updatePhotoPreview() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_FlowFund_FullScreenDialog)
        existingId = arguments?.getLong(ARG_TX_ID, 0L)?.takeIf { it > 0 }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_add_edit_transaction, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireParentFragment(), ViewModelFactory(requireContext()))[TransactionViewModel::class.java]

        val etAmount      = view.findViewById<EditText>(R.id.etAmount)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val rgType        = view.findViewById<RadioGroup>(R.id.rgType)
        val spinnerCat    = view.findViewById<Spinner>(R.id.spinnerCategory)
        val btnDate       = view.findViewById<Button>(R.id.btnDate)
        val btnStartTime  = view.findViewById<Button>(R.id.btnStartTime)
        val btnEndTime    = view.findViewById<Button>(R.id.btnEndTime)
        val btnPhoto      = view.findViewById<Button>(R.id.btnPhoto)
        val btnCamera     = view.findViewById<Button>(R.id.btnCamera)
        val ivPreview     = view.findViewById<ImageView>(R.id.ivPhotoPreview)
        val btnSave       = view.findViewById<Button>(R.id.btnSave)
        val btnCancel     = view.findViewById<Button>(R.id.btnCancel)
        val tvTitle       = view.findViewById<TextView>(R.id.tvTitle)

        tvTitle.text = if (existingId != null) "Edit Transaction" else "Add Transaction"

        // Load categories into spinner
        viewModel.categories.observe(viewLifecycleOwner) { cats ->
            categories = cats
            val names = listOf("No Category") + cats.map { it.name }
            spinnerCat.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, names)
        }

        // Load existing transaction
        if (existingId != null) {
            lifecycleScope.launch {
                existingTx = viewModel.getTransactionById(existingId!!)
                existingTx?.let { tx ->
                    etAmount.setText(tx.amount.toString())
                    etDescription.setText(tx.description)
                    if (tx.type == "INCOME") rgType.check(R.id.rbIncome) else rgType.check(R.id.rbExpense)
                    selectedDate = tx.date
                    startTime    = tx.startTime
                    endTime      = tx.endTime
                    photoPath    = tx.photoPath
                    updateDateBtn(btnDate)
                    updateTimeBtn(btnStartTime, startTime, "Start Time")
                    updateTimeBtn(btnEndTime, endTime, "End Time")
                    updatePhotoPreview(ivPreview)
                    val catIdx = categories.indexOfFirst { it.id == tx.categoryId }
                    if (catIdx >= 0) spinnerCat.setSelection(catIdx + 1)
                }
            }
        }

        btnDate.setOnClickListener {
            val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
            DatePickerDialog(requireContext(), { _, y, m, d ->
                cal.set(y, m, d); selectedDate = cal.timeInMillis; updateDateBtn(btnDate)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnStartTime.setOnClickListener { pickTime("Start Time", btnStartTime) { t -> startTime = t } }
        btnEndTime.setOnClickListener   { pickTime("End Time",   btnEndTime)   { t -> endTime  = t } }

        btnPhoto.setOnClickListener  { galleryLauncher.launch("image/*") }
        btnCamera.setOnClickListener { launchCamera() }

        btnCancel.setOnClickListener { dismiss() }

        btnSave.setOnClickListener {
            val amtStr = etAmount.text.toString()
            if (amtStr.isBlank()) { Toast.makeText(requireContext(), "Enter amount", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            val amount = amtStr.toDoubleOrNull() ?: run { Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            val type = if (rgType.checkedRadioButtonId == R.id.rbIncome) "INCOME" else "EXPENSE"
            val catId = if (spinnerCat.selectedItemPosition == 0) null else categories[spinnerCat.selectedItemPosition - 1].id
            viewModel.saveTransaction(existingId, catId, amount, type, etDescription.text.toString(), selectedDate, startTime, endTime, photoPath)
        }

        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            if (result == null) return@observe
            // Immediately clear so reopening the dialog doesn't re-fire the old result
            viewModel.saveResult.value = null
            if (result.isSuccess) dismiss()
            else Toast.makeText(requireContext(), result.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_SHORT).show()
        }

        updateDateBtn(btnDate)
    }

    private fun pickTime(label: String, btn: Button, onSet: (Long) -> Unit) {
        val cal = Calendar.getInstance()
        TimePickerDialog(requireContext(), { _, h, m ->
            cal.set(Calendar.HOUR_OF_DAY, h); cal.set(Calendar.MINUTE, m)
            onSet(cal.timeInMillis)
            btn.text = "$label: ${DateUtils.formatTime(cal.timeInMillis)}"
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
    }

    private fun updateDateBtn(btn: Button) { btn.text = "Date: ${DateUtils.format(selectedDate)}" }

    private fun updateTimeBtn(btn: Button, time: Long?, label: String) {
        btn.text = if (time != null) "$label: ${DateUtils.formatTime(time)}" else label
    }

    private fun updatePhotoPreview(iv: ImageView? = view?.findViewById(R.id.ivPhotoPreview)) {
        iv ?: return
        if (photoPath != null) {
            iv.visibility = View.VISIBLE
            Glide.with(this).load(Uri.parse(photoPath)).centerCrop().into(iv)
        } else iv.visibility = View.GONE
    }

    private fun launchCamera() {
        val file = File(requireContext().filesDir, "photo_${System.currentTimeMillis()}.jpg")
        cameraUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
        cameraLauncher.launch(cameraUri!!)
    }

    companion object {
        private const val ARG_TX_ID = "tx_id"
        fun newInstance(txId: Long?) = AddEditTransactionFragment().apply {
            arguments = Bundle().apply { if (txId != null) putLong(ARG_TX_ID, txId) }
        }
    }
}
