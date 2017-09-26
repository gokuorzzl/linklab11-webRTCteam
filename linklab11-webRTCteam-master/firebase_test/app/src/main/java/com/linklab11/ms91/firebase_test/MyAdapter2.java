package com.linklab11.ms91.firebase_test;

/**
 * Created by User on 2017-07-24.
 */
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class MyAdapter2 extends RecyclerView.Adapter<MyAdapter2.ViewHolder> {
    List<AlertMessage> mAlertMessage;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView2;
        public ViewHolder(View itemView) {
            super(itemView);
            mTextView2 = (TextView) itemView.findViewById(R.id.mTextView2);
        }
    }

    public MyAdapter2(List<AlertMessage> mAlertMessage) {
        this.mAlertMessage = mAlertMessage;
    }

    @Override
    public MyAdapter2.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_text_view2, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mTextView2.setText(mAlertMessage.get(position).getText()); // position 설정을 위해서는 getter 설정이 필요 (마우스 우클릭 -> Generate)
    }

    @Override
    public int getItemCount() {
        return mAlertMessage.size();
    }

}
