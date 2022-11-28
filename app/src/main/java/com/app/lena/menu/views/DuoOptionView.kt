package com.app.lena.menu.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.app.lena.R


class DuoOptionView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    RelativeLayout(context, attrs, defStyleAttr) {
    private var mOptionViewHolder: OptionViewHolder? = null
    private var mIsSideSelectorEnabled = false
    private var mIsSelectorEnabled = false

    init {
        initialize()
    }

    private fun initialize() {
        val rootView = inflate(context, R.layout.duo_view_option, this) as ViewGroup
        mOptionViewHolder = OptionViewHolder(rootView)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        isSelected = isSelected
    }
    /**
     * Check if the side selector is enabled or not.
     *
     * @return True if the side selector is enabled.
     */
    /**
     * Set the option view side selector enabled.
     * By default a red rectangle in front of the option text and when enabled
     * in front of the selector.
     *
     * @param sideSelectorEnabled Either true or false. Enabling/disabling the side selector.
     */
    var isSideSelectorEnabled: Boolean
        get() = mIsSideSelectorEnabled
        set(sideSelectorEnabled) {
            mIsSideSelectorEnabled = sideSelectorEnabled
            invalidate()
            requestLayout()
        }
    /**
     * Check if the selector is enabled or not.
     *
     * @return True if the selector is enabled.
     */
    /**
     * Set the option view selector enabled.
     * By default a little white circle in front of the option text.
     *
     * @param selectorEnabled Either true or false. Enabling/disabling the selector.
     */
    var isSelectorEnabled: Boolean
        get() = mIsSelectorEnabled
        set(selectorEnabled) {
            mIsSelectorEnabled = selectorEnabled
            invalidate()
            requestLayout()
        }

    /**
     * Set the option view as selected.
     * Using the wishes of the programmer.
     * By default only makes the option text white.
     *
     * @param selected Either true or false. Setting the option view as selected/unselected.
     */
    override fun setSelected(selected: Boolean) {
        if (selected) {
            mOptionViewHolder!!.mTextViewOption.alpha = ALPHA_CHECKED
            if (mIsSelectorEnabled) {
                mOptionViewHolder!!.mImageViewSelector.visibility = VISIBLE
                mOptionViewHolder!!.mImageViewSelector.alpha = ALPHA_CHECKED
            } else {
                mOptionViewHolder!!.mImageViewSelector.visibility = GONE
            }
            if (mIsSideSelectorEnabled) {
                mOptionViewHolder!!.mImageViewSelectorSide.visibility = VISIBLE
            }
        } else {
            mOptionViewHolder!!.mTextViewOption.alpha = ALPHA_UNCHECKED
            if (mIsSelectorEnabled) {
                mOptionViewHolder!!.mImageViewSelector.visibility = VISIBLE
                mOptionViewHolder!!.mImageViewSelector.alpha = ALPHA_UNCHECKED
            } else {
                mOptionViewHolder!!.mImageViewSelector.visibility = GONE
            }
            if (mIsSideSelectorEnabled) {
                mOptionViewHolder!!.mImageViewSelectorSide.visibility = GONE
            }
        }
    }

    /**
     * Check if the option view is selected or not.
     *
     * @return True if the option view is selected.
     */
    override fun isSelected(): Boolean {
        return mOptionViewHolder!!.mTextViewOption.alpha == ALPHA_CHECKED
    }

    /**
     * Binds the option view with it's content
     *
     * @param optionText Text to show as option in the menu.
     */
    /*fun bind(optionText: String?) {
        mOptionViewHolder!!.mTextViewOption.text = optionText
        Log.e("Rshjbshjfs",optionText.toString() +"   counter"+ count)
        mOptionViewHolder!!.mTextViewOption.alpha = ALPHA_UNCHECKED
        mOptionViewHolder!!.mImageViewSelector.visibility = GONE
    }*/

    /**
     * Binds the option view with it's content
     *
     * @param optionText       Text to show as option in the menu.
     * @param selectorDrawable Selector to show when option is selected.
     * Set to "null" to use it's default.
     * By default it shows a white circle.
     */
   /* fun bind(optionText: String?, selectorDrawable: Drawable?) {
        mOptionViewHolder!!.mTextViewOption.text = optionText
        mOptionViewHolder!!.mTextViewOption.alpha = ALPHA_UNCHECKED
        if (selectorDrawable != null) {
            mOptionViewHolder!!.mImageViewSelector.setImageDrawable(selectorDrawable)
        }
        mOptionViewHolder!!.mImageViewSelector.alpha = ALPHA_UNCHECKED
        isSelectorEnabled = true
    }*/

    /**
     * Binds the option view with it's content
     *
     * @param optionText           Text to show as option in the menu.
     * @param selectorDrawable     Selector to show when option is selected.
     * Set to "null" to use it's default.
     * By default it shows a white circle.
     * @param selectorSideDrawable Side selector to show when option is selected.
     * Set to "null" to use it's default.
     * By default it shows a red rectangle.
     */
    fun bind(position:Int, optionText: String?, selectorDrawable: Drawable?, selectorSideDrawable: Drawable?) {
        var myImageList = intArrayOf(
            R.drawable.ic_home,
            R.drawable.ic_categories,
            R.drawable.ic_find_store,
            R.drawable.ic_cart,
            R.drawable.ic_profile,
            R.drawable.ic_wishlist,
            R.drawable.ic_logout)

        mOptionViewHolder!!.mTextViewOption.text = optionText
        mOptionViewHolder!!.mImageViewSelector.setImageResource(myImageList[position])
        mOptionViewHolder!!.mTextViewOption.alpha = ALPHA_UNCHECKED
        if (selectorDrawable != null) {
            mOptionViewHolder!!.mImageViewSelector.setImageDrawable(selectorDrawable)
        }
        mOptionViewHolder!!.mImageViewSelector.alpha = ALPHA_UNCHECKED
        if (selectorSideDrawable != null) {
            mOptionViewHolder!!.mImageViewSelectorSide.setImageDrawable(selectorSideDrawable)
        }
        isSelectorEnabled = true
        isSideSelectorEnabled = true
    }

    /**
     * View holder that holds the views for this layout.
     */
    private inner class OptionViewHolder internal constructor(rootView: ViewGroup) {
        val mTextViewOption: TextView
        val mImageViewSelector: ImageView
        val mImageViewSelectorSide: ImageView

        init {
            mTextViewOption = rootView.findViewById<View>(R.id.duo_view_option_text) as TextView
            mImageViewSelector =
                rootView.findViewById<View>(R.id.duo_view_option_selector) as ImageView
            mImageViewSelectorSide =
                rootView.findViewById<View>(R.id.duo_view_option_selector_side) as ImageView
            hideSelectorsByDefault()
        }

        /**
         * By default both selectors are disabled.
         */
        private fun hideSelectorsByDefault() {
            mImageViewSelector.visibility = INVISIBLE
            mImageViewSelectorSide.visibility = GONE
        }
    }

    companion object {
        private const val ALPHA_CHECKED = 1f
        private const val ALPHA_UNCHECKED = 0.5f
    }
}