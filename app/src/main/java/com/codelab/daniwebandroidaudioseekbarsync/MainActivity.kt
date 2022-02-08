package com.codelab.daniwebandroidaudioseekbarsync

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : AppCompatActivity() {

    //Keeps a reference here to make it easy to release later
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Gets the textView_time reference
        val timeView = findViewById<TextView>(R.id.textView_time)

        //Gets the seekBar reference
        val seekBar = findViewById<SeekBar>(R.id.seekBar)

        //Launcher to open file with a huge callback. Organize in real code.
        val openMusicLauncher = registerForActivityResult(OpenDocument()){ uri ->
            //Instantiates a MediaPlayer here now that we have the Uri.
            mediaPlayer = MediaPlayer.create(applicationContext, uri)
                .also { //also {} scope function skips multiple null checks
                    seekBar.max = it.duration
                    it.start()

                    //Should be safe to use this coroutine to access MediaPlayer (not thread-safe)
                    //because it uses MainCoroutineDispatcher by default
                    lifecycleScope.launch {
                        while (it.isPlaying){
                            seekBar.progress = it.currentPosition
                            timeView.text = "${it.currentPosition.milliseconds}"
                            delay(1000)
                        }
                        //Can also release mediaPlayer here, if not looping.
                    }
                }

            //Move this object somewhere else in real code
            val seekBarListener = object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser){
                        //sets the playing file progress to the same seekbar progressive, in relative scale
                        mediaPlayer?.seekTo(progress)

                        //Also updates the textView because the coroutine only runs every 1 second
                        timeView.text = "${progress.milliseconds}"
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }

            seekBar.setOnSeekBarChangeListener(seekBarListener)
        }

        val mimeTypes = arrayOf("audio/mpeg")
        openMusicLauncher.launch(mimeTypes)
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}