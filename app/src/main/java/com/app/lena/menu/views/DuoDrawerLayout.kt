package com.app.lena.menu.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.*
import android.widget.RelativeLayout
import androidx.annotation.IntDef
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import com.app.lena.R
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

class DuoDrawerLayout @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) :
    RelativeLayout(context, attrs, defStyle) {
    private var mContentScaleClosed = CONTENT_SCALE_CLOSED
    private var mContentScaleOpen = CONTENT_SCALE_OPEN
    private var mMenuScaleClosed = MENU_SCALE_CLOSED
    private var mMenuScaleOpen = MENU_SCALE_OPEN
    private var mMenuAlphaClosed = MENU_ALPHA_CLOSED
    private var mMenuAlphaOpen = MENU_ALPHA_OPEN
    private var mMarginFactor = MARGIN_FACTOR
    private var mClickToCloseScale = CLICK_TO_CLOSE_SCALE
    private var mDragOffset = 0f
    private var mDraggedXOffset = 0f
    private var mDraggedYOffset = 0f

    @LockMode
    private var mLockMode = 0

    @State
    private var mDrawerState = STATE_IDLE

    @LayoutRes
    private var mMenuViewId = 0

    @LayoutRes
    private var mContentViewId = 0
    private var mViewDragHelper: ViewDragHelper? = null
    private var mLayoutInflater: LayoutInflater? = null
    private var mDrawerListener: DrawerListener? = null
    private var mViewDragCallback: ViewDragCallback? = null
    private var mContentView: View? = null
    private var mMenuView: View? = null

    init {
        readAttributes(attrs)
        initialize()
    }

    private fun readAttributes(attributeSet: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.DuoDrawerLayout)
        try {
            mMenuViewId =
                typedArray.getResourceId(R.styleable.DuoDrawerLayout_menu, DEFAULT_ATTRIBUTE_VALUE)
            mContentViewId = typedArray.getResourceId(
                R.styleable.DuoDrawerLayout_content,
                DEFAULT_ATTRIBUTE_VALUE
            )
            mContentScaleClosed = typedArray.getFloat(
                R.styleable.DuoDrawerLayout_contentScaleClosed,
                CONTENT_SCALE_CLOSED
            )
            mContentScaleOpen = typedArray.getFloat(
                R.styleable.DuoDrawerLayout_contentScaleOpen,
                CONTENT_SCALE_OPEN
            )
            mMenuScaleClosed =
                typedArray.getFloat(R.styleable.DuoDrawerLayout_menuScaleClosed, MENU_SCALE_CLOSED)
            mMenuScaleOpen =
                typedArray.getFloat(R.styleable.DuoDrawerLayout_menuScaleOpen, MENU_SCALE_OPEN)
            mMenuAlphaClosed =
                typedArray.getFloat(R.styleable.DuoDrawerLayout_menuAlphaClosed, MENU_ALPHA_CLOSED)
            mMenuAlphaOpen =
                typedArray.getFloat(R.styleable.DuoDrawerLayout_menuAlphaOpen, MENU_ALPHA_OPEN)
            mMarginFactor =
                typedArray.getFloat(R.styleable.DuoDrawerLayout_marginFactor, MARGIN_FACTOR)
            mClickToCloseScale = typedArray.getFloat(
                R.styleable.DuoDrawerLayout_clickToCloseScale,
                CLICK_TO_CLOSE_SCALE
            )
        } finally {
            typedArray.recycle()
        }
    }

    private fun initialize() {
        mLayoutInflater = LayoutInflater.from(context)
        mViewDragCallback = ViewDragCallback()
        mViewDragHelper = ViewDragHelper.create(this, 1.0f, mViewDragCallback!!)
        mViewDragHelper!!.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT)
        this.isFocusableInTouchMode = true
        this.clipChildren = false
        this.requestFocus()
    }

    private fun map(x: Float, inMin: Float, inMax: Float, outMin: Float, outMax: Float): Float {
        return (x - inMin) * (outMax - outMin) / (inMax.toInt() - inMin) + outMin
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        handleViews()
        mContentView!!.offsetLeftAndRight(mDraggedXOffset.toInt())
        mContentView!!.offsetTopAndBottom(mDraggedYOffset.toInt())
    }

    /**
     * Checks if it can find the menu & content views with their tags.
     * If this fails it will check for the corresponding attribute.
     * If this fails it wil throw an IllegalStateException.
     */
    private fun handleViews() {
        for (i in 0 until childCount) {
            val view = getChildAt(i)
            try {
                val tag = view.tag as String
                if (tag == TAG_CONTENT) {
                    mContentView = view
                } else if (tag == TAG_MENU) {
                    mMenuView = view
                }
            } catch (ignored: Exception) {
            }
            if (mContentView != null && mMenuView != null) break
        }
        if (mMenuView == null) {
            checkForMenuAttribute()
        }
        if (mContentView == null) {
            checkForContentAttribute()
        }
        if (mDragOffset == 0f) {
            setViewAndChildrenEnabled(mContentView, true)
            setViewAndChildrenEnabled(mMenuView, false)
        }
    }

    /**
     * Checks if it can inflate the menu view with its corresponding attribute.
     * If this fails it wil throw an IllegalStateException.
     */
    private fun checkForMenuAttribute() {
        if (mMenuViewId == DEFAULT_ATTRIBUTE_VALUE) {
            throw IllegalStateException(
                "Missing menu layout. " +
                        "Set a \"menu\" tag on the menu layout (in XML android:xml=\"menu\"). " +
                        "Or set the \"app:menu\" attribute on the drawer layout."
            )
        }
        mMenuView = mLayoutInflater!!.inflate(mMenuViewId, this, false)
        if (mMenuView != null) {
            mMenuView!!.tag = TAG_MENU
            addView(mMenuView)
        }
    }

    /**
     * Checks if it can inflate the content view with its corresponding attribute.
     * If this fails it wil throw an IllegalStateException.
     */
    private fun checkForContentAttribute() {
        if (mContentViewId == DEFAULT_ATTRIBUTE_VALUE) {
            throw IllegalStateException(
                ("Missing content layout. " +
                        "Set a \"content\" tag on the content layout (in XML android:xml=\"content\"). " +
                        "Or set the \"app:content\" attribute on the drawer layout.")
            )
        }
        mContentView = mLayoutInflater!!.inflate(mContentViewId, this, false)
        if (mContentView != null) {
            mContentView!!.tag = TAG_CONTENT
            addView(mContentView)
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())
        bundle.putFloat("dragOffset", mDragOffset)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        var state: Parcelable? = state
        if (state is Bundle) {
            val bundle = state
            state = bundle.getParcelable("superState")
            if (bundle.getFloat("dragOffset", 0f) > .6f) {
                openDrawer()
            }
        }
        super.onRestoreInstanceState(state)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (isDrawerOpen && keyCode == KeyEvent.KEYCODE_BACK) {
            closeDrawer()
            return true
        } else {
            return super.onKeyDown(keyCode, event)
        }
    }

    override fun computeScroll() {
        super.computeScroll()
        if (mViewDragHelper!!.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        } else {
            mDraggedXOffset = mContentView!!.left.toFloat()
            mDraggedYOffset = mContentView!!.top.toFloat()
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val action = MotionEventCompat.getActionMasked(ev)
        when (action) {
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                mViewDragHelper!!.cancel()
                return false
            }
        }
        return mViewDragHelper!!.shouldInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mViewDragHelper!!.processTouchEvent(event)
        return true
    }

    /**
     * Hide the the touch interceptor.
     */
    fun hideTouchInterceptor() {
        if (findViewWithTag<View?>(TAG_OVERLAY) != null) {
            findViewWithTag<View>(TAG_OVERLAY).visibility =
                INVISIBLE
        }
    }

    /**
     * Show the the touch interceptor.
     */
    private fun showTouchInterceptor() {
        if (findViewWithTag<View?>(TAG_OVERLAY) == null) {
            addTouchInterceptor()
        }
        if (mContentView == null) {
            mContentView = findViewWithTag(TAG_CONTENT)
        }
        val offset = map(
            mContentView!!.left.toFloat(),
            0f,
            this@DuoDrawerLayout.width * mMarginFactor,
            0f,
            1f
        )
        val scaleFactorContent = map(offset, 0f, 1f, mContentScaleClosed, mClickToCloseScale)
        val interceptor = findViewWithTag<View>(TAG_OVERLAY)
        if (interceptor != null) {
            interceptor.translationX = mContentView!!.left.toFloat()
            interceptor.translationY = mContentView!!.top.toFloat()
            interceptor.scaleX = scaleFactorContent
            interceptor.scaleY = scaleFactorContent
            interceptor.visibility = VISIBLE
        }
    }

    /**
     * Boolean to check if a touch is a click.
     *
     * @return Returns true if a touch is a click.
     */
    private fun touchIsClick(startX: Float, endX: Float, startY: Float, endY: Float): Boolean {
        val differenceX = Math.abs(startX - endX)
        val differenceY = Math.abs(startY - endY)
        return !(differenceX > MAX_CLICK_RANGE || differenceY > MAX_CLICK_RANGE)
    }

    /**
     * Adds a touch interceptor to the layout when needed.
     * The interceptor wil take care of touch events occurring
     * on the content view when the drawer is open.
     */
    private fun addTouchInterceptor() {
        val touchInterceptor: View = mLayoutInflater!!.inflate(R.layout.duo_overlay, this, false)
        touchInterceptor.tag = TAG_OVERLAY
        touchInterceptor.setOnTouchListener(object : OnTouchListener {
            var startX = 0f
            var startY = 0f
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (mLockMode != LOCK_MODE_LOCKED_OPEN) {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            startX = event.x
                            startY = event.y
                        }
                        MotionEvent.ACTION_UP -> {
                            val endX = event.x
                            val endY = event.y
                            if (touchIsClick(startX, endX, startY, endY)) {
                                closeDrawer()
                            }
                        }
                        MotionEvent.ACTION_MOVE -> {
                            mViewDragCallback!!.mIsEdgeDrag = true
                            val pointerIndex =
                                (event.action and MotionEvent.ACTION_POINTER_INDEX_MASK) shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
                            mViewDragHelper!!.captureChildView((mContentView)!!, pointerIndex)
                        }
                    }
                    return true
                } else return true
            }
        })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            touchInterceptor.translationZ = MAX_ATTRIBUTE_MULTIPLIER
        }
        addView(touchInterceptor)
    }

    /**
     * Disables/Enables a view and all of its child views.
     * Leaves the toolbar enabled at all times.
     *
     * @param view    The view to be disabled/enabled
     * @param enabled True or false, enabled/disabled
     */
    private fun setViewAndChildrenEnabled(view: View?, enabled: Boolean) {
        view!!.isEnabled = enabled
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
     * Kept for compatibility. {@see #isDrawerOpen()}.
     *
     * @param gravity Ignored
     * @return true if the drawer view in in an open state
     */
    fun isDrawerOpen(gravity: Int): Boolean {
        return isDrawerOpen
    }

    /**
     * Check if the drawer view is currently in an open state.
     * To be considered "open" the drawer must have settled into its fully
     * visible state.
     *
     * @return true if the drawer view is in an open state
     */
    val isDrawerOpen: Boolean
        get() = mDragOffset == 1f

    /**
     * Kept for compatibility. {@see #openDrawer()}.
     *
     * @param gravity Ignored
     */
    fun openDrawer(gravity: Int) {
        openDrawer()
    }

    /**
     * Open the drawer animated.
     */
    fun openDrawer() {
        val drawerWidth = (width * mMarginFactor).toInt()
        if (drawerWidth == 0) {
            viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    this@DuoDrawerLayout.viewTreeObserver.removeOnPreDrawListener(this)
                    if (mViewDragHelper!!.smoothSlideViewTo(
                            (mContentView)!!,
                            ((width * mMarginFactor).toInt()),
                            mContentView!!.top
                        )
                    ) {
                        ViewCompat.postInvalidateOnAnimation(this@DuoDrawerLayout)
                    }
                    return false
                }
            })
        } else {
            if (mViewDragHelper!!.smoothSlideViewTo(
                    (mContentView)!!,
                    drawerWidth,
                    mContentView!!.top
                )
            ) {
                ViewCompat.postInvalidateOnAnimation(this@DuoDrawerLayout)
            }
        }
    }

    /**
     * Kept for compatibility. {@see #closeDrawer()}.
     *
     * @param gravity Ignored
     */
    fun closeDrawer(gravity: Int) {
        closeDrawer()
    }

    /**
     * Close the drawer animated.
     */
    fun closeDrawer() {
        if (mContentView == null) {
            mContentView = findViewWithTag(TAG_CONTENT)
        }
        if (mContentView != null) {
            if (mViewDragHelper!!.smoothSlideViewTo(
                    mContentView!!,
                    0 - mContentView!!.paddingLeft,
                    mContentView!!.top
                )
            ) {
                ViewCompat.postInvalidateOnAnimation(this@DuoDrawerLayout)
            }
        }
    }

    /**
     * Kept for compatibility. {@see #isDrawerVisible()}.
     *
     * @param gravity Ignored.
     */
    fun isDrawerVisible(gravity: Int): Boolean {
        return isDrawerVisible
    }

    /**
     * Check if the drawer is visible on the screen.
     *
     * @return true if the drawer is visible.
     */
    val isDrawerVisible: Boolean
        get() = mDragOffset > 0

    /**
     * Enable or disable interaction the drawer.
     *
     *
     * This allows the application to restrict the user's ability to open or close
     * any drawer within this layout. DuloDrawerLayout will still respond to calls to
     * [.openDrawer], [.closeDrawer] and friends if a drawer is locked.
     *
     *
     * Locking drawers open or closed will implicitly open or close
     * any drawers as appropriate.
     *
     * @param lockMode The new lock mode. One of [.LOCK_MODE_UNLOCKED],
     * [.LOCK_MODE_LOCKED_CLOSED] or [.LOCK_MODE_LOCKED_OPEN].
     * @see .LOCK_MODE_UNLOCKED
     *
     * @see .LOCK_MODE_LOCKED_CLOSED
     *
     * @see .LOCK_MODE_LOCKED_OPEN
     */
    fun setDrawerLockMode(@LockMode lockMode: Int) {
        mLockMode = lockMode
        when (lockMode) {
            LOCK_MODE_LOCKED_CLOSED -> {
                mViewDragHelper!!.cancel()
                closeDrawer()
            }
            LOCK_MODE_LOCKED_OPEN -> {
                mViewDragHelper!!.cancel()
                openDrawer()
            }
            LOCK_MODE_UNLOCKED -> {}
        }
    }

    /**
     * Returns the menu view.
     *
     * @return The current menu view.
     */
    val menuView: View?
        get() {
            if (mMenuView == null) {
                mMenuView = findViewWithTag(TAG_MENU)
            }
            return mMenuView
        }

    /**
     * Sets the menu view.
     *
     * @param menuView View that becomes the menu view.
     */
    fun setMenuView(menuView: View) {
        if (menuView.parent != null) {
            throw IllegalStateException("Your menu view already has a parent. Please make sure your menu view does not have a parent.")
        }
        mMenuView = findViewWithTag(TAG_MENU)
        if (mMenuView != null) {
            removeView(mMenuView)
        }
        mMenuView = menuView
        mMenuView!!.tag = TAG_MENU
        addView(mMenuView)
        invalidate()
        requestLayout()
    }

    /**
     * Returns the content view.
     *
     * @return The current content view.
     */
    val contentView: View?
        get() {
            if (mContentView == null) {
                mContentView = findViewWithTag(TAG_CONTENT)
            }
            return mContentView
        }

    /**
     * Sets the content view.
     *
     * @param contentView View that becomes the content view.
     */
    fun setContentView(contentView: View) {
        if (contentView.parent != null) {
            throw IllegalStateException("Your content view already has a parent. Please make sure your content view does not have a parent.")
        }
        mContentView = findViewWithTag(TAG_CONTENT)
        if (mContentView != null) {
            removeView(mContentView)
        }
        mContentView = contentView
        mContentView!!.tag = TAG_CONTENT
        addView(mContentView)
        invalidate()
        requestLayout()
    }

    /**
     * Set the scale of the content when the drawer is closed. 1.0f is the original size.
     *
     * @param contentScaleClosed Scale of the content if the drawer is closed.
     */
    fun setContentScaleClosed(contentScaleClosed: Float) {
        mContentScaleClosed = contentScaleClosed
        invalidate()
        requestLayout()
    }

    /**
     * Set the scale of the content when the drawer is open. 1.0f is the original size.
     *
     * @param contentScaleOpen Scale of the content when the drawer is open.
     */
    fun setContentScaleOpen(contentScaleOpen: Float) {
        mContentScaleOpen = contentScaleOpen
        invalidate()
        requestLayout()
    }

    /**
     * Set the scale of the menu when the drawer is closed. 1.0f is the original size.
     *
     * @param menuScaleClosed Scale of the menu when the drawer is closed.
     */
    fun setMenuScaleClosed(menuScaleClosed: Float) {
        mMenuScaleClosed = menuScaleClosed
        invalidate()
        requestLayout()
    }

    /**
     * Set the scale of the menu when the drawer is open. 1.0f is the original size.
     *
     * @param menuScaleOpen Scale of the menu when the drawer is open.
     */
    fun setMenuScaleOpen(menuScaleOpen: Float) {
        mMenuScaleOpen = menuScaleOpen
        invalidate()
        requestLayout()
    }

    /**
     * Set the scale of the click to close surface when the drawer is open. 0.7f is the original scaling.
     *
     * @param clickToCloseScale Scale of the click to close surface when the drawer is open.
     */
    fun setClickToCloseScale(clickToCloseScale: Float) {
        mClickToCloseScale = clickToCloseScale
        invalidate()
        requestLayout()
    }

    /**
     * Set the alpha of the menu when the drawer is closed.
     * 0.0f is transparent, 1.0f is completely visible.
     *
     * @param menuAlphaClosed Alpha of the menu when the drawer is closed.
     */
    fun setMenuAlphaClosed(menuAlphaClosed: Float) {
        mMenuAlphaClosed = menuAlphaClosed
        invalidate()
        requestLayout()
    }

    /**
     * Set the alpha of the menu when the drawer is open.
     * 0.0f is transparent, 1.0f is completely visible.
     *
     * @param menuAlphaOpen Alpha of the menu when the drawer is open.
     */
    fun setMenuAlphaOpen(menuAlphaOpen: Float) {
        mMenuAlphaOpen = menuAlphaOpen
        invalidate()
        requestLayout()
    }

    /**
     * Set the amount of space of the content visible when the drawer is opened.
     * 1.0f will move the drawer completely of the screen. The default value is 0.7f.
     *
     * @param marginFactor Amount of space of the content when drawer is open.
     */
    fun setMarginFactor(marginFactor: Float) {
        mMarginFactor = marginFactor
        invalidate()
        requestLayout()
    }

    /**
     * Set a listener to be notified of drawer events.
     *
     * @param drawerListener Listener to notify when drawer events occur
     * @see DrawerListener
     */
    fun setDrawerListener(drawerListener: DrawerListener?) {
        mDrawerListener = drawerListener
    }

    private inner class ViewDragCallback() : ViewDragHelper.Callback() {
        var mIsEdgeDrag = false
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return (mLockMode == LOCK_MODE_UNLOCKED || mLockMode == LOCK_MODE_LOCKED_OPEN) && (child === mContentView) && mIsEdgeDrag
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            if (left < 0) return 0
            val width = (width * mMarginFactor).toInt()
            return if (left > width) width else left
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return topInset
        }

        override fun onEdgeDragStarted(edgeFlags: Int, pointerId: Int) {
            mIsEdgeDrag = true
            if (tryCaptureView(
                    (mContentView)!!,
                    pointerId
                ) && edgeFlags == ViewDragHelper.EDGE_LEFT
            ) {
                mViewDragHelper!!.captureChildView((mContentView)!!, pointerId)
            }
        }

        override fun onViewCaptured(capturedChild: View, activePointerId: Int) {
            super.onViewCaptured(capturedChild, activePointerId)
        }

        override fun onEdgeTouched(edgeFlags: Int, pointerId: Int) {
            super.onEdgeTouched(edgeFlags, pointerId)
        }

        override fun getViewHorizontalDragRange(child: View): Int {
            return this@DuoDrawerLayout.measuredWidth
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            super.onViewReleased(releasedChild, xvel, yvel)
            if (xvel > 0 || xvel == 0f && mDragOffset > 0.5f) {
                openDrawer()
            } else {
                closeDrawer()
            }
            mIsEdgeDrag = false
        }

        override fun onViewPositionChanged(
            changedView: View,
            left: Int,
            top: Int,
            dx: Int,
            dy: Int
        ) {
            super.onViewPositionChanged(changedView, left, top, dx, dy)
            mDragOffset =
                map(left.toFloat(), 0f, this@DuoDrawerLayout.width * mMarginFactor, 0f, 1f)
            val scaleFactorContent =
                map(mDragOffset, 0f, 1f, mContentScaleClosed, mContentScaleOpen)
            mContentView!!.scaleX = scaleFactorContent
            mContentView!!.scaleY = scaleFactorContent
            val scaleFactorMenu = map(mDragOffset, 0f, 1f, mMenuScaleClosed, mMenuScaleOpen)
            mMenuView!!.scaleX = scaleFactorMenu
            mMenuView!!.scaleY = scaleFactorMenu
            val alphaValue = map(mDragOffset, 0f, 1f, mMenuAlphaClosed, mMenuAlphaOpen)
            mMenuView!!.alpha = alphaValue
            if (mDrawerListener != null) {
                mDrawerListener!!.onDrawerSlide(this@DuoDrawerLayout, mDragOffset)
            }
        }

        override fun onViewDragStateChanged(state: Int) {
            super.onViewDragStateChanged(state)
            if ((context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                        && mDragOffset >= .6f)
            ) {
                mDragOffset = 1f
            }
            mDraggedXOffset = mContentView!!.left.toFloat()
            mDraggedYOffset = mContentView!!.top.toFloat()
            if (state == STATE_IDLE) {
                if (mDragOffset == 0f) {
                    hideTouchInterceptor()
                    setViewAndChildrenEnabled(mMenuView, false)
                    if (mDrawerListener != null) {
                        mDrawerListener!!.onDrawerClosed(this@DuoDrawerLayout)
                    }
                } else if (mDragOffset == 1f) {
                    showTouchInterceptor()
                    setViewAndChildrenEnabled(mMenuView, true)
                    if (mDrawerListener != null) {
                        mDrawerListener!!.onDrawerOpened(this@DuoDrawerLayout)
                    }
                }
            }
            if (state != mDrawerState) {
                mDrawerState = state
                if (mDrawerListener != null) {
                    mDrawerListener!!.onDrawerStateChanged(state)
                }
            }
        }

        private val topInset: Int
            private get() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return 0
                if (!mContentView!!.fitsSystemWindows) return 0
                var result = 0
                val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
                if (resourceId > 0) {
                    result = resources.getDimensionPixelSize(resourceId)
                }
                return result
            }
    }

    /**
     * @hide
     */
    @IntDef(STATE_IDLE, STATE_DRAGGING, STATE_SETTLING)
    @Retention(RetentionPolicy.SOURCE)
    private annotation class State()

    /**
     * @hide
     */
    @IntDef(LOCK_MODE_UNLOCKED, LOCK_MODE_LOCKED_CLOSED, LOCK_MODE_LOCKED_OPEN)
    @Retention(
        RetentionPolicy.SOURCE
    )
    private annotation class LockMode()
    companion object {
        /**
         * Indicates that any drawers are in an idle, settled state. No animation is in progress.
         */
        const val STATE_IDLE = ViewDragHelper.STATE_IDLE

        /**
         * Indicates that a drawer is currently being dragged by the user.
         */
        const val STATE_DRAGGING = ViewDragHelper.STATE_DRAGGING

        /**
         * Indicates that a drawer is in the process of settling to a final position.
         */
        const val STATE_SETTLING = ViewDragHelper.STATE_SETTLING

        /**
         * The drawer is unlocked.
         */
        const val LOCK_MODE_UNLOCKED = 0

        /**
         * The drawer is locked closed. The user may not open it, though
         * the app may open it programmatically.
         */
        const val LOCK_MODE_LOCKED_CLOSED = 1

        /**
         * The drawer is locked open. The user may not close it, though the app
         * may close it programmatically.
         */
        const val LOCK_MODE_LOCKED_OPEN = 2

        /**
         * Length of time to delay before peeking the drawer.
         */
        private val PEEK_DELAY = 160
        private val TAG_MENU = "menu"
        private val TAG_CONTENT = "content"
        private val TAG_OVERLAY = "overlay"

        @SuppressLint("ResourceType")
        @LayoutRes
        private val DEFAULT_ATTRIBUTE_VALUE = -54321
        private val CONTENT_SCALE_CLOSED = 1.0f
        private val CONTENT_SCALE_OPEN = 0.7f
        private val CLICK_TO_CLOSE_SCALE = 0.7f
        private val MENU_SCALE_CLOSED = 1.1f
        private val MENU_SCALE_OPEN = 1.0f
        private val MENU_ALPHA_CLOSED = 0.0f
        private val MENU_ALPHA_OPEN = 1.0f
        private val MARGIN_FACTOR = 0.7f
        private val MAX_ATTRIBUTE_MULTIPLIER = 100f
        private val MAX_CLICK_RANGE = 300f
    }
}