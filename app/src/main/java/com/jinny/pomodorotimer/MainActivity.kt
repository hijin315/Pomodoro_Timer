package com.jinny.pomodorotimer

import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.SeekBar
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private val minuteTextView: TextView by lazy {
        findViewById(R.id.minuteTextView)
    }
    private val secondTextView: TextView by lazy {
        findViewById(R.id.secondTextView)
    }
    private val seekBar: SeekBar by lazy {
        findViewById(R.id.seekBar)
    }
    private val soundPool = SoundPool.Builder().build()
    private var tickingsoundId: Int? = null
    private var bellSoundId: Int? = null

    private var currentCountDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        initSound()
    }

    override fun onResume() {
        super.onResume()
        soundPool.autoResume()
    }

    override fun onPause() {
        super.onPause()
        soundPool.autoPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()     //soundPool에 로드되었던 파일들이 해제됨 -> 메모리를 아낄 수 있음
    }

    private fun bindViews() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // from User : 사용자에 의한 변경인지
                if (fromUser) {
                    updateRemainTime(progress * 60 * 1000L)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                stopCountDown()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar ?: return     //seekBar가 null인 경우 return
                if (seekBar.progress == 0) {
                    stopCountDown()
                } else {
                    startCountDown()
                }
            }
        }
        )
    }
    private fun stopCountDown(){
        currentCountDownTimer?.cancel()
        currentCountDownTimer = null
        soundPool.autoPause()
    }
    private fun initSound() {
        tickingsoundId = soundPool.load(this, R.raw.timer_ticking, 1)
        bellSoundId = soundPool.load(this, R.raw.timer_bell, 1)
    }

    private fun createCountDownTimer(initialMillis: Long): CountDownTimer {
        return object : CountDownTimer(initialMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                //1초마다 한번씩 불림
                updateRemainTime(millisUntilFinished)
                updateSeekBar(millisUntilFinished)
            }

            override fun onFinish() {
                completeCountDown()
            }
        }

    }

    private fun startCountDown() {
        currentCountDownTimer =
            createCountDownTimer(seekBar!!.progress * 60 * 1000L).start()
        currentCountDownTimer?.start()
        tickingsoundId?.let { soundID ->
            soundPool.play(soundID, 1F, 1F, 0, -1, 1F)
        }
    }

    private fun completeCountDown() {
        updateRemainTime(0)
        updateSeekBar(0)

        soundPool.autoPause()
        bellSoundId?.let { soundId ->
            soundPool.play(soundId, 1F, 1F, 0, 0, 1F)
        }
    }

    private fun updateRemainTime(remainMillis: Long) {
        val remainSecond = remainMillis / 1000
        minuteTextView.text = "%02d`".format(remainSecond / 60)
        secondTextView.text = "%02d".format(remainSecond % 60)
    }

    private fun updateSeekBar(remainMillis: Long) {
        seekBar.progress = (remainMillis / 1000 / 60).toInt()
    }
}