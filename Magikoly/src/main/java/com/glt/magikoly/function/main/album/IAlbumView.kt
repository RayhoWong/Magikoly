package com.glt.magikoly.function.main.album

import com.glt.magikoly.function.main.album.presenter.AlbumPresenter
import com.glt.magikoly.mvp.IViewInterface

interface IAlbumView : IViewInterface<AlbumPresenter> {
    fun showProgressBar()
    fun hideProgressBar()
}
