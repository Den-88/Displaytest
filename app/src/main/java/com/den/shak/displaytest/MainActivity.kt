package com.den.shak.displaytest

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.den.shak.displaytest.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.common.MobileAds
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    // Переменная для отображения баннерной рекламы
    private var bannerAd: BannerAdView? = null
    // ViewBinding для работы с layout через объект binding
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Инициализация рекламного SDK Yandex
        MobileAds.initialize(this) {}
        // Инициализация binding для доступа к элементам разметки
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Включение отображения значка "Домой" в ActionBar
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        // Установка отступов для главного View с учетом системных панелей
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Обработка нажатия кнопки "Назад", при которой приложение сворачивается
        onBackPressedDispatcher.addCallback(this) {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        // Отслеживание изменения размера контейнера для рекламы и загрузка баннера
        binding.adContainerView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // Удаляем слушатель после первого вызова
                binding.adContainerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                // Загружаем баннерную рекламу с вычисленным размером
                bannerAd = loadBannerAd(adSize)
            }
        })
    }

    // Вычисление размера баннера на основе ширины контейнера и плотности экрана
    private val adSize: BannerAdSize
        get() {
            // Получаем ширину контейнера
            var adWidthPixels = binding.adContainerView.width
            if (adWidthPixels == 0) {
                adWidthPixels = resources.displayMetrics.widthPixels
            }
            // Преобразуем ширину в dp для баннера
            val adWidth = (adWidthPixels / resources.displayMetrics.density).roundToInt()
            return BannerAdSize.stickySize(this, adWidth)
        }

    // Создание меню
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // Обработка нажатий на элементы меню
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Переход к экрану с инструкцией
            R.id.instr -> {
                val intent = Intent(this, ManualActivity::class.java)
                startActivity(intent)
                return true
            }
            // Открытие диалогового окна "О приложении"
            R.id.about -> {
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.MenuAbout)
                    .setMessage(R.string.AboutText)
                    .setNegativeButton(R.string.AboutButton) { dialog, _ -> dialog.cancel() }
                    .show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    // Обработчик для кнопки, запускающий TestActivity
    fun start(view: View?) {
        val intent = Intent(this@MainActivity, TestActivity::class.java)
        startActivity(intent)
    }

    // Метод для загрузки баннерной рекламы с обработкой событий
    private fun loadBannerAd(adSize: BannerAdSize): BannerAdView {
        return binding.adContainerView.apply {
            setAdSize(adSize)
            setAdUnitId(ConfigReader.getAdUnitId(this@MainActivity)) // Получаем ID рекламного блока
            setBannerAdEventListener(object : BannerAdEventListener {
                // Обработка успешной загрузки рекламы
                override fun onAdLoaded() {
                    if (isDestroyed) {
                        bannerAd?.destroy()
                        return
                    }
                }

                // Обработка ошибки загрузки рекламы
                override fun onAdFailedToLoad(error: AdRequestError) {}

                // Обработка клика на рекламу
                override fun onAdClicked() {}

                // Событие при уходе пользователя из приложения
                override fun onLeftApplication() {}

                // Событие при возврате в приложение
                override fun onReturnedToApplication() {}

                // Событие при показе рекламы
                override fun onImpression(impressionData: ImpressionData?) {}
            })
            // Загружаем рекламный запрос
            loadAd(AdRequest.Builder().build())
        }
    }
}
