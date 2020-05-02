package com.example.td2_exo3


import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_intervention.*
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class InterventionActivity : AppCompatActivity() {

    var currentIndexPlomboier = 0
    var currentIndexType = 0

    var list = arrayListOf<Intervention>()

    lateinit var intervention : Intervention

    private lateinit var interventionDatabase : InterventionRoomDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intervention)

        interventionDatabase = InterventionRoomDatabase.getDatabase(this)


        val numeroIntervention = intent.getIntExtra("numeroIntervention", -1)

        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val currentDate = sdf.format(Date())

        intervention = Intervention(numeroIntervention, currentDate, 0, 0)

        getData()

        getIntervention(numeroIntervention)

        ArrayAdapter.createFromResource(
            this,
            R.array.plombierList,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
            spinnerPlombier.adapter = adapter
        }

        spinnerPlombier.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentIndexPlomboier = position
            }
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.typesList,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
            spinnerType.adapter = adapter
        }

        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentIndexType = position
            }
        }

        getDate.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)


            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                var dayS = "$dayOfMonth"
                var monthS = "${monthOfYear+1}"
                if (dayOfMonth < 10){
                    dayS = "0$dayOfMonth"
                }


                if (monthOfYear < 9){
                    monthS = "0${monthOfYear+1}"
                }

                dateIntervention.setText("" + dayS + "/" + monthS + "/" + year)
            }, year, month, day)

            dpd.show()

        }


        checkBtn.setOnClickListener {
            val newIntervention = Intervention(intervention.numero, dateIntervention.text.toString(), currentIndexPlomboier, currentIndexType)
            addIntervention(newIntervention)
        }

        floatingActionButton.setOnClickListener{
            deleteIntervention(intervention)
        }

    }

    fun addIntervention(intervention: Intervention) {
        AppExecutors.instance!!.diskIO().execute {
            interventionDatabase.interventionDao().addIntervention(intervention)

            AppExecutors.instance!!.mainThread().execute( Runnable {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            })
        }
    }


    fun getData(){
        AppExecutors.instance!!.diskIO().execute(Runnable {
            list.clear()
            list.addAll(interventionDatabase.interventionDao().loadAllIntervention())

        })
    }

    fun deleteIntervention(intervention: Intervention){
        AppExecutors.instance!!.diskIO().execute(Runnable {
            interventionDatabase.interventionDao().deleteIntervention(intervention)

            AppExecutors.instance!!.mainThread().execute( Runnable {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            })

        })
    }

    fun getIntervention(numeroIntervention : Int){
        AppExecutors.instance!!.diskIO().execute(Runnable {
            val searchedList = interventionDatabase.interventionDao().findIntervention(numeroIntervention)
            //refreshList()
            AppExecutors.instance!!.mainThread().execute( Runnable {
                if (searchedList.isNotEmpty()){
                    intervention = searchedList[0]
                    currentIndexPlomboier = intervention.plombier
                    currentIndexType = intervention.type
                }
                interventionNumber.text = intervention.numero.toString()
                dateIntervention.setText(intervention.date)
                spinnerPlombier.setSelection(intervention.plombier)
                spinnerType.setSelection(intervention.type)
            })
        })
    }
}
