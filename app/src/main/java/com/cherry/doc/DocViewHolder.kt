package com.cherry.doc

import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView.OnItemClickListener
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cherry.doc.data.DocGroupInfo

/*
 * -----------------------------------------------------------------
 * Copyright (C) 2018-2028, by Victor, All rights reserved.
 * -----------------------------------------------------------------
 * File: DocViewHolder
 * Author: Victor
 * Date: 2023/10/26 10:57
 * Description: 
 * -----------------------------------------------------------------
 */

class DocViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), OnClickListener {
    var mOnItemClickListener: OnItemClickListener? = null
    private var mTvTypeName: TextView
    private var mRvDocCell: RecyclerView

    init {
        itemView.setOnClickListener(this)
        mTvTypeName = itemView.findViewById(R.id.mTvTypeName)
        mRvDocCell = itemView.findViewById(R.id.mRvDocCell)
    }

    fun bindData(data: DocGroupInfo?) {
        mTvTypeName.text = data?.typeName
//        itemView.mRvDocCell.onFlingListener = null
//        LinearSnapHelper().attachToRecyclerView(itemView.mRvDocCell)
        val cellAdapter = DocCellAdapter(
            itemView.context, mOnItemClickListener,
            adapterPosition
        )
        cellAdapter.showDatas(data?.docList)
        mRvDocCell.adapter = cellAdapter
    }

    override fun onClick(v: View?) {
        mOnItemClickListener?.onItemClick(null, v, adapterPosition, 0)
    }

}