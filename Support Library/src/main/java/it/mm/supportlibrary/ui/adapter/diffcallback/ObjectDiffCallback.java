package it.mm.supportlibrary.ui.adapter.diffcallback;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public class ObjectDiffCallback extends DiffUtil.Callback {

    private final List<Object> mOldEmployeeList;
    private final List<Object> mNewEmployeeList;

    public ObjectDiffCallback(List<Object> oldEmployeeList, List<Object> newEmployeeList) {
        this.mOldEmployeeList = oldEmployeeList;
        this.mNewEmployeeList = newEmployeeList;
    }

    @Override
    public int getOldListSize() {
        return mOldEmployeeList.size();
    }

    @Override
    public int getNewListSize() {
        return mNewEmployeeList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldEmployeeList.get(oldItemPosition) == mNewEmployeeList.get(newItemPosition);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        final Object oldEmployee = mOldEmployeeList.get(oldItemPosition);
        final Object newEmployee = mNewEmployeeList.get(newItemPosition);

        return oldEmployee.equals(newEmployee);
    }

//    @Nullable
//    @Override
//    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
//        // Implement method if you're going to use ItemAnimator
//        return super.getChangePayload(oldItemPosition, newItemPosition);
//    }
}
