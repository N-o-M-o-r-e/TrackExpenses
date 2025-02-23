package com.tstool.trackexpenses.utils.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.ViewPager2
import java.io.Serializable

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class BaseActivity<Binding : ViewBinding>(private val inflate: Inflate<Binding>) :
    BaseView() {
    protected lateinit var binding: Binding


    @RequiresApi(Build.VERSION_CODES.O)
    override fun bindView() {
        binding = inflate(LayoutInflater.from(this), null, false)
        val mView = binding.root
        setContentView(mView)
        actionWindow()
    }

    @SuppressLint("NewApi")
    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun actionWindow() {
        enableEdgeToEdge()
        window.navigationBarColor = Color.BLACK
        window.statusBarColor = Color.BLACK
        window.decorView.windowInsetsController?.setSystemBarsAppearance(
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS.inv(),
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        )

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * START ACTIVITY WITH RESULT
     */
    private val launcherStartActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val callback = callbackActivityResult
            callback?.invoke(it)
            callbackActivityResult = null
        }

    private var callbackActivityResult: ((result: ActivityResult?) -> Unit)? = null

    fun startActivityWithResult(
        intent: Intent, callback: (result: ActivityResult?) -> Unit
    ) {
        kotlin.runCatching {
            callbackActivityResult = callback
            launcherStartActivityResult.launch(intent)
        }.getOrElse {
            callback(null)
        }
    }/*
    * START ACTIVITY WITH RESULT
    * */

    /*
    * REQUEST PERMISSION
    * */
    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        permissionCallback?.invoke(results.takeIf { it.isNotEmpty() }?.all { it.value } ?: false)
    }

    private var permissionCallback: ((granted: Boolean) -> Unit)? = null

    fun startRequestPermissions(
        permissions: Array<String>, callback: (granted: Boolean) -> Unit
    ) {
        runCatching {
            permissionCallback = callback
            requestPermissionsLauncher.launch(permissions)
        }.getOrElse {
            it.printStackTrace()
            callback(false)
        }
    }


}


