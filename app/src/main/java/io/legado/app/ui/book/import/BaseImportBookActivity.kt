package io.legado.app.ui.book.import

import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModel
import io.legado.app.R
import io.legado.app.base.VMBaseActivity
import io.legado.app.databinding.ActivityImportBookBinding
import io.legado.app.help.config.AppConfig
import io.legado.app.lib.dialogs.alert
import io.legado.app.lib.theme.primaryTextColor
import io.legado.app.ui.document.HandleFileContract
import io.legado.app.utils.applyTint
import io.legado.app.utils.viewbindingdelegate.viewBinding
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

abstract class BaseImportBookActivity<VM : ViewModel> : VMBaseActivity<ActivityImportBookBinding, VM>() {

    final override val binding by viewBinding(ActivityImportBookBinding::inflate)

    private var localBookTreeSelectListener: ((Boolean) -> Unit)? = null
    private val searchView: SearchView by lazy {
        binding.titleBar.findViewById(R.id.search_view)
    }

    private val localBookTreeSelect = registerForActivityResult(HandleFileContract()) {
        it.uri?.let { treeUri ->
            AppConfig.defaultBookTreeUri = treeUri.toString()
            localBookTreeSelectListener?.invoke(true)
        } ?: localBookTreeSelectListener?.invoke(false)
    }

    /**
     * 设置书籍保存位置
     */
    protected suspend fun setBookStorage() = suspendCoroutine { block ->
        localBookTreeSelectListener = {
            block.resume(it)
        }
        //测试书籍保存位置是否设置
        if (!AppConfig.defaultBookTreeUri.isNullOrBlank()) {
            block.resume(true)
            return@suspendCoroutine
        }
        //测试读写??
        val storageHelp = String(assets.open("storageHelp.md").readBytes())
        val hint = getString(R.string.select_book_folder)
        alert(hint, storageHelp) {
            yesButton {
                localBookTreeSelect.launch {
                    title = hint
                }
            }
            noButton {
                block.resume(false)
            }
            onCancelled {
                block.resume(false)
            }
        }
    }

    abstract fun onSearchTextChange(newText: String?)

    private fun initSearchView() {
        searchView.applyTint(primaryTextColor)
        searchView.onActionViewExpanded()
        searchView.isSubmitButtonEnabled = true
        searchView.queryHint = getString(R.string.screen)
        searchView.clearFocus()
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                onSearchTextChange(newText)
                return false
            }
        })
    }

}