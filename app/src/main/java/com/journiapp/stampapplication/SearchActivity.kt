package com.journiapp.stampapplication

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.widget.textChanges
import com.journiapp.stampapplication.model.API
import com.journiapp.stampapplication.model.CountryCodeRequest
import com.journiapp.stampapplication.model.CountryCodeResponse
import com.journiapp.stampapplication.model.SearchSuggestion
import io.reactivex.android.schedulers.AndroidSchedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit


/** Search for places and show autocomplete results, return selected country (ISO2 country code). */
class SearchActivity : AppCompatActivity(), SearchAdapter.OnItemClickListener {

    private lateinit var searchEditText: AppCompatEditText
    var adapter: SearchAdapter? = null
    var api: API? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Search"
        setSupportActionBar(toolbar)

        api = NetworkUtil.providesRetrofit(applicationContext).create(API::class.java)
        searchEditText = findViewById(R.id.et_search_destination)

        adapter = SearchAdapter(applicationContext, this)

        val rvSearchSuggestion = findViewById<RecyclerView>(R.id.rv_search_suggestion)
        rvSearchSuggestion.layoutManager = LinearLayoutManager(applicationContext)
        rvSearchSuggestion.adapter = adapter

        RxsearchView()

        findViewById<Button>(R.id.btn_done).setOnClickListener { v ->
            finish()
        }
    }

    fun bindSearchSuggestion(countryName: String) {
        findViewById<RecyclerView>(R.id.rv_search_suggestion).visibility = View.GONE
        val etSearchDestination = findViewById<EditText>(R.id.et_search_destination)
        etSearchDestination.setText(countryName)
        etSearchDestination.setSelection(etSearchDestination.text!!.length)
    }

    fun requestSearchSuggestions(query: String?) {
        if (query != null && query.length >= 2) {
            api!!.getCountries(query).enqueue(object : Callback<ArrayList<SearchSuggestion>> {

                override fun onResponse(
                    call: Call<ArrayList<SearchSuggestion>>,
                    response: Response<ArrayList<SearchSuggestion>>
                ) {
                    adapter?.setItems(response.body()!!)
                    adapter?.notifyDataSetChanged()
                }

                override fun onFailure(call: Call<ArrayList<SearchSuggestion>>, t: Throwable) {
                }
            })
        }
    }

    fun requestCountryCode(searchSuggestion: SearchSuggestion) {
        val request = CountryCodeRequest(searchSuggestion.reference)
        api!!.getCountryCode(request)
            .enqueue(object : Callback<CountryCodeResponse> {
                override fun onResponse(
                    call: Call<CountryCodeResponse>,
                    response: Response<CountryCodeResponse>
                ) {
                    val countryCode = response.body()!!
                    DataSingleton.countryCode = countryCode.countryCode
                }

                override fun onFailure(call: Call<CountryCodeResponse>, throwable: Throwable) {

                }
            })
    }

    override fun onSearchSuggestionClick(position: Int) {
        // very rarely items are not updated on time. in this case the object is null and we do nothing
        val searchSuggestion = adapter!!.getItem(position) ?: return
        bindSearchSuggestion(searchSuggestion.description)
        requestCountryCode(searchSuggestion)
    }

    // TODO: code is not optimise properly
    fun RxsearchView() {
        searchEditText
            .textChanges()
            .subscribe({
                Log.d("SearchActivity", it.toString())
            }, {
                Log.e("SearchActivity", it.toString())
            })
        searchEditText
            .textChanges()
            .skip(1)
            .map { it.toString() }
            .doOnNext {

            }
            .debounce(800, TimeUnit.MILLISECONDS)

            .observeOn(AndroidSchedulers.mainThread())
            .doOnEach {

            }
            .doOnError { Log.e("TAG", "error") }
            .retry()
            .subscribe({
                requestSearchSuggestions(it.toString())
            }, {
                Log.e("SearchActivity", it.toString())
            })
    }
}