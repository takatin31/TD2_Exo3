package com.example.td2_exo3

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    var dataList = arrayListOf<Intervention>()

    var searchedList = arrayListOf<Intervention>()

    lateinit var adapter: InterventionAdapter
    lateinit var layoutManager : LinearLayoutManager

    private lateinit var interventionDatabase : InterventionRoomDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        interventionDatabase = InterventionRoomDatabase.getDatabase(this)

        save.setOnClickListener {
            val gson = Gson()
            val gsonPretty = GsonBuilder().setPrettyPrinting().create()

            val jsonList = gsonPretty.toJson(dataList)

            writeToFile(jsonList, this)

        }

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        dateSearch.setOnClickListener {
            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                var dayS = "$dayOfMonth"
                var monthS = "${monthOfYear+1}"
                if (dayOfMonth < 10){
                    dayS = "0$dayOfMonth"
                }


                if (monthOfYear < 9){
                    monthS = "0${monthOfYear+1}"
                }

                dateSearch.setText("" + dayS + "/" + monthS + "/" + year)

                searchedList.clear()
                val searchedText = dateSearch.text.toString().toLowerCase()
                for (intervention in dataList){
                    if (intervention.date.toLowerCase().contains(searchedText)){
                        searchedList.add(intervention)
                    }
                }
                adapter.notifyDataSetChanged()
            }, year, month, day)

            dpd.show()
        }

        addIntervention.setOnClickListener {
            addNewIntervention()
        }



        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        adapter = InterventionAdapter(this)
        recyclerView.adapter = adapter



        //initData()

        getData()



    }

    private fun writeToFile(data: String, context: Context) {
        try {
            val outputStreamWriter =
                OutputStreamWriter(context.openFileOutput("file.json", Context.MODE_PRIVATE))
            outputStreamWriter.write(data)
            outputStreamWriter.close()
            Toast.makeText(this, "Saved to $filesDir", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            Log.e("Exception", "File write failed: " + e.toString())
        }
    }


    fun initData(){
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val currentDate = sdf.format(Date())

        val inter1 = Intervention(0, "03/05/2020", 0, 0)
        val inter2 = Intervention(1, currentDate, 1, 1)
        val inter3 = Intervention(2, currentDate, 2, 2)
        val inter4 = Intervention(3, currentDate, 3, 3)
        val inter5 = Intervention(4, currentDate, 4, 2)


        addIntervention(inter1)
        addIntervention(inter2)
        addIntervention(inter3)
        addIntervention(inter4)
        addIntervention(inter5)


        getData()
    }


    fun addIntervention(intervention: Intervention) {
        AppExecutors.instance!!.diskIO().execute {
            interventionDatabase.interventionDao().addIntervention(intervention)
        }
    }

    fun addNewIntervention(){
        AppExecutors.instance!!.diskIO().execute {
            val next = interventionDatabase.interventionDao().getNextNumero()
            if (next.isNotEmpty()){
                val nextIndex = next[0] + 1
                AppExecutors.instance!!.mainThread().execute( Runnable {
                    val intent = Intent(this, InterventionActivity::class.java)
                    intent.putExtra("numeroIntervention", nextIndex)
                    startActivity(intent)
                })
            }
        }
    }

    fun getData(){
        AppExecutors.instance!!.diskIO().execute(Runnable {
            dataList.clear()
            searchedList.clear()
            dataList.addAll(interventionDatabase.interventionDao().loadAllIntervention())
            searchedList.addAll(dataList)
            //refreshList()
            AppExecutors.instance!!.mainThread().execute( Runnable {
                adapter.notifyDataSetChanged()
            })
        })
    }

    class InterventionAdapter(val activity : MainActivity) : RecyclerView.Adapter<InterventionAdapter.InterventionViewHolder>(){
        class InterventionViewHolder(v : View) : RecyclerView.ViewHolder(v){
            val numeroTitle = v.findViewById<TextView>(R.id.number_title)
            val dateTitle = v.findViewById<TextView>(R.id.date_title)
            val plombierTitle = v.findViewById<TextView>(R.id.plombierTitle)
            val typeTitle = v.findViewById<TextView>(R.id.typeTitle)
            val itemLayout = v.findViewById<RelativeLayout>(R.id.dataItemLayout)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InterventionViewHolder {
            return InterventionViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_layout, parent, false))
        }

        override fun getItemCount(): Int {
            return activity.searchedList.size
        }

        override fun onBindViewHolder(holder: InterventionViewHolder, position: Int) {
            holder.numeroTitle.text = activity.searchedList[position].numero.toString()
            holder.dateTitle.text = activity.searchedList[position].date
            holder.typeTitle.text = activity.resources.getStringArray(R.array.typesList)[activity.searchedList[position].type]
            holder.plombierTitle.text = activity.resources.getStringArray(R.array.plombierList)[activity.searchedList[position].plombier]

            holder.itemLayout.setOnClickListener {
                val intent = Intent(activity, InterventionActivity::class.java)
                intent.putExtra("numeroIntervention", activity.searchedList[position].numero)
                activity.startActivity(intent)
            }


        }
    }
}