abstract class BaseView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindView()
        initAds()
        initData()
        initView()
        initAction()
        initViewModel()
    }


    /**
     * Note: Hide both the status bar and the navigation bar
     */


    protected abstract fun bindView()
    protected abstract fun initAds()
    protected abstract fun initViewModel()
    protected abstract fun initData()
    protected abstract fun initView()
    protected abstract fun initAction()

    override fun onBackPressed() {
        super.onBackPressedDispatcher.onBackPressed()
    }


    /**
     * Note: action user focus view
     */

    override fun dispatchTouchEvent(motionEvent: MotionEvent): Boolean {
        if (motionEvent.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (view is EditText) {
                val outRect = Rect()
                view.getGlobalVisibleRect(outRect)
                if (!outRect.contains(motionEvent.rawX.toInt(), motionEvent.rawY.toInt())) {
                    view.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
                }
            }
        }

        return super.dispatchTouchEvent(motionEvent)
    }

    val flagClearOldTask: Int
        get() = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

    protected fun goToNewActivity(
        activity: Class<*>,
        isFinish: Boolean = false,
        key: String = "",
        data: Any? = null,
        flags: Int? = null
    ) {
        val intent = Intent(this, activity).apply {
            // Truyền dữ liệu với key
            if (key.isNotEmpty() && data != null) {
                when (data) {
                    is Parcelable -> putExtra(key, data)
                    is Serializable -> putExtra(key, data)
                    else -> throw IllegalArgumentException("Data must be Parcelable or Serializable")
                }
            }

            // Thêm các flags nếu có
            flags?.let {
                addFlags(it)
            }
        }
        startActivity(intent)

        if (isFinish) {
            finish()
        }

    }

    /**
     * registerForActivityResult
     */

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                onActivityResultReceived(data) // Gọi hàm callback khi có kết quả
            }
        }

    /**
     * Hàm mở Activity với kết quả trả về
     */
    protected fun goToNewActivityForResult(
        activity: Class<*>, key: String = "", data: Any? = null, flags: Int? = null
    ) {
        val intent = Intent(this, activity).apply {
            if (key.isNotEmpty() && data != null) {
                when (data) {
                    is Parcelable -> putExtra(key, data)
                    is Serializable -> putExtra(key, data)
                    else -> throw IllegalArgumentException("Data must be Parcelable or Serializable")
                }
            }
            flags?.let { addFlags(it) }
        }
        resultLauncher.launch(intent) // Mở Activity và chờ kết quả
    }

    /**
     * Callback nhận dữ liệu từ Activity con
     */
    protected open fun onActivityResultReceived(data: Intent?) {
        // Các Activity con có thể override để xử lý kết quả

    }

    /**
     * Callback nhận dữ liệu từ Activity con
     */
    protected open fun returnData(resultCode: Int, keyReturn: String, dataReturn: Any) {
        val resultIntent = Intent().apply {
            when (dataReturn) {
                is Parcelable -> putExtra(keyReturn, dataReturn)
                is Serializable -> putExtra(keyReturn, dataReturn)
                else -> throw IllegalArgumentException("Data must be Parcelable or Serializable")
            }
        }
        setResult(resultCode, resultIntent)
        finish()
    }

    /**
     * Lấy dữ liệu từ Intent
     */
    protected inline fun <reified T> getIntentData(key: String, default: T? = null): T? {
        val data = intent?.extras?.get(key)
        return when {
            data is T -> data
            default != null -> default
            else -> null
        }
    }


    class FadePageTransformer : ViewPager2.PageTransformer {
        override fun transformPage(page: View, position: Float) {
            val pageWidth = page.width
            if (position < -1) {
                page.alpha = 0f
            } else if (position <= 0) {
                page.alpha = 1f
                page.translationX = 0f
                page.scaleX = 1f
                page.scaleY = 1f
            } else if (position <= 1) {
                page.alpha = 1 - position
                page.translationX = pageWidth * -position
            } else {
                page.alpha = 0f
            }
        }
    }

    protected fun toastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    protected fun showToast(messageResId: Int) {
        Toast.makeText(this, getText(messageResId), Toast.LENGTH_SHORT).show()
    }

}

/**
 * Các sử dụng registerForActivityResult:
 *
 * class MainActivity : BaseActivity() {
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         setContentView(R.layout.activity_main)
 *
 *         val btnOpen = findViewById<Button>(R.id.btnOpenActivity)
 *         btnOpen.setOnClickListener {
 *             goToNewActivityForResult(SecondActivity::class.java, "EXTRA_MESSAGE", "Hello từ MainActivity!")
 *         }
 *     }
 *
 *     // Nhận dữ liệu từ SecondActivity
 *     override fun onActivityResultReceived(data: Intent?) {
 *         val message = data?.getStringExtra("RESULT_MESSAGE") ?: "Không có dữ liệu"
 *         Toast.makeText(this, "Kết quả: $message", Toast.LENGTH_SHORT).show()
 *     }
 * }
 *=======================================================
 * class SecondActivity : BaseActivity() {
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         setContentView(R.layout.activity_second)
 *
 *         val message = getIntentData("EXTRA_MESSAGE", "Không có tin nhắn")
 *         findViewById<TextView>(R.id.tvMessage).text = message
 *
 *         findViewById<Button>(R.id.btnSendResult).setOnClickListener {
 *              val data: String = "Kết quả từ SecondActivity"
 *             returnData(Activity.RESULT_OK,KEY_RETURN_DATA, data) // Đóng Activity và gửi kết quả về
 *         }
 *     }
 *
 *     companion object{
 *         const val KEY_RETURN_DATA = "KEY_RETURN_DATA"
 *     }
 * }
 *
 *
 */

