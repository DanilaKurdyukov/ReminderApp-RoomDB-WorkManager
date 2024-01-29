package com.example.notificationapp.fragments

import android.opengl.Visibility
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.notificationapp.R
import com.example.notificationapp.adapters.ReminderAdapter
import com.example.notificationapp.data.Database
import com.example.notificationapp.models.Reminder
import com.google.android.material.floatingactionbutton.FloatingActionButton

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ReminderFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReminderFragment : Fragment() {


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
        return inflater.inflate(R.layout.fragment_reminder, container, false)
    }

    private lateinit var btnAdd: FloatingActionButton
    private lateinit var remindersRecycler: RecyclerView
    private var reminders = arrayListOf<Reminder>()
    private lateinit var reminderAdapter: ReminderAdapter

    private val reminderDao by lazy { Database.getDb(requireActivity()).reminderDao() }

    private lateinit var selected: Reminder
    private var itemPosition: Int = 0
    private var selectedCount: Int = 0

    private lateinit var navController: NavController

    private lateinit var menuProvider: MenuProvider
    private lateinit var menuHost: MenuHost
    private var isMenuSet = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navHostFragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment?

        navController = navHostFragment!!.navController

        init(view)
    }

    private fun removeMenu() {
        menuHost.removeMenuProvider(menuProvider)
    }

    private fun setMenu() {
        menuHost = requireActivity() as MenuHost

         menuProvider = object: MenuProvider{
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when(menuItem.itemId){
                    R.id.item_delete -> {
                        deleteReminder(selected)
                        menuHost.removeMenuProvider(menuProvider)
                        isMenuSet = false
                        true
                    }
                    R.id.item_edit -> {
                        var args = Bundle()
                        args.putParcelable("editReminder", selected)
                        navController.navigate(R.id.addEditReminderBottomSheetFragment, args)
                        menuHost.removeMenuProvider(menuProvider)
                        isMenuSet = false
                        true
                    }
                    else -> false
                }
            }


        }
        menuHost.addMenuProvider(menuProvider)
    }

    private fun deleteReminder(reminder: Reminder) {
        reminderDao.delete(reminder)
        reminders.removeAt(itemPosition)
        reminderAdapter.notifyItemRemoved(itemPosition)
    }

    private fun init(view: View){
        remindersRecycler = view.findViewById(R.id.recycler_view_reminders)
        btnAdd = view.findViewById(R.id.fab_addReminder)

        btnAdd.setOnClickListener(View.OnClickListener{
            navController.navigate(R.id.addEditReminderBottomSheetFragment)
        })

        getData()
    }

    private fun getData() {
        reminders = reminderDao.get() as ArrayList<Reminder>
        reminderAdapter = ReminderAdapter(requireContext(), reminders, reminderDao)
        reminderAdapter.setOnItemClickListener(object: ReminderAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                    selected = reminders[position]
                    itemPosition = position
                    if (!isMenuSet){
                        setMenu()
                        isMenuSet = true
                    }
                    else {
                        removeMenu()
                        isMenuSet = false
                    }
            }
        })
        remindersRecycler.adapter = reminderAdapter
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ReminderFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ReminderFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}