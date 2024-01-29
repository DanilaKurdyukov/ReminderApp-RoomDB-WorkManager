package com.example.notificationapp.adapters

import android.content.Context
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.notificationapp.R
import com.example.notificationapp.data.ReminderDAO
import com.example.notificationapp.models.Reminder
import com.example.notificationapp.notification.NotificationWorker
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textview.MaterialTextView
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.TimeUnit


class ReminderAdapter(var context: Context, var reminders: ArrayList<Reminder>, var reminderDao: ReminderDAO) : RecyclerView.Adapter<ReminderAdapter.ViewHolder>(){

    private lateinit var mListener: onItemClickListener
    private val selectedItems = SparseBooleanArray()
    private var selectedPosition = -1

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(mListener: onItemClickListener){
        this.mListener = mListener
    }

    inner class ViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView){
        val txtReminderTitle = itemView.findViewById<MaterialTextView>(R.id.text_view_reminderTitle)
        val txtReminderDate = itemView.findViewById<MaterialTextView>(R.id.text_view_reminderDate)
        val txtReminderTime = itemView.findViewById<MaterialTextView>(R.id.text_view_reminderTime)
        val txtReminderDescription = itemView.findViewById<MaterialTextView>(R.id.text_view_reminderDescription)
        val switchIsEnable = itemView.findViewById<SwitchMaterial>(R.id.switch_isEnable)

        init{
            itemView.setOnClickListener() {
                if (selectedItems!=null){
                    if (selectedItems.get(adapterPosition,false)){
                        selectedItems.delete(adapterPosition);
                        it.isSelected = false;
                    }
                    else{
                        if(selectedItems.size()<1){
                            selectedItems.put(adapterPosition,true);
                            it.isSelected = true;
                        }
                    }
                }
                listener.onItemClick(adapterPosition)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.reminder_item, parent, false),mListener)
    }

    override fun getItemCount(): Int {
        return reminders.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val current = reminders[position]

        if (current.isReminderEnable){
            holder.txtReminderDate.setTextColor(context.getColor(R.color.white))
            holder.txtReminderDescription.setTextColor(context.getColor(R.color.white))
            holder.txtReminderTitle.setTextColor(context.getColor(R.color.white))
            holder.txtReminderTime.setTextColor(context.getColor(R.color.white))
            startWork(current)
        } else {
            holder.txtReminderDate.setTextColor(context.getColor(R.color.grey))
            holder.txtReminderDescription.setTextColor(context.getColor(R.color.grey))
            holder.txtReminderTitle.setTextColor(context.getColor(R.color.grey))
            holder.txtReminderTime.setTextColor(context.getColor(R.color.grey))
        }

        holder.txtReminderDescription.text = current.description
        val reminderDate = current.reminderDateTime.toString().split('T')[0]
        val reminderTime = current.reminderDateTime.toString().split('T')[1].substring(0,5)

        holder.txtReminderTitle.text = "${current.title}"

        holder.txtReminderDate.text = "$reminderDate"

        holder.txtReminderTime.text = "$reminderTime"

        holder.switchIsEnable.isChecked = current.isReminderEnable

        holder.switchIsEnable.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                holder.txtReminderDate.setTextColor(context.getColor(R.color.white))
                holder.txtReminderDescription.setTextColor(context.getColor(R.color.white))
                holder.txtReminderTitle.setTextColor(context.getColor(R.color.white))
                holder.txtReminderTime.setTextColor(context.getColor(R.color.white))
                startWork(current)
                Toast.makeText(context,"Напоминание запущено!", Toast.LENGTH_SHORT).show()

            } else {
                holder.txtReminderDate.setTextColor(context.getColor(R.color.grey))
                holder.txtReminderDescription.setTextColor(context.getColor(R.color.grey))
                holder.txtReminderTitle.setTextColor(context.getColor(R.color.grey))
                holder.txtReminderTime.setTextColor(context.getColor(R.color.grey))
                cancelWork(current)
            }
            updateReminder(current.id, current.title, current.description, current.reminderDateTime, isChecked, position)
        }
    }

    private fun cancelWork(reminder: Reminder){
        WorkManager.getInstance(context).cancelUniqueWork("${reminder.title}_${reminder.reminderDateTime}")
        Toast.makeText(context,"Напоминание остановлено!", Toast.LENGTH_SHORT).show()
    }

    private fun startWork(reminder: Reminder){
        var userSelectedDateTime = Calendar.getInstance()
        val date = reminder.reminderDateTime
        val todayDateTime = Calendar.getInstance()
        userSelectedDateTime.set(date.year, date.monthValue-1,date.dayOfMonth, date.hour, date.minute)
        val delayInSeconds = (userSelectedDateTime.timeInMillis/1000L) - (todayDateTime.timeInMillis/1000L)
        createWorkRequest(delayInSeconds,reminder.title,reminder.description, reminder.id,"${reminder.title}_${reminder.reminderDateTime}")
    }

    private fun createWorkRequest(delayInSeconds: Long, contentTitle: String, contentText: String, notificationId: Int, workName: String){
        val notificationWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delayInSeconds, TimeUnit.SECONDS)
            .setInputData(workDataOf("title" to contentTitle, "text" to contentText, "id" to notificationId))
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(workName,
            ExistingWorkPolicy.REPLACE,notificationWorkRequest)
    }

    private fun updateReminder(id: Int, title: String, description: String,
                               reminderDateTime: OffsetDateTime, isReminderEnable: Boolean, changedItemPosition: Int) {
        val reminder = Reminder(id, title, description, reminderDateTime, isReminderEnable)
        reminderDao.update(reminder)
    }
}