package info.papdt.express.helper.ui.fragment.home;

import android.annotation.SuppressLint;
import android.os.Bundle;

import info.papdt.express.helper.dao.PackageDatabase;
import info.papdt.express.helper.ui.adapter.HomePackageListAdapter;

@SuppressLint("ValidFragment")
public class FragmentAllNew extends BaseFragmentNew {

    private static final String ARG_TYPE = "arg_type";

    public static final int TYPE_ALL = 0, TYPE_DELIVERED = 1, TYPE_DELIVERING = 2;

    public static FragmentAllNew newInstance(PackageDatabase db, int type) {
        FragmentAllNew fragment = new FragmentAllNew(db);
        Bundle data = new Bundle();
        data.putInt(ARG_TYPE, type);
        fragment.setArguments(data);
        return fragment;
    }

    public FragmentAllNew(PackageDatabase database) {
        super(database);
    }

    public FragmentAllNew() {
        super();
    }

    @Override
    protected void setUpAdapter() {
        HomePackageListAdapter adapter = new HomePackageListAdapter(getDatabase(), getArguments().getInt(ARG_TYPE), getMainActivity());
        setAdapter(adapter);
    }

    @Override
    public int getFragmentId() {
        return getArguments().getInt(ARG_TYPE);
    }

}
