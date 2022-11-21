package com.app.lena

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager

/**
 * A simple [Fragment] subclass.
 */
class MainFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        view.findViewById<View>(R.id.button).setOnClickListener {
            Toast.makeText(
                context,
                "Clicked a button!",
                Toast.LENGTH_SHORT
            ).show()
        }
        val pager = view.findViewById<View>(R.id.pager) as ViewPager
        pager.adapter = PagerAdapter()
        return view
    }

    private inner class PagerAdapter : androidx.viewpager.widget.PagerAdapter() {
        override fun getCount(): Int {
            return 6
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {

            // Create some layout params
            val layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)

            // Create some text
            val textView = getTextView(container.context)
            textView.text = position.toString()
            textView.layoutParams = layoutParams
            val layout = RelativeLayout(container.context)
            layout.setBackgroundColor(
                ContextCompat.getColor(
                    container.context,
                    R.color.colorPrimary
                )
            )
            layout.layoutParams = layoutParams
            layout.addView(textView)
            container.addView(layout)
            return layout
        }

        private fun getTextView(context: Context): TextView {
            val textView = TextView(context)
            textView.setTextColor(Color.WHITE)
            textView.textSize = 30f
            textView.typeface = Typeface.DEFAULT_BOLD
            return textView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as RelativeLayout)
        }
    }
}