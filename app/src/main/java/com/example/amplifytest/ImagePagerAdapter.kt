package com.example.amplifytest

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.TransformationUtils.circleCrop

class ImagePagerAdapter(
    private val imageList: MutableList<String>,
    private val context: Context,
    private val onClick: () -> Unit
) : PagerAdapter() {

    private val layoutInflater: LayoutInflater by lazy { context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater }

    override fun getCount(): Int {
        return imageList.size
    }

    override fun isViewFromObject(view: View, parent: Any): Boolean {
        return view == parent as ConstraintLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemView = layoutInflater.inflate(R.layout.image_pager_item, container, false)
        val imageView = itemView.findViewById<ImageView>(R.id.image_view)
        imageView.setOnClickListener {
            onClick.invoke()
        }
        try {
            Glide.with(context)
                .load(imageList[position])
                .error(R.drawable.ic_launcher_foreground)
                .into(imageView)
            container.addView(itemView)
        } catch (e: Exception) {
        }
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
        (container as ViewPager).removeView(view as ConstraintLayout)
    }
}