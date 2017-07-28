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

/**
 * Created by KimMinYoung on 2017-07-23.
 */

public class MyAdapter2_Alert extends RecyclerView.Adapter<MyAdapter2_Alert.ViewHolder> {
    List<AlertMessage> mAlertMessage;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.mTextView);
        }
    }

    public MyAdapter2_Alert(List<AlertMessage> mAlertMessage) {
        this.mAlertMessage = mAlertMessage;
    }

    @Override
    public MyAdapter2_Alert.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_text_view, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mTextView.setText(mAlertMessage.get(position).getText()); // position 설정을 위해서는 getter 설정이 필요 (마우스 우클릭 -> Generate)
    }

    @Override
    public int getItemCount() {
        return mAlertMessage.size();
    }

}
