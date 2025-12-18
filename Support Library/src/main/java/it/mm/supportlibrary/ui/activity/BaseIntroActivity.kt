package it.mm.supportlibrary.ui.activity

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import it.mm.supportlibrary.R
import it.mm.supportlibrary.ui.adapter.IntroPagerAdapter

abstract class BaseIntroActivity(
    @LayoutRes private val layoutRes: Int = R.layout.activity_intro_base
) : AppCompatActivity() {

    protected lateinit var pager: ViewPager2
    protected lateinit var indicator: TabLayout
    protected lateinit var adapter: IntroPagerAdapter

    lateinit var btnPrev: MaterialButton
    lateinit var btnSkip: MaterialButton
    lateinit var btnClose: MaterialButton
    lateinit var btnNext: MaterialButton

    private var mediator: TabLayoutMediator? = null

    /** Le activity figlie riempiono qui i fragment */
    protected abstract fun buildPages(): List<Fragment>

    private val pageCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            val lastIndex = adapter.itemCount - 1
            val isLast = (position == lastIndex) && lastIndex >= 0
            onLastPageChanged(isLast, position, lastIndex)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutRes)

        pager = findViewById(R.id.pager)
        indicator = findViewById(R.id.pageIndicator)

        btnPrev = findViewById(R.id.btn_prev)
        btnSkip = findViewById(R.id.btn_skip)
        btnClose = findViewById(R.id.btn_close)
        btnNext = findViewById(R.id.btn_next)

        btnPrev.setOnClickListener { goToPrevPage() }
        btnNext.setOnClickListener { goToNextPage() }
        btnSkip.setOnClickListener { finish() }
        btnClose.setOnClickListener { finish() }

        adapter = IntroPagerAdapter(this)
        pager.adapter = adapter

        // Config opzionale
        pager.offscreenPageLimit = 1

        // Carico pagine definite dalla child
        val pages = buildPages()
        adapter.setPages(pages)

        // Aggancio i pallini
        attachIndicator()

        pager.registerOnPageChangeCallback(pageCallback)
    }

    override fun onDestroy() {
        pager.unregisterOnPageChangeCallback(pageCallback)
        super.onDestroy()
    }

    protected open fun onLastPageChanged(isLast: Boolean, position: Int, lastIndex: Int) {}

    protected fun goToNextPage(smoothScroll: Boolean = true) {
        val next = pager.currentItem + 1
        if (next < adapter.itemCount) {
            pager.setCurrentItem(next, smoothScroll)
        }
    }

    protected fun goToPrevPage(smoothScroll: Boolean = true) {
        val prev = pager.currentItem - 1
        if (prev >= 0) {
            pager.setCurrentItem(prev, smoothScroll)
        }
    }

    protected fun addPage(fragment: Fragment) {
        adapter.addPage(fragment)
        refreshIndicator()
    }

    protected fun setPages(fragments: List<Fragment>) {
        adapter.setPages(fragments)
        refreshIndicator()
    }

    private fun attachIndicator() {
        mediator?.detach()
        mediator = TabLayoutMediator(indicator, pager) { tab, _ ->
            tab.setIcon(R.drawable.intro_dot)
        }.also { it.attach() }
    }

    private fun refreshIndicator() {
        // dopo notifyDataSetChanged, ri-attacchiamo per sincronizzare i dot
        attachIndicator()
    }
}
