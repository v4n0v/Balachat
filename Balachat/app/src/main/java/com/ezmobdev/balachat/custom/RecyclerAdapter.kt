package com.ezmobdev.balachat.custom

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

@Suppress("UNCHECKED_CAST")
class RecyclerAdapter<T, G : ViewDataBinding>(private var items: ArrayList<T>, var id: Int, private val onItemSelect: OnItemSelect<T, G>?,
                                              private var call: G.(item: T, pos: Int) -> Unit) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder<T, G>>() {
    override fun onBindViewHolder(p0: ViewHolder<T, G>, p1: Int) = p0.bind(items[p1])
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder<T, G> {
        val layoutInflater = LayoutInflater.from(p0.context)
        val binding = DataBindingUtil.inflate<G>(layoutInflater, id, p0, false)
        return ViewHolder(binding, onItemSelect, call, items)
    }

    override fun getItemCount() = items.size
    fun setData(items: ArrayList<T>) {
        this.items = items
        this.notifyDataSetChanged()
    }


    class RecyclerAdapterBuilder<T, G : ViewDataBinding>() {
        private var items: ArrayList<T>? = null
        private var id: Int? = null
        private var onItemSelect: OnItemSelect<T, G>? = null
        private var call: (G.(data: T, pos: Int) -> Unit)? = null
        private var binding: G? = null
        fun setOnItemSelect(onItemSelect: (data: T,  binding: G, pos: Int) -> Unit): RecyclerAdapterBuilder<T, G> {
            val onclick = object : OnItemSelect<T, G> {
                override fun onSelect(data: T,  binding: G,pos: Int) {
                    onItemSelect(data, binding, pos)
                }

            }
            this.onItemSelect = onclick
            return this
        }

        fun setItems(items: ArrayList<T>): RecyclerAdapterBuilder<T, G> {
            this.items = items
            return this
        }

        fun setLauoutId(id: Int): RecyclerAdapterBuilder<T, G> {
            this.id = id
            return this
        }

        fun initBinding(onBind: G.(data: T, pos: Int) -> Unit): RecyclerAdapterBuilder<T, G> {
            this.call = onBind
            return this
        }

        fun build(): RecyclerAdapter<T, G> {
            return RecyclerAdapter(items ?: arrayListOf(), id!!, onItemSelect, call!!)

        }
    }

    class ViewHolder<T, G>(private var binding: ViewDataBinding, private val onItemSelect: OnItemSelect<T, G>?, val call: G.(data: T, pos: Int) -> Unit, var items: ArrayList<T>) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        init {
            binding.root.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            onItemSelect?.onSelect(items[this.layoutPosition], binding as G, this.layoutPosition)
        }

        fun bind(item: T) {
            call(binding as G, item, layoutPosition)
            binding.executePendingBindings()
        }
    }


    interface OnItemSelect<T, G> {
        fun onSelect(data: T, binding: G, pos: Int)
    }
}