package com.journiapp.stampapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.journiapp.stampapplication.model.*
import com.journiapp.stampapplication.utils.StempClickListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/** Main screen. Shows stamps added by the user. */
// TODO: eveything is on the main thread , need to use some architecture guildline
class StampActivity : AppCompatActivity(), StempClickListener {

    var stempList = ArrayList<Stamp>()

    private lateinit var adapter: StampAdapter
    var api: API? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stamp)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Stamps"
        setSupportActionBar(toolbar)

        val sharedPrefHelper = SharedPrefHelper(applicationContext)

        api = NetworkUtil.providesRetrofit(applicationContext).create(API::class.java)

        val user = sharedPrefHelper.loadUserInfoJson()

        if (user == null) {
            // Note: Programmatic login to speed things up, but we would need a proper login screen
            val loginRequest = LoginRequest(
                email = "challenge@journiapp.com",
                password = "FreshAccount5"
            )
            api!!.login(loginRequest).enqueue(object : Callback<ProfileResponse> {

                override fun onResponse(
                    call: Call<ProfileResponse>,
                    response: Response<ProfileResponse>
                ) {
                    val user = UserInformation()
                    NetworkUtil.getCookieString(response.headers())?.let { user.sessionCookie = it }
                    sharedPrefHelper.saveUserInfo(user)
                    bindStamps(response.body()!!.stamps)
                    addStamp()
                }

                override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                    TODO("Handle failure")
                }
            })
        } else {

            api!!.getProfile().enqueue(object : Callback<ProfileResponse> {

                override fun onResponse(
                    call: Call<ProfileResponse>,
                    response: Response<ProfileResponse>
                ) {
                    response.body()?.let { stempList.addAll(it.stamps) }
                    bindStamps(response.body()!!.stamps)
                    addStamp()
                }

                override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {

                }
            })
        }

        findViewById<Button>(R.id.btn_search).setOnClickListener { openSearch() }
    }

    override fun onResume() {
        super.onResume()
        addStamp()
    }

    fun bindStamps(stempList: ArrayList<Stamp>) {

        adapter = StampAdapter(this)
        adapter.stamps = stempList
        val rvStamps = findViewById<RecyclerView>(R.id.rv_stamps)
        rvStamps.layoutManager =
            GridLayoutManager(applicationContext, resources.getInteger(R.integer.column_count))
        rvStamps.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    fun addStamp() {
        if (DataSingleton.countryCode == null) return

        val request = StampRequest(
            countryCode = DataSingleton.countryCode!!,
            create = System.currentTimeMillis(),false
        )
        DataSingleton.countryCode = null
        api!!.postStamp(request).enqueue(object : Callback<StampResponse> {

            override fun onResponse(call: Call<StampResponse>, response: Response<StampResponse>) {

                api!!.getProfile().enqueue(object : Callback<ProfileResponse> {

                    override fun onResponse(
                        call: Call<ProfileResponse>,
                        response: Response<ProfileResponse>
                    ) {
                        bindStamps(response.body()!!.stamps)
                        addStamp()

                    }

                    override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {

                    }
                })
            }

            override fun onFailure(call: Call<StampResponse>, t: Throwable) {
            }
        })
    }

    fun openSearch() {
        startActivity(Intent(this, SearchActivity::class.java))
    }

    override fun onStempClickListener(position: Int) {
        ShowDeleteDialog(position)
    }

    fun deleteStemp(position: Int) {

        val request = StampRequest(
            countryCode = stempList[position].countryCode.toString(),
            create = System.currentTimeMillis(), true
        )
        DataSingleton.countryCode = null
        api!!.postStamp(request).enqueue(object : Callback<StampResponse> {

            override fun onResponse(call: Call<StampResponse>, response: Response<StampResponse>) {

                api!!.getProfile().enqueue(object : Callback<ProfileResponse> {

                    override fun onResponse(
                        call: Call<ProfileResponse>,
                        response: Response<ProfileResponse>
                    ) {
                        if (response.isSuccessful) {
                            //removing the Item by Api call and updating the new list with Response
                            bindStamps(response.body()!!.stamps)
                            adapter.notifyItemRemoved(position)

                        }

                    }

                    override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {

                    }
                })
            }

            override fun onFailure(call: Call<StampResponse>, t: Throwable) {
            }
        })
    }

    //implemented dialog to allow user to remove stemp, I feel some issues while updating the list
    private fun ShowDeleteDialog(position: Int) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage(getString(R.string.dialog_message))
            .setCancelable(true)
            .setPositiveButton(getString(R.string.delete_positive_btn)) { dialog, id ->
                deleteStemp(position)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.delete_negative_btn)) { dialog, id ->
                dialog.cancel()
            }
        val alert = dialogBuilder.create()
        alert.setTitle(getString(R.string.delete_title))
        alert.show()
    }
}