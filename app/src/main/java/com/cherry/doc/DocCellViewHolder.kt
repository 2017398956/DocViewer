package com.cherry.doc

import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.cherry.doc.data.DocInfo
import com.cherry.lib.doc.bean.FileType
import com.cherry.lib.doc.util.FileUtils
import java.io.File

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

class DocCellViewHolder(itemView: View, groupPosition: Int) : RecyclerView.ViewHolder(itemView),OnClickListener {
    var mOnItemClickListener: OnItemClickListener? = null
    var parentPosition: Int = groupPosition
    private var mCvDocCell:CardView
    private var mIvType:ImageView
    private var mTvFileName:TextView
    private var mTvFileDes:TextView

    init {
        itemView.setOnClickListener(this)
        mCvDocCell = itemView.findViewById(R.id.mCvDocCell)
        mIvType = itemView.findViewById(R.id.mIvType)
        mTvFileName = itemView.findViewById(R.id.mTvFileName)
        mTvFileDes = itemView.findViewById(R.id.mTvFileDes)
    }

    fun bindData(data: DocInfo?) {
        val typeIcon = data?.getTypeIcon() ?: -1
        if (typeIcon == -1) {
            val file = File(data?.path)
            if (file.exists()) {
                mIvType.load(File(data?.path))
            } else {
                mIvType.load(com.cherry.lib.doc.R.drawable.all_doc_ic)
            }
        } else {
            mIvType.load(typeIcon)
        }
        mTvFileName.text = data?.fileName
        mTvFileDes.text = "${data?.getFileType()} | ${data?.fileSize}\n${data?.lastModified}"

        val type = FileUtils.getFileTypeForUrl(data?.path)
        when (type) {
            FileType.PDF -> {
                mCvDocCell.setCardBackgroundColor(
                    ResourcesCompat.getColor(
                        mCvDocCell.resources,
                        R.color.listItemColorPdf,
                        mCvDocCell.context.theme
                    )
                )
            }
            FileType.DOC,FileType.DOCX -> {
                mCvDocCell.setCardBackgroundColor(
                    ResourcesCompat.getColor(
                        mCvDocCell.resources,
                        R.color.listItemColorDoc,
                        mCvDocCell.context.theme
                    )
                )
            }
            FileType.XLS,FileType.XLSX -> {
                mCvDocCell.setCardBackgroundColor(
                    ResourcesCompat.getColor(
                        mCvDocCell.resources,
                        R.color.listItemColorExcel,
                        mCvDocCell.context.theme
                    )
                )
            }
            FileType.PPT,FileType.PPTX -> {
                mCvDocCell.setCardBackgroundColor(
                    ResourcesCompat.getColor(
                        mCvDocCell.resources,
                        R.color.listItemColorPPT,
                        mCvDocCell.context.theme
                    )
                )
            }
            FileType.IMAGE -> {
                mCvDocCell.setCardBackgroundColor(
                    ResourcesCompat.getColor(
                        mCvDocCell.resources,
                        R.color.listItemColorImage,
                        mCvDocCell.context.theme
                    )
                )
            }
        }
    }

    override fun onClick(v: View?) {
        mOnItemClickListener?.onItemClick(null,v,adapterPosition,parentPosition.toLong())
    }

}