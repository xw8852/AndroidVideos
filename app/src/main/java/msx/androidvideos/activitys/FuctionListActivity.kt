package msx.androidvideos.activitys

import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.widget.TextViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import msx.androidvideos.R

class FuctionListActivity : AppCompatActivity() {

    var recyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fuction_list)
        recyclerView = getView(R.id.recycle)
        recyclerView?.layoutManager = LinearLayoutManager(this)
        recyclerView?.adapter = adapter
        adapter.data = arrayListOf("camera预览(API21以下)")

    }

    var adapter: SimpleRecyclerAdapter<String> = object : SimpleRecyclerAdapter<String>() {

        override fun onBindViewHolder(holder: DSLViewHolder?, position: Int) {
            holder?.getView<TextView>(R.id.textView)?.setTextColor(Color.BLACK)
            holder?.getView<TextView>(R.id.textView2)?.setTextColor(Color.BLACK)
            holder?.getView<TextView>(R.id.textView)?.text = data[position]
            holder?.getView<TextView>(R.id.textView2)?.text = data[position] + "_  ${position} test for end"
            holder?.itemView?.setOnClickListener {
                startActivity(Intent(this@FuctionListActivity, CameraActivity::class.java))
            }
        }

        override fun getViewLayout(viewType: Int, parent: ViewGroup?): View {
            return LayoutInflater.from(parent?.context).inflate(R.layout.layout_item, null)
        }
    }
}
