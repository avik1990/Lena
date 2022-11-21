package com.app.lena.menu.views

import android.annotation.SuppressLint
import android.content.Context
import android.database.DataSetObserver
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.app.lena.R

/**
 * Created by PSD on 13-04-17.
 */
class DuoMenuView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    RelativeLayout(context, attrs, defStyleAttr) {
    @DrawableRes
    private var mBackgroundDrawableId = 0

    @LayoutRes
    private var mHeaderViewId = 0

    @LayoutRes
    private var mFooterViewId = 0
    private var mOnMenuClickListener: OnMenuClickListener? = null
    private var mDataSetObserver: DataSetObserver? = null
    private var mMenuViewHolder: MenuViewHolder? = null
    private var mLayoutInflater: LayoutInflater? = null
    private var mAdapter: Adapter? = null

    init {
        readAttributes(attrs)
        initialize()
    }

    private fun readAttributes(attributeSet: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.DuoMenuView)
        try {
            mBackgroundDrawableId = typedArray.getResourceId(
                R.styleable.DuoMenuView_background,
                DEFAULT_DRAWABLE_ATTRIBUTE_VALUE
            )
            mHeaderViewId = typedArray.getResourceId(
                R.styleable.DuoMenuView_header,
                DEFAULT_LAYOUT_ATTRIBUTE_VALUE
            )
            mFooterViewId = typedArray.getResourceId(
                R.styleable.DuoMenuView_footer,
                DEFAULT_LAYOUT_ATTRIBUTE_VALUE
            )
        } finally {
            typedArray.recycle()
        }
    }

    /**
     * Initialize the menu view.
     */
    private fun initialize() {
        val rootView = inflate(context, R.layout.duo_view_menu, this) as ViewGroup
        mMenuViewHolder = MenuViewHolder(rootView)
        mLayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mDataSetObserver = object : DataSetObserver() {
            override fun onChanged() {
                super.onChanged()
                handleOptions()
                postInvalidate()
                requestLayout()
            }
        }
        handleBackground()
        handleHeader()
        handleFooter()
    }

    /**
     * Handles the background.
     */
    private fun handleBackground() {
        if (mMenuViewHolder!!.mMenuBackground == null) {
            return
        }
        if (mBackgroundDrawableId != DEFAULT_DRAWABLE_ATTRIBUTE_VALUE) {
            val backgroundDrawable = ContextCompat.getDrawable(context, mBackgroundDrawableId)
            if (backgroundDrawable != null) {
                mMenuViewHolder!!.mMenuBackground!!.setImageDrawable(backgroundDrawable)
                return
            }
        }
        mMenuViewHolder!!.mMenuBackground!!.setBackgroundColor(primaryColor)
    }

    /**
     * Handles the header view.
     */
    private fun handleHeader() {
        if (mHeaderViewId == DEFAULT_LAYOUT_ATTRIBUTE_VALUE || mMenuViewHolder!!.mMenuHeader == null) {
            return
        }
        val view = mLayoutInflater!!.inflate(mHeaderViewId, null, false)
        if (view != null) {
            if (mMenuViewHolder!!.mMenuHeader!!.childCount > 0) {
                mMenuViewHolder!!.mMenuHeader!!.removeAllViews()
            }
            mMenuViewHolder!!.mMenuHeader!!.addView(view)
            view.tag = TAG_HEADER
            view.bringToFront()
            view.setOnClickListener {
                if (mOnMenuClickListener != null) {
                    mOnMenuClickListener!!.onHeaderClicked()
                }
            }
        }
    }

    /**
     * Handles the footer view.
     */
    private fun handleFooter() {
        if (mFooterViewId == DEFAULT_LAYOUT_ATTRIBUTE_VALUE || mMenuViewHolder!!.mMenuFooter == null) {
            return
        }
        val view = mLayoutInflater!!.inflate(mFooterViewId, null, false)
        if (view != null) {
            if (mMenuViewHolder!!.mMenuFooter!!.childCount > 0) {
                mMenuViewHolder!!.mMenuFooter!!.removeAllViews()
            }
            mMenuViewHolder!!.mMenuFooter!!.addView(view)
            view.tag = TAG_FOOTER
            view.bringToFront()
            if (view is ViewGroup) {
                val viewGroup = view
                for (i in 0 until viewGroup.childCount) {
                    if (viewGroup.getChildAt(i) is Button) {
                        viewGroup.getChildAt(i).setOnClickListener {
                            if (mOnMenuClickListener != null) {
                                mOnMenuClickListener!!.onFooterClicked()
                            }
                        }
                        return
                    }
                }
            }
        }
    }

    /**
     * Handles the menu options when adapter is set.
     */
    private fun handleOptions() {
        if (mAdapter == null || mAdapter!!.isEmpty || mMenuViewHolder!!.mMenuOptions == null) {
            return
        }
        if (mMenuViewHolder!!.mMenuOptions!!.childCount > 0) {
            mMenuViewHolder!!.mMenuOptions!!.removeAllViews()
        }
        for (i in 0 until mAdapter!!.count) {
            val optionView = mAdapter!!.getView(i, null, this)
            if (optionView != null) {
                mMenuViewHolder!!.mMenuOptions!!.addView(optionView)
                optionView.setOnClickListener {
                    if (mOnMenuClickListener != null) {
                        mOnMenuClickListener!!.onOptionClicked(i, mAdapter!!.getItem(i))
                    }
                }
            }
        }
    }

    /**
     * Gets the primary color of this project.
     *
     * @return primary color of this project.
     */
    private val primaryColor: Int
        private get() {
            val typedArray = context.obtainStyledAttributes(
                TypedValue().data,
                intArrayOf(android.R.attr.colorPrimary)
            )
            val color = typedArray.getColor(0, 0)
            typedArray.recycle()
            return color
        }

    /**
     * Sets the listener for menu clicks.
     *
     * @param onMenuClickListener Listener that registers menu clicks.
     */
    fun setOnMenuClickListener(onMenuClickListener: OnMenuClickListener?) {
        mOnMenuClickListener = onMenuClickListener
    }

    /**
     * Returns the header.
     *
     * @return The current header view.
     */
    val headerView: View
        get() = findViewWithTag(TAG_HEADER)

    /**
     * Sets the header view.
     *
     * @param headerViewId View that becomes the header.
     */
    fun setHeaderView(@LayoutRes headerViewId: Int) {
        mHeaderViewId = headerViewId
        handleHeader()
    }

    /**
     * Returns the footer.
     *
     * @return The current footer view.
     */
    val footerView: View
        get() = findViewWithTag(TAG_FOOTER)

    /**
     * Sets the footer view.
     *
     * @param footerViewId View that becomes the footer.
     */
    fun setFooterView(@LayoutRes footerViewId: Int) {
        mFooterViewId = footerViewId
        handleFooter()
    }

    /**
     * Sets the background drawable of the MenuView.
     *
     * @param backgroundDrawableId Drawable that becomes the background.
     */
    fun setBackground(@DrawableRes backgroundDrawableId: Int) {
        mBackgroundDrawableId = backgroundDrawableId
        handleBackground()
    }
    /**
     * Returns the adapter currently in use.
     *
     * @return The adapter currently used to display data in the Menu.
     */
    /**
     * Sets the data behind this MenuView.
     *
     * @param adapter The Adapter which is responsible for maintaining the
     * data backing this list and for producing a view to represent an
     * item in that data set.
     * @see .getAdapter
     */
    var adapter: Adapter?
        get() = mAdapter
        set(adapter) {
            if (mAdapter != null) mAdapter!!.unregisterDataSetObserver(mDataSetObserver)
            mAdapter = adapter
            mAdapter!!.registerDataSetObserver(mDataSetObserver)
            handleOptions()
        }

    /**
     * Disables/Enables a view and all of its child views.
     * Leaves the toolbar enabled at all times.
     *
     * @param view    The view to be disabled/enabled
     * @param enabled True or false, enabled/disabled
     */
    private fun setViewAndChildrenEnabled(view: View, enabled: Boolean) {
        view.isEnabled = enabled
        if (view is ViewGroup) {
            val viewGroup = view
            for (i in 0 until viewGroup.childCount) {
                val child = viewGroup.getChildAt(i)
                if (child is Toolbar) {
                    setViewAndChildrenEnabled(child, true)
                } else {
                    setViewAndChildrenEnabled(child, enabled)
                }
            }
        }
    }

    /**
     * Holds the views in this menu
     */
    private inner class MenuViewHolder internal constructor(rootView: ViewGroup) {
        val mMenuOptions: LinearLayout?
        val mMenuBackground: ImageView?
        val mMenuHeader: ViewGroup?
        val mMenuFooter: ViewGroup?

        init {
            mMenuOptions =
                rootView.findViewById<View>(R.id.duo_view_menu_options_layout) as LinearLayout
            mMenuBackground =
                rootView.findViewById<View>(R.id.duo_view_menu_background) as ImageView
            mMenuHeader = rootView.findViewById<View>(R.id.duo_view_menu_header_layout) as RelativeLayout
            mMenuFooter = rootView.findViewById<View>(R.id.duo_view_menu_footer_layout) as RelativeLayout
        }
    }

    /**
     * Listener that listens to menu click events.
     */
    interface OnMenuClickListener {
        /**
         * Will be called when user pressed a button in the footer view.
         * Will only be called when footer contains a button.
         */
        fun onFooterClicked()

        /**
         * Will be called when user pressed on the header view.
         */
        fun onHeaderClicked()

        /**
         * Will be called when user pressed an option view.
         */
        fun onOptionClicked(position: Int, objectClicked: Any?)
    }

    companion object {
        private const val TAG_FOOTER = "footer"
        private const val TAG_HEADER = "header"

        @SuppressLint("ResourceType")
        @DrawableRes
        private val DEFAULT_DRAWABLE_ATTRIBUTE_VALUE = -54321

        @SuppressLint("ResourceType")
        @LayoutRes
        private val DEFAULT_LAYOUT_ATTRIBUTE_VALUE = -54320
    }
}