package com.example.kimminyoung.channeltest;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by KimMinYoung on 2017-07-23.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    List<RecordingMessage> mRecordingMessage;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.mTextView);
        }
    }

    public MyAdapter(List<RecordingMessage> mRecordingMessage) {
        this.mRecordingMessage = mRecordingMessage;
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_text_view, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mTextView.setText(mRecordingMessage.get(position).getText()); // position 설정을 위해서는 getter 설정이 필요 (마우스 우클릭 -> Generate)
    }

    @Override
    public int getItemCount() {
        return mRecordingMessage.size();
    }

}
