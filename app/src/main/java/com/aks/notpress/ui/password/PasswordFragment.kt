package com.aks.notpress.ui.password

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.aks.notpress.R
import com.aks.notpress.databinding.FragmentPasswordBinding
import com.aks.notpress.utils.FragmentUtil
import com.aks.notpress.utils.PasswordEnterViewModel
import com.aks.notpress.utils.PreferencesBasket

class PasswordFragment: Fragment(){
    private val fragmentUtil = FragmentUtil()
    private lateinit var factory: PasswordFactory

    private lateinit var viewModel: PasswordViewModel
    private lateinit var binding: FragmentPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        factory = PasswordFactory(PreferencesBasket(context?:return))
        viewModel = ViewModelProvider(this, factory).get(PasswordViewModelImpl::class.java)
        fragmentUtil.observe(this, viewModel, activity?.supportFragmentManager)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPasswordBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        binding.itemsPassword.drawerView.setViewModel(viewModel)
        binding.setLifecycleOwner(this)
        getPosition()
        return binding.root
    }

    private fun getPosition(){
        binding.itemsPassword.container.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener{
        override fun onGlobalLayout() {
            val list = listOf(binding.itemsPassword.iv1.getSizeXY(),
                binding.itemsPassword.iv2.getSizeXY(),
                binding.itemsPassword.iv3.getSizeXY(),
                binding.itemsPassword.iv4.getSizeXY(),
                binding.itemsPassword.iv5.getSizeXY(),
                binding.itemsPassword.iv6.getSizeXY(),
                binding.itemsPassword.iv7.getSizeXY(),
                binding.itemsPassword.iv8.getSizeXY(),
                binding.itemsPassword.iv9.getSizeXY())
            binding.itemsPassword.drawerView.setPositions(list)
            binding.itemsPassword.container.viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    })
}
    private fun View.getSizeXY() = Password(this.x + this.width / 2, this.y + this.height / 2)

    companion object {
        fun newInstance() = PasswordFragment()
    }
}

open class Password {
    constructor()
    constructor(x: Float, y: Float){
        this.x = x
        this.y = y
    }
    var x: Float = 0f
    var y: Float = 0f
    val isTouch: MutableLiveData<Boolean> = MutableLiveData(false)
}

open class IntFloat(startX: Float, startY: Float, stopX: Float, stopY: Float) {
    var startX: Float? = startX
    var startY: Float? = startY
    var stopX: Float? = stopX
    var stopY: Float? = stopY
}


class DrawingView: View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private val PASSWORD_SIZE = 3
    private val tag = "DrawingView"

    private lateinit var viewModel: PasswordEnterViewModel
    private lateinit var positions: List<Password>

    private val scope = context.resources.getDimension(R.dimen.margin_5) * (resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)
    private var pen: Paint = Paint()


    init{
        pen.color = ContextCompat.getColor(context, R.color.colorGreen)
        pen.strokeWidth = 10f
    }

    private var mPatches: ArrayList<IntFloat> = ArrayList()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mPatches.map {
            Log.d(tag, "paint ${it.startX}, ${it.startY}, ${it.stopX}, ${it.stopY} ")
            canvas.drawLine(it.startX!!,it.startY!!,it.stopX!!, it.stopY!!, pen)
        }
    }

    private fun actionUp(){
        mPatches = ArrayList()
        positions.map { it.isTouch.postValue(false) }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event==null) return true


        when (event.action) {
            MotionEvent.ACTION_DOWN ->{
                positions.find { it.x - scope < event.x && it.x + scope > event.x && it.y - scope < event.y && it.y + scope > event.y }
                    ?.let {
                        mPatches.add(IntFloat(it.x,it.y, event.x,event.y))
                        it.isTouch.postValue(true)
                        invalidate()
                    }
            }

            MotionEvent.ACTION_MOVE -> {
                positions.find { it.x - scope < event.x && it.x + scope > event.x && it.y - scope < event.y && it.y + scope > event.y }
                    ?.let {
                        if (!it.isTouch.value!!) {
                            mPatches.lastOrNull()?.stopX = it.x
                            mPatches.lastOrNull()?.stopY = it.y
                            it.isTouch.value = true
                            findIntersections()
                            mPatches.add(IntFloat(it.x,it.y, event.x,event.y))
                        }
                        invalidate()
                    }
                mPatches.lastOrNull()?.stopX = event.x
                mPatches.lastOrNull()?.stopY = event.y
                invalidate()
            }

            MotionEvent.ACTION_UP ->{
                if (positions.filter { it.isTouch.value!! }.size > PASSWORD_SIZE)
                    viewModel.onCheck(positions.map { it.isTouch.value!! })
                actionUp()
                invalidate()
            }
        }
        return true
    }

    private fun findIntersections(){
        mPatches.forEach {patch ->
            if (patch.startX!=null && patch.stopX!=null && patch.startY!=null && patch.stopY!=null) {
                positions.filter { !it.isTouch.value!! }.let {
                    it.let {
                        when {
                            patch.startX == patch.stopX -> {
                                it.filter { it.x == patch.startX }
                                    .firstOrNull {
                                        if (patch.startY!! <= patch.stopY!!) it.y in patch.startY!!..patch.stopY!!
                                        else it.y in patch.stopY!!..patch.startY!!
                                    }
                            }
                            patch.startY == patch.stopY -> {
                                it.filter { it.y == patch.startY }
                                    .firstOrNull {
                                        if (patch.startX!! <= patch.stopX!!) it.x in patch.startX!!..patch.stopX!!
                                        else it.x in patch.stopX!!..patch.startX!!
                                    }
                            }
                            patch.startX != patch.stopX && patch.startY != patch.stopY -> {
                                it.filter { it.y == it.x }
                                    .firstOrNull {
                                        if (patch.startX!! <= patch.stopX!! && patch.startY!! <= patch.stopY!!)
                                            it.x in patch.startX!!..patch.stopX!! && it.y in patch.startY!!..patch.stopY!!
                                        else it.x in patch.stopX!!..patch.startX!! && it.y in patch.stopY!!..patch.startY!!
                                    }
                            }
                            else -> null
                        }
                    }?.isTouch?.value = true
                }
            }
        }
    }

    fun setViewModel(viewModel: PasswordEnterViewModel){ this.viewModel = viewModel }
    fun setPositions(positions: List<Password>){
        this.viewModel.initPasswordList(positions)
        this.positions = positions
    }
}