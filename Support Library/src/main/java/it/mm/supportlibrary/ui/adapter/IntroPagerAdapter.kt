package it.mm.supportlibrary.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class IntroPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val pages = mutableListOf<Fragment>()

    override fun getItemCount(): Int = pages.size
    override fun createFragment(position: Int): Fragment = pages[position]

    fun setPages(newPages: List<Fragment>) {
        pages.clear()
        pages.addAll(newPages)
        notifyDataSetChanged()
    }

    fun addPage(fragment: Fragment) {
        pages.add(fragment)
        notifyDataSetChanged()
    }

    fun removePage(index: Int) {
        if (index in pages.indices) {
            pages.removeAt(index)
            notifyDataSetChanged()
        }
    }

    fun getPagesCount(): Int = pages.size
}