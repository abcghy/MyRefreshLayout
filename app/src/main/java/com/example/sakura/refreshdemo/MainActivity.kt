package com.example.sakura.refreshdemo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.lib.MyRefreshLayout
import com.example.lib.OnRefreshListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mrl.onRefreshListener = object: OnRefreshListener {
            override fun onRefresh(refreshLayout: MyRefreshLayout) {
                Log.d("test", "onRefresh")
                Thread {
                    Thread.sleep(2000)
                    runOnUiThread {
                        refreshLayout.finishRefresh()
                    }
                }.start()
            }
        }
    }
}
