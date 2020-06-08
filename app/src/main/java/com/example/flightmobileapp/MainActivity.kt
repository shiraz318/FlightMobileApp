package com.example.flightmobileapp

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room

class MainActivity : AppCompatActivity() {
    lateinit var connectButton: Button
    lateinit var inputUrl: EditText
    //private lateinit var urlItem: RecyclerView
    //lateinit var specificUrl: TextView
    //private lateinit var urlViewModel: URLViewModel
    private lateinit var listView: ListView

    // @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //setContentView(R.layout.recyclerview_item)

        inputUrl = findViewById(R.id.input_text)
        connectButton = findViewById(R.id.connect_button)
        //urlItem = findViewById(R.id.recyclerview)


        //urlItem.setOnClickListener { clickMe() }

        val db = Room.databaseBuilder(applicationContext, URLRoomDatabase::class.java, "url_table")
            .build()
        connectButton.setOnClickListener { connect(inputUrl, db) }
        listView = findViewById<ListView>(R.id.recipe_list_view)
// 1
        //val recipeList = Recipe.getRecipesFromFile("recipes.json", this)
        val urlList = db.urlDao().getAlphabetizedWords()
        val size = db.urlDao().getSize()
        val listItems = arrayOfNulls<String>(size)

// 3
        for (i in 0 until size) {
            val url = urlList.value?.get(i)
            if (url != null) {
                listItems[i] = url.url
            }
        }
// 4
        val adapter1 = ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems)
        listView.adapter = adapter1


//        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
//        val adapter = URLListAdapter(this)
//        recyclerView.adapter = adapter
//        recyclerView.layoutManager = LinearLayoutManager(this)
//
//        urlViewModel = ViewModelProvider(this).get(URLViewModel::class.java)
//        urlViewModel.allUrls.observe(this, Observer { words ->
//            // Update the cached copy of the words in the adapter.
//            words?.let { adapter.setUrls(it) }
//        })
//        recyclerView.addOnItemTouchListener(
//            RecyclerItemClickListenr(
//                this,
//                recyclerView,
//                object : RecyclerItemClickListenr.OnItemClickListener {
//
//                    override fun onItemClick(view: View, position: Int) {
//                        if (position == 0) {
//                            Toast.makeText(
//                                applicationContext,
//                                "first item",
//                                Toast.LENGTH_LONG
//                            ).show()
//                        }
//                        if (position == 1) {
//                            Toast.makeText(
//                                applicationContext,
//                                "second item",
//                                Toast.LENGTH_LONG
//                            ).show()
//                        }
//                    }
//
//                    override fun onItemLongClick(view: View?, position: Int) {
//                        Toast.makeText(
//                            applicationContext,
//                            R.string.clicked,
//                            Toast.LENGTH_LONG
//                        ).show()
//                    }
//                })
//        )
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        Toast.makeText(
//            applicationContext,
//            R.string.inside,
//            Toast.LENGTH_LONG
//        ).show()
//        if (requestCode == newURLActivityRequestCode && resultCode == Activity.RESULT_OK) {
//            Toast.makeText(
//                applicationContext,
//                R.string.inside,
//                Toast.LENGTH_LONG
//            ).show()
//            data?.getStringExtra(MainActivity.EXTRA_REPLY)?.let {
//                val word = URL(it, "1")
//                urlViewModel.insert(word)
//            }
//        } else {
//            Toast.makeText(
//                applicationContext,
//                R.string.empty_not_saved,
//                Toast.LENGTH_LONG
//            ).show()
//        }
//    }

//    companion object {
//        const val EXTRA_REPLY = "com.example.android.urllistsql.REPLY"
//    }

    //  @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun connect(inputUrl: EditText, db : URLRoomDatabase) {
        // Server stuff.

        if (TextUtils.isEmpty(inputUrl.text)) {
            Toast.makeText(
                applicationContext,
                R.string.empty_not_saved,
                Toast.LENGTH_LONG
            ).show()
        } else {

            val url = inputUrl.text.toString()
            val word = URL(url, "1")
            db.urlDao().insert(word)
        }

        // If we connected successfully - go to the next activity.
        //startActivity(Intent(this, ControlActivity::class.java))
    }

    private fun Click() {
        val url = URL("shiraz", "2");
        Toast.makeText(
            applicationContext,
            R.string.clicked,
            Toast.LENGTH_LONG
        ).show()

        //inputUrl.text = url.url as Editable
        //inputUrl.setText(url.url)

    }

    fun clickMe() {
        Toast.makeText(
            applicationContext,
            R.string.clicked,
            Toast.LENGTH_LONG
        ).show()
    }

}