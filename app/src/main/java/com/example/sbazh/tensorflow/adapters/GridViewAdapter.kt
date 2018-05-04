package com.example.sbazh.tensorflow.adapters

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import com.example.sbazh.tensorflow.GridItem
import com.example.sbazh.tensorflow.R
import com.squareup.picasso.Picasso


class GridViewAdapter(private val mContext: Context,
                      private val layoutResourceId: Int,
                      private var mGridData: ArrayList<GridItem>) :
        ArrayAdapter<GridItem>(mContext,layoutResourceId,mGridData) {

    fun setGridData(mGridData: ArrayList<GridItem>)
    {
        this.mGridData = mGridData
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var row = convertView
        var holder = ViewHolder()

        if (row == null)
        {
            val inflater = (mContext as Activity).layoutInflater
            row = inflater.inflate(layoutResourceId, parent, false)
            holder.imageView = row.findViewById(R.id.grid_item_image)
            row.tag = holder
        }
        else
            holder = row.tag as ViewHolder

        val item = mGridData[position]
        Picasso.with(mContext).load(item.image).into(holder.imageView)
        return row
    }

    inner class ViewHolder{
        lateinit var imageView: ImageView
    }
}