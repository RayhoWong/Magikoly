package com.glt.magikoly.function.main.discovery.presenter

import com.android.volley.VolleyError
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.bean.net.SearchImageResponseBean
import com.glt.magikoly.function.FaceFunctionManager
import com.glt.magikoly.function.main.discovery.DiscoveryController
import com.glt.magikoly.function.main.discovery.IDiscoveryView
import com.glt.magikoly.mvp.AbsPresenter
import com.glt.magikoly.net.RequestCallback
import com.glt.magikoly.utils.Logcat
import com.glt.magikoly.utils.Machine
import magikoly.magiccamera.R

class DiscoveryPresenter : AbsPresenter<IDiscoveryView>() {

    fun search(keyword: String, mode: Int, page: Int) {
        if (keyword.isBlank()) {
            obtainSearchHotWord()
        } else {
            FaceFunctionManager.requestSearchImage(keyword, mode, page,
                    object : RequestCallback<SearchImageResponseBean> {
                        override fun onErrorResponse(error: VolleyError?) {
                            view?.obtainSearchContentFailure(error?.message ?: "", page)
                        }

                        override fun onResponse(response: SearchImageResponseBean?) {
                            view?.obtainSearchContentSuccess(
                                    response?.images ?: arrayListOf(), page)
                        }
                    })
        }
    }

    /**
     * 获取热词数据
     */
    fun obtainSearchHotWord() {
        val hotwordOnline = DiscoveryController.getInstance().getHotwordOnline()
        if (hotwordOnline.isEmpty()) {
            Logcat.d("ldf", "SearchHotWord local")
            obtainLocalHotword()
        } else {
            view?.obtainHotWordSuccess(hotwordOnline.map {
                Logcat.d("ldf", "SearchHotWord online")
                it.word ?: ""
            })
        }
    }

    private fun obtainLocalHotword() {
        val country = Machine.getSimCountryIso(FaceAppState.getContext()).toLowerCase()
        val hotword = when (country) {
            "us" -> {
                FaceAppState.getContext().getString(R.string.search_hotword_us)
            }
            "uk", "gb" -> {
                FaceAppState.getContext().getString(R.string.search_hotword_uk)
            }
            "ca" -> {
                FaceAppState.getContext().getString(R.string.search_hotword_ca)
            }
            "au" -> {
                FaceAppState.getContext().getString(R.string.search_hotword_au)
            }
            "no" -> {
                FaceAppState.getContext().getString(R.string.search_hotword_no)
            }
            "de" -> {
                FaceAppState.getContext().getString(R.string.search_hotword_de)
            }
            "fr" -> {
                FaceAppState.getContext().getString(R.string.search_hotword_fr)
            }
            "es" -> {
                FaceAppState.getContext().getString(R.string.search_hotword_es)
            }
            "pt" -> {
                FaceAppState.getContext().getString(R.string.search_hotword_pt)
            }
            "hk" -> {
                FaceAppState.getContext().getString(R.string.search_hotword_hk)
            }
            "tw" -> {
                FaceAppState.getContext().getString(R.string.search_hotword_tw)
            }
            "cn" -> {
                FaceAppState.getContext().getString(R.string.search_hotword_ch)
            }
            "ie" -> {
                FaceAppState.getContext().getString(R.string.search_hotword_ie)
            }
            "kr" -> {
                FaceAppState.getContext().getString(R.string.search_hotword_kr)
            }
            "jp" -> {
                FaceAppState.getContext().getString(R.string.search_hotword_jp)
            }
            "it" -> {
                FaceAppState.getContext().getString(R.string.search_hotword_it)
            }
            "ru" -> {
                FaceAppState.getContext().getString(R.string.search_hotword_ru)
            }
            "ar","eg" -> {
                FaceAppState.getContext().getString(R.string.search_hotword_ar)
            }
            else -> {
                FaceAppState.getContext().getString(R.string.search_hotword_us)
            }
        }
        view?.obtainHotWordSuccess(hotword.split(","))
    }
}