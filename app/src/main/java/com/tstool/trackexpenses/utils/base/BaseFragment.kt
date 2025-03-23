package com.tstool.trackexpenses.utils.base

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import java.io.Serializable

/**
 * Note: convert view xml to databinding before use
 */
abstract class BaseFragment<Binding : ViewBinding>(private val inflate: Inflate<Binding>) :
    Fragment() {
    protected lateinit var binding: Binding


    protected abstract fun initAds()

    protected abstract fun initViewModel()

    protected abstract fun initData()

    protected abstract fun initView()

    protected abstract fun initAction()



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAds()
        initViewModel()
        initView()
        initData()
        initAction()
    }

    val flagClearOldTask =
        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK


    protected fun goToNewActivity(
        activity: Class<*>,
        isFinish: Boolean = false,
        key: String = "",
        data: Any? = null,
        flags: Int? = null
    ) {
        val intent = Intent(requireActivity(), activity).apply {
            if (key.isNotEmpty() && data != null) {
                when (data) {
                    is Parcelable -> putExtra(key, data)
                    is Serializable -> putExtra(key, data)
                    else -> throw IllegalArgumentException("Data must be Parcelable or Serializable")
                }
            }
            flags?.let {
                addFlags(it)
            }
        }

        startActivity(intent)

        if (isFinish) {
            requireActivity().finish()
        }
    }


    protected fun toastMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }




//    fun isPremiumAccount(): Boolean {
//        val isPremium = when (AppLovinManager.IS_TEST_PREMIUM) {
//            true -> true
//            false -> MobileAdsModule.premiumSharedPref().isPremium
//        }
//        Log.d("__PREMIUM", "isPremiumAccount: $isPremium")
//        return isPremium
//    }
}
