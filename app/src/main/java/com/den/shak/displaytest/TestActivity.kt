package com.den.shak.displaytest

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity

class TestActivity : AppCompatActivity() {
    // Handler для задержек выполнения операций
    private val mHideHandler = Handler(Looper.getMainLooper())
    // View для изменения цвета фона
    private var mColor: View? = null
    // Счетчик для смены цветов
    private var n: Int = 0
    // Runnable для скрытия системных элементов интерфейса
    private val mHideRunnable = Runnable { hide() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        // Инициализация View, у которой будет меняться цвет
        mColor = findViewById(R.id.testColor)

        // Обработчик нажатия кнопки "Назад"
        onBackPressedDispatcher.addCallback(this) {
            // Меняет цвет при каждом нажатии "Назад"
            changeColor(view = findViewById(R.id.testColor))
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Запуск метода hide() через 100 миллисекунд после создания активности
        mHideHandler.postDelayed(mHideRunnable, 100)
    }

    // Метод для скрытия системных элементов интерфейса (ActionBar, статус и навигационные панели)
    private fun hide() {
        // Скрыть ActionBar, если он существует
        val actionBar = supportActionBar
        actionBar?.hide()

        // Версия Android R и выше использует WindowInsetsController для управления системными панелями
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                // Настройка поведения системных панелей при свайпе
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                // Скрыть системные панели (статус бар и навигация)
                it.hide(WindowInsets.Type.systemBars())
            }
        } else {
            // Устаревший метод для скрытия системных панелей до Android R
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }
    }

    // Метод для смены цвета фона
    fun changeColor(view: View) {
        when (n) {
            0 -> changeBackgroundColor(Color.WHITE, Color.BLACK)  // Изменение с белого на черный
            1 -> changeBackgroundColor(Color.BLACK, Color.RED)    // Изменение с черного на красный
            2 -> changeBackgroundColor(Color.RED, Color.GREEN)    // Изменение с красного на зеленый
            3 -> changeBackgroundColor(Color.GREEN, Color.BLUE)   // Изменение с зеленого на синий
            4 -> {
                // По достижению 4 изменений, обнуляем счетчик и переходим на другую активность
                n = 0
                val intent = Intent(this@TestActivity, MainActivity::class.java)
                startActivity(intent)
            }
        }
        n++  // Увеличиваем счетчик для отслеживания последовательности цветов
    }

    // Анимация изменения цвета фона
    private fun changeBackgroundColor(colorFrom: Int, colorTo: Int) {
        val duration = 700  // Продолжительность анимации (в миллисекундах)
        mColor?.let {
            // Создание анимации смены цвета с использованием ArgbEvaluator
            ObjectAnimator.ofObject(it, "backgroundColor", ArgbEvaluator(), colorFrom, colorTo)
                .setDuration(duration.toLong())  // Установка продолжительности анимации
                .start()  // Запуск анимации
        }
    }
}
