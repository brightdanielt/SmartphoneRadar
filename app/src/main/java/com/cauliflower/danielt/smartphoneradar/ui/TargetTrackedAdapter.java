package com.cauliflower.danielt.smartphoneradar.ui;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.cauliflower.danielt.smartphoneradar.R;
import com.cauliflower.danielt.smartphoneradar.databinding.TargetTrackedItemBinding;
import com.cauliflower.danielt.smartphoneradar.data.RadarUser;

import java.util.List;

public class TargetTrackedAdapter extends RecyclerView.Adapter<TargetTrackedAdapter.TrackedItemViewHolder> {
    private List<RadarUser> mUserList;
    private TargetTrackedClickCallback clickCallback;

    public TargetTrackedAdapter(@Nullable TargetTrackedClickCallback callback) {
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
        holder.binding.setTargetTracked(mUserList.get(position));
        holder.binding.executePendingBindings();
    }

    public void setUserList(List<RadarUser> userList) {
        if (mUserList == null) {
            mUserList = userList;
            notifyItemRangeInserted(0, userList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mUserList.size();
                }

                @Override
                public int getNewListSize() {
                    return userList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mUserList.get(oldItemPosition).getEmail().equals(
                            userList.get(newItemPosition).getEmail());
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    RadarUser oldUser = mUserList.get(oldItemPosition);
                    RadarUser newUser = userList.get(newItemPosition);
                    //todo 不知怎麼搞的，mUserList 比對前，已經更新成新的值，所以這個方法每次都回傳 true😠😠😠🔥🔥🔥
                    /*return oldUser.getEmail().equals(newUser.getEmail()) &&
                            oldUser.getPassword().equals(newUser.getPassword()) &&
                            oldUser.getUsedFor().equals(newUser.getUsedFor());*/
                    return false;
                }
            });
            mUserList = userList;
            result.dispatchUpdatesTo(this);
        }
    }

    @Override
    public int getItemCount() {
        return mUserList == null ? 0 : mUserList.size();
    }

    class TrackedItemViewHolder extends RecyclerView.ViewHolder {
        private TargetTrackedItemBinding binding;

        public TrackedItemViewHolder(TargetTrackedItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
