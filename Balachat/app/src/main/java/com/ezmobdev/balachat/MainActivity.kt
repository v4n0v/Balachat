package com.ezmobdev.balachat

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.ezmobdev.balachat.custom.RecyclerAdapter
import com.ezmobdev.balachat.databinding.ItemMessageBinding
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.Stream
import com.tinder.scarlet.StreamAdapter
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

import okio.ByteString
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivityTAG"
        const val CLOSE_STATUS = 1000
             const val URL = "ws://5.23.52.7:8080"
        //      const val URL = "ws://echo.websocket.org"
    }

    private val messages = arrayListOf<String>()
    lateinit var client: TestService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val okHttpClient = provideOkHttpClient()
        client = Scarlet.Builder()
            .webSocketFactory(okHttpClient.newWebSocketFactory(URL))
//            .addStreamAdapterFactory( RxJava2StreamAdapterFactory())

//            .lifecycle(AndroidLifecycle.ofApplicationForeground(application))
            .build()
            .create()
        val adapter = RecyclerAdapter.RecyclerAdapterBuilder<String, ItemMessageBinding>()
            .setLauoutId(R.layout.item_message)
            .initBinding{item, _ ->
                this.tvMessage.text = item
            }
            .setItems(messages)
            .build()

        rvMesaages.adapter = adapter


        client.observeText().start(object : Stream.Observer<String> {
            override fun onComplete() {
                Log.d(TAG, "onComplete")
            }

            override fun onError(throwable: Throwable) {
                Log.d(TAG, "onError ${throwable.message}")

            }

            override fun onNext(data: String) {
                Log.d(TAG, "onNext $data")
                runOnUiThread {
                    messages.add(data)
                    adapter.notifyDataSetChanged()
                }

            }

        })


        button.setOnClickListener {
            val t = inputText.text.toString()
            if (t.isNotEmpty())
                send(t)
        }
    }

    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .build()


    private fun send(s: String) {


        Completable.fromAction {
            Log.d(TAG, "try Send")
            client.sendText(s)
        }
            .subscribeOn(Schedulers.computation())
            .subscribe()

    }


    interface TestService {
        @Send
        fun sendText(message: String)

        @Receive
        fun observeText(): Stream<String>
    }

}
