package com.cauliflower.danielt.smartphoneradar.ui;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.cauliflower.danielt.smartphoneradar.R;
import com.cauliflower.danielt.smartphoneradar.databinding.TargetTrackedItemBinding;
import com.cauliflower.danielt.smartphoneradar.obj.RadarUser;

import java.util.List;

public class TargetTrackedAdapter extends RecyclerView.Adapter<TargetTrackedAdapter.TrackedItemViewHolder> {
    private List<RadarUser> userList;
    private TargetTrackedClickCallback clickCallback;

    public TargetTrackedAdapter(TargetTrackedClickCallback callback) {
        clickCallback = callback;
    }

    @NonNull
    @Override
    public TrackedItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        TargetTrackedItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(context), R.layout.target_tracked_item, parent, false);
        binding.setClickCallback(clickCallback);
        return new TrackedItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackedItemViewHolder holder, int position) {
        holder.binding.setTargetTracked(userList.get(position));
        holder.binding.executePendingBindings();
    }

    public void setUserList(List<RadarUser> userList) {
        this.userList = userList;
    }

    @Override
    public int getItemCount() {
        return userList == null ? 0 : userList.size();
    }

    class TrackedItemViewHolder extends RecyclerView.ViewHolder {
        private TargetTrackedItemBinding binding;

        public TrackedItemViewHolder(TargetTrackedItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
