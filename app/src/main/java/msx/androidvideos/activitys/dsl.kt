package msx.androidvideos.activitys

import android.app.Activity
import android.support.v4.util.ArrayMap
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlin.properties.Delegates

/**
 * Created by xiaowei on 2017/11/23.
 */

fun <T : View> Activity.getView(id: Int): T {
    return findViewById(id) as T
}

class DSLViewHolder : RecyclerView.ViewHolder {
    constructor(itemView: View?) : super(itemView)

    var maps: ArrayMap<Int, View> = ArrayMap()

    open fun <T : View> getView(id: Int): T {
        var tmp = maps.get(id)
        if (tmp == null) {
            tmp = itemView.findViewById(id)
            maps.put(id, tmp)
        }
        return tmp as T
    }

}

abstract class RecyclerAdapter : RecyclerView.Adapter<DSLViewHolder> {
    constructor() : super()
}

abstract class SimpleRecyclerAdapter<T> : RecyclerAdapter {

    open var data: List<T> by Delegates.observable(ArrayList()) {
        _, _, _ ->
        notifyDataSetChanged()
    }

    constructor(data: List<T>) : super() {
        this.data = data
    }

    constructor(vararg data: T) : super() {
        this.data = ArrayList()
        if (data != null && data.isNotEmpty())
            this.data = this.data.plus(data)
    }

    abstract fun getViewLayout(viewType: Int, parent: ViewGroup?): View

    override final fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): DSLViewHolder {
        return DSLViewHolder(getViewLayout(viewType, parent))
    }

    override fun getItemCount(): Int {
        return data.size
    }
}