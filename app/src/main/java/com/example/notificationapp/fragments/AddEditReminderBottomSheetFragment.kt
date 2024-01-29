package com.example.notificationapp.fragments

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.notificationapp.R
import com.example.notificationapp.data.Database
import com.example.notificationapp.models.Reminder
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddEditReminderBottomSheetFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddEditReminderBottomSheetFragment : BottomSheetDialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_reminder_bottom_sheet, container, false)
    }

    private lateinit var txtTitle: TextInputEditText
    private lateinit var txtDescription: TextInputEditText
    private lateinit var btnSave: FloatingActionButton
    private lateinit var btnReminderDate: ExtendedFloatingActionButton
    private lateinit var btnReminderTime: ExtendedFloatingActionButton
    private lateinit var switchEnable: SwitchMaterial

    private lateinit var timePicker: MaterialTimePicker
    private val reminderDao by lazy { Database.getDb(requireActivity()).reminderDao() }

    private lateinit var navController: NavController

    private var edit: Reminder? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)

        getNavHostFragment()
    }

    private fun init(view: View){
        txtTitle = view.findViewById(R.id.edit_text_reminderTitle)
        txtDescription = view.findViewById(R.id.edit_text_reminderDescription)
        btnSave = view.findViewById(R.id.fab_save)
        btnReminderDate = view.findViewById(R.id.fab_reminderDate)
        btnReminderTime = view.findViewById(R.id.fab_reminderTime)
        switchEnable = view.findViewById(R.id.switch_enable)

        if(arguments!=null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                edit = arguments?.getParcelable("editReminder", Reminder:: class.java)!!
            }
            else{
                edit = arguments?.getParcelable("editReminder")!!
            }
            txtTitle.setText(edit?.title)
            txtDescription.setText(edit?.description)
            val reminderDateTime = Calendar.getInstance()
            reminderDateTime.set(edit?.reminderDateTime!!.year, edit?.reminderDateTime!!.monthValue-1,
                                    edit?.reminderDateTime!!.dayOfMonth, edit?.reminderDateTime!!.hour, edit?.reminderDateTime!!.minute)
            val dateFormatter = SimpleDateFormat("yyyy-MM-dd")
            val date = dateFormatter.format(Date(reminderDateTime.timeInMillis))
            btnReminderDate.text = date
            if(reminderDateTime.get(Calendar.HOUR) < 10 || reminderDateTime.get(Calendar.MINUTE) < 10 ){
                if (reminderDateTime.get(Calendar.HOUR) < 10 && reminderDateTime.get(Calendar.MINUTE) < 10 )
                    btnReminderTime.text = "0${reminderDateTime.get(Calendar.HOUR)}:0${reminderDateTime.get(Calendar.MINUTE)}"
                else if (reminderDateTime.get(Calendar.HOUR) < 10)
                    btnReminderTime.text = "0${reminderDateTime.get(Calendar.HOUR)}:${reminderDateTime.get(Calendar.MINUTE)}"
                else if (reminderDateTime.get(Calendar.MINUTE) < 10)
                    btnReminderTime.text = "${reminderDateTime.get(Calendar.HOUR)}:0${reminderDateTime.get(Calendar.MINUTE)}"
            } else{
                btnReminderTime.text = "${reminderDateTime.get(Calendar.HOUR)}:${reminderDateTime.get(Calendar.MINUTE)}"
            }
            switchEnable.visibility = View.GONE
        }

        btnReminderDate.setOnClickListener {
            setDate()
        }

        btnReminderTime.setOnClickListener {
            setTime()
        }

        btnSave.setOnClickListener {

                if (!txtTitle.text.isNullOrBlank()&&!txtDescription.text.isNullOrBlank()){
                    if (edit==null){
                        add(txtTitle.text.toString(),txtDescription.text.toString())
                    }
                    else{
                        update()
                    }
                }
        }
    }

    private fun setTime() {
        timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            .setTheme(R.style.TimePickerStyle)
            .build()
        timePicker.show(childFragmentManager,"TimePicker")

        timePicker.addOnPositiveButtonClickListener {
            if(timePicker.hour<10||timePicker.minute<10){
                if (timePicker.hour<10&&timePicker.minute<10)
                    btnReminderTime.text = "0${timePicker.hour}:0${timePicker.minute}"
                else if (timePicker.hour<10)
                    btnReminderTime.text = "0${timePicker.hour}:${timePicker.minute}"
                else if (timePicker.minute<10)
                    btnReminderTime.text = "${timePicker.hour}:0${timePicker.minute}"
            } else{
                btnReminderTime.text = "${timePicker.hour}:${timePicker.minute}"
            }
        }

    }

    private fun setDate() {
        val datePicker = MaterialDatePicker.Builder
            .datePicker()
            .setTheme(R.style.MaterialCalendarTheme)
            .build()
        datePicker.show(childFragmentManager, "DatePicker")
        datePicker.addOnPositiveButtonClickListener {
            val dateFormatter = SimpleDateFormat("yyyy-MM-dd")
            val date = dateFormatter.format(Date(it))
            btnReminderDate.text = date
        }
    }

    private fun add(title: String, description: String) {
        val reminder = Reminder(0,title, description, OffsetDateTime.parse("${btnReminderDate.text}T${btnReminderTime.text}:00.000+03:00"),switchEnable.isChecked)
        reminderDao.add(reminder)
        if(switchEnable.isChecked){
            Toast.makeText(context,"Напоминание запущено!", Toast.LENGTH_SHORT).show()
        }
        navController.navigate(R.id.reminderFragment)
    }

    private fun update(){
        edit?.title = txtTitle.text.toString();
        edit?.description = txtDescription.text.toString();
        edit?.reminderDateTime = OffsetDateTime.parse("${btnReminderDate.text}T${btnReminderTime.text}:00.000+03:00")
        reminderDao.update(edit!!)
        if(switchEnable.isChecked){
            Toast.makeText(context,"Напоминание запущено!", Toast.LENGTH_SHORT).show()
        }
        navController.navigate(R.id.reminderFragment)
    }

    private fun getNavHostFragment() {
         val navHostFragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
         navController = navHostFragment.navController
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddReminderBottomSheetFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddEditReminderBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